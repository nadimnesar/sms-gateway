package io.github.nadimnesar.dispatcher.service;

import io.github.nadimnesar.common.enums.DeliveryStatus;
import io.github.nadimnesar.common.enums.Priority;
import io.github.nadimnesar.common.enums.TelecomOperator;
import io.github.nadimnesar.common.model.SmsMessage;
import io.github.nadimnesar.common.util.PhoneNumberUtil;
import io.github.nadimnesar.dispatcher.client.TelecomClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Priority-Aware SMS Dispatcher Service.
 *
 * Incoming messages from Kafka consumers are enqueued into a PriorityBlockingQueue,
 * ensuring messages are processed in strict priority order:
 * CRITICAL (Alerts) > HIGH (OTPs) > MEDIUM (Info) > LOW (Promotional / Survey).
 *
 * Messages with the same priority are processed in FIFO order according to createdAt timestamp.
 */
@Slf4j
@Service
public class SmsDispatcherService {

    private final Map<TelecomOperator, TelecomClient> clientMap = new EnumMap<>(TelecomOperator.class);
    private final PriorityBlockingQueue<SmsMessage> priorityQueue = new PriorityBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${sms.dispatcher.worker-threads:4}")
    private int workerThreadCount = 4;

    private ExecutorService workerExecutor;

    public SmsDispatcherService(List<TelecomClient> clients) {
        for (TelecomClient client : clients) {
            clientMap.put(client.getSupportedOperator(), client);
        }
    }

    @PostConstruct
    public synchronized void startWorkers() {
        if (running.compareAndSet(false, true)) {
            final AtomicInteger threadNumber = new AtomicInteger(1);
            this.workerExecutor = Executors.newFixedThreadPool(workerThreadCount, runnable -> {
                Thread thread = new Thread(runnable, "dispatcher-worker-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            });

            for (int i = 0; i < workerThreadCount; i++) {
                workerExecutor.submit(this::workerLoop);
            }
            log.info("Initialized Priority SMS Dispatcher with {} worker threads", workerThreadCount);
        }
    }

    @PreDestroy
    public synchronized void stopWorkers() {
        if (running.compareAndSet(true, false)) {
            log.info("Shutting down Priority SMS Dispatcher workers (queue remaining: {})...", priorityQueue.size());
            if (workerExecutor != null) {
                workerExecutor.shutdown();
                try {
                    if (!workerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        workerExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    workerExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Enqueues an SMS message into the priority dispatch queue for prioritized processing.
     * Messages are ordered by priority (CRITICAL > HIGH > MEDIUM > LOW), then FIFO.
     *
     * @param message SMS payload
     * @return true if accepted into the dispatch queue
     */
    public boolean dispatch(SmsMessage message) {
        if (message == null) {
            log.warn("Received null SMS message for dispatch");
            return false;
        }

        // Guarantee priority is populated
        if (message.getPriority() == null) {
            message.setPriority(Priority.fromSmsType(message.getSmsType()));
        }

        message.setStatus(DeliveryStatus.QUEUED);
        boolean offered = priorityQueue.offer(message);
        if (offered) {
            log.info("[PRIORITY DISPATCHER] Enqueued SMS [{}] with Priority: [{}] (Queue depth: {})",
                    message.getMessageId(), message.getPriority(), priorityQueue.size());
        } else {
            log.error("[PRIORITY DISPATCHER] Failed to enqueue SMS [{}]", message.getMessageId());
        }
        return offered;
    }

    /**
     * Dispatches a batch of messages into the priority queue.
     */
    public void dispatchAll(List<SmsMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        for (SmsMessage msg : messages) {
            dispatch(msg);
        }
    }

    /**
     * Synchronous direct dispatch to telecom client, bypassing the queue.
     */
    public boolean dispatchDirect(SmsMessage message) {
        return processMessage(message);
    }

    /**
     * Core worker processing loop. Continually extracts the highest priority SMS from the queue.
     */
    private void workerLoop() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                SmsMessage message = priorityQueue.take();
                processMessage(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                log.error("Unexpected error in SMS dispatch worker loop: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * Delivers an SMS message to the resolved telecom network client.
     */
    public boolean processMessage(SmsMessage message) {
        if (message == null) {
            log.warn("Received null SMS message for processing");
            return false;
        }

        TelecomOperator operator = message.getOperator();
        if (operator == null || operator == TelecomOperator.UNKNOWN) {
            operator = PhoneNumberUtil.resolveOperator(message.getRecipientPhoneNumber());
            message.setOperator(operator);
        }

        TelecomClient client = clientMap.get(operator);
        if (client == null) {
            log.error("No telecom client available for operator: {} (recipient: {})",
                    operator, message.getRecipientPhoneNumber());
            message.setStatus(DeliveryStatus.FAILED);
            return false;
        }

        message.setStatus(DeliveryStatus.PROCESSING);
        try {
            log.info("[DISPATCH WORKER] Delivering [{}] SMS ID: {} [Type: {}] to {} via {}",
                    message.getPriority(), message.getMessageId(), message.getSmsType(),
                    message.getRecipientPhoneNumber(), operator);

            boolean success = client.sendSms(message);
            if (success) {
                message.setStatus(DeliveryStatus.SENT);
                log.info("Successfully dispatched [{}] SMS ID: {} via {}",
                        message.getPriority(), message.getMessageId(), operator);
                return true;
            } else {
                message.setStatus(DeliveryStatus.FAILED);
                log.error("Failed to dispatch [{}] SMS ID: {} via {}",
                        message.getPriority(), message.getMessageId(), operator);
                return false;
            }
        } catch (Exception ex) {
            message.setStatus(DeliveryStatus.FAILED);
            log.error("Exception while dispatching [{}] SMS ID: {} via {}: {}",
                    message.getPriority(), message.getMessageId(), operator, ex.getMessage(), ex);
            return false;
        }
    }

    public int getQueueSize() {
        return priorityQueue.size();
    }

    public TelecomClient getClientForOperator(TelecomOperator operator) {
        return clientMap.get(operator);
    }
}
