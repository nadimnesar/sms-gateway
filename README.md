# sms-gateway

## Diagram

```mermaid
flowchart TB
    A["Producer"]
    subgraph Topics["Kafka Topics"]
        direction TB
        T1["ALERT"]
        T2["OTP"]
        T3["INFO"]
        T4["PROMOTIONAL"]
        T5["SURVEY"]
        P1["P0 | P1 | P2 | P3"]
        P2["P0 | P1 | P2"]
        P3["P0 | P1"]
        P4["P0"]
        P5["P0"]
        T1 --> P1
        T2 --> P2
        T3 --> P3
        T4 --> P4
        T5 --> P5
    end

    subgraph Consumers["Kafka Consumer Groups"]
        G1["Critical - 4 Consumers"]
        G2["High - 3 Consumers"]
        G3["Medium - 2 Consumers"]
        G4["Low - 2 Consumers"]
    end

    S["Dispatcher"]

    subgraph Telecom["Telecom Networks"]
        GP["Grameenphone"]
        RB["Robi"]
        TT["Teletalk"]
        BL["Banglalink"]
    end

    A --> T1 & T2 & T3 & T4 & T5
    P1 --> G1
    P2 --> G2
    P3 --> G3
    P4 --> G4
    P5 --> G4
    G1 & G2 & G3 & G4 --> S
    S --> GP & RB & TT & BL
```

## Partition Routing Strategy

Each SMS uses the recipient's **normalized phone number** as the Kafka message key.

### How It Works

Kafka determines the target partition using Murmur2 hash:

```text
partition = abs(MurmurHash2(phoneNumber)) % total_partitions
```

| Topic           | Partitions | Routing                          |
|:----------------|:----------:|:---------------------------------|
| **ALERT**       |     4      | `hash % 4` &rarr; P0, P1, P2, P3 |
| **OTP**         |     3      | `hash % 3` &rarr; P0, P1, P2     |
| **INFO**        |     2      | `hash % 2` &rarr; P0, P1         |
| **PROMOTIONAL** |     1      | Always P0                        |
| **SURVEY**      |     1      | Always P0                        |

### Key Benefits

1. **Strict FIFO per Recipient**: All SMS for the same phone number always land on the same partition in chronological
   order.
2. **Uniform Load Balancing**: Different phone numbers distribute evenly across all partitions.

## Message Priority & Dispatch Processing

Each SMS message carries a priority level:

| Priority | Level | Default Topic | Description |
|:---|:---:|:---|:---|
| **CRITICAL** | 1 | `ALERT` | Urgent system & security alerts |
| **HIGH** | 2 | `OTP` | Time-sensitive one-time passwords |
| **MEDIUM** | 3 | `INFO` | Account notifications & transactional updates |
| **LOW** | 4 | `PROMOTIONAL`, `SURVEY` | Marketing campaigns & feedback requests |

### Dispatch Worker Processing

- **Priority Queue**: All consumer groups deliver messages into the Dispatcher's `PriorityBlockingQueue`.
- **Strict Precedence**: Worker threads immediately dequeue and process higher-priority messages first (`CRITICAL` &gt; `HIGH` &gt; `MEDIUM` &gt; `LOW`).
- **FIFO Fairness**: Messages sharing identical priority are processed in FIFO order based on their creation timestamp (`createdAt`).


