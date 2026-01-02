# Kafka notes


<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/ebfbd767-e3c4-46ec-8137-2341813273ec" />



## What is Apache Kafka?

Apache Kafka is a **distributed event-streaming platform** used to build **real-time, scalable, fault-tolerant systems**. It allows applications to **publish, store, and consume streams of records** efficiently.

In microservices architecture, Kafka acts as a **message backbone** between services.

---

## 2. Why Kafka is Needed (Problem → Solution)

### Without Kafka (Tightly Coupled System)

- Services communicate using synchronous REST calls
- Failure of one service impacts others
- Poor scalability and high latency

### With Kafka (Loosely Coupled System)

- Services communicate asynchronously
- Producers and consumers are independent
- High throughput and fault tolerance

**Key Reasons Kafka is Used:**

- Loose coupling
- Asynchronous communication
- Message durability
- Horizontal scalability
- Replayability of events

---

## 3. Kafka Architecture (High-Level)

Producer  --->  Kafka Broker(s)  --->  Consumer

|   |   |

Topic with Partitions

Kafka stores data in **topics**, which are divided into **partitions** and distributed across **brokers**.

---

## 4. Core Kafka Components (Detailed)

### 4.1 Producer

A **producer** sends messages (events) to Kafka topics.

Responsibilities:

- Serialize data
- Choose topic and partition
- Handle acknowledgements and retries

Example (Spring Boot):

- Product Service sending product/order events

---

### 4.2 Topic

A **topic** is a logical category to which messages are sent.

Characteristics:

- Append-only
- Immutable records
- Can have multiple partitions

Example:

- `product-topic`

---

### 4.3 Partitions (Very Important)

A topic is split into **multiple partitions** for scalability and parallelism.

product-topic

├── Partition 0 (offset 0,1,2...)

├── Partition 1 (offset 0,1,2...)

└── Partition 2 (offset 0,1,2...)

Key Points:

- Order is guaranteed **only within a partition**
- Each partition is an ordered log
- Enables parallel consumption

---

### 4.4 Broker

A **broker** is a Kafka server that:

- Stores partitions
- Handles produce and consume requests
- Replicates data

Kafka cluster = multiple brokers

---

### 4.5 Consumer

A **consumer** reads messages from Kafka topics.

Responsibilities:

- Deserialize messages
- Track offsets
- Process data

Consumers pull data from Kafka (poll model).

---

### 4.6 Consumer Group (Interview Favorite)

A **consumer group** is a set of consumers working together.

### Case 1: Same Topic, Same Group ID

Topic (3 partitions)

├── Consumer-1 (P0)

├── Consumer-2 (P1)

└── Consumer-3 (P2)

- Each message processed **once**
- Load balancing

### Case 2: Same Topic, Different Group IDs

Group-A → gets all messages

Group-B → gets all messages

- Fan-out pattern
- Independent processing

---

## 5. Message Flow (End-to-End)

Client Request

|

v

REST Controller

|

v

Kafka Producer

|

v

Kafka Topic → Partition → Broker

|

v

Consumer Group

|

v

Consumer Logic

---

## 6. Offsets and Message Tracking

### What is Offset?

- A unique sequential ID per message per partition

Partition 0:

Offset 0 → msg1

Offset 1 → msg2

Offset 2 → msg3

Offsets are stored per:

Consumer Group + Topic + Partition

Why Offsets Matter:

- Resume consumption
- Fault tolerance
- Replay messages

---

## 7. Serialization & Deserialization

Kafka stores data as **bytes**.

### Producer Side

- Serializer converts Java object → bytes

### Consumer Side

- Deserializer converts bytes → Java object

Common serializers:

- String
- JSON
- Avro

---

## 8. Kafka Configuration Properties (Why Each Matters)

### Producer Properties

- `bootstrap.servers` → Kafka address
- `key/value.serializer` → Data conversion
- `acks` → Reliability guarantee
- `retries` → Retry on failure

### Consumer Properties

- `group.id` → Consumer grouping
- `auto-offset-reset` → Start position
- `enable-auto-commit` → Offset control
- `key/value.deserializer` → Data conversion

---

## 9. Delivery Semantics

| Type | Description |
| --- | --- |
| At-most-once | No retry, possible loss |
| At-least-once | Retry possible duplicates |
| Exactly-once | No loss, no duplicates |

Kafka supports **Exactly-Once Semantics (EOS)** using:

- Idempotent producers
- Transactions

---

## 10. Fault Tolerance & Reliability

- Data replicated across brokers
- Leader–Follower model
- Automatic leader election
- Consumer rebalancing on failure

---

## 

# PRODUCER (Who sends data)

## 📘 Theory

A **Producer** creates events and sends them to Kafka.

- Producer does **not know consumers**
- Producer sends data **only to a topic**

---

## 💻 Code Example (Producer)

```java
@Configuration
publicclassProducerConfig {

@Bean
public Supplier<String>producer() {
return () -> {
Stringmsg="OrderCreated";
            System.out.println("Producing: " + msg);
return msg;
        };
    }
}

```

### application.yml

```yaml
spring:
cloud:
function:
definition:producer
stream:
bindings:
producer-out-0:
destination:order-topic

```

---

## 🔁 Flow Diagram

```
Producer ──▶order-topic

```

## 🖨 Output

```
Producing: OrderCreated

```

---

# 2️⃣ TOPIC (What data is stored)

## 📘 Theory

A **Topic** is a **named stream of events**.

- Stores events in order
- Multiple producers & consumers can use it

---

## 💻 Code Example (Sending to topic)

```yaml
destination:order-topic

```

---

## 🔁 Flow Diagram

```
Producer ──▶order-topic ──▶ Consumer

```

---

# 3️⃣ PARTITION (How Kafka scales)

## 📘 Theory

A **partition** is a **physical division** of a topic.

- Enables parallelism
- Order guaranteed **per partition**

---

## 💻 Code Example (Create topic with partitions)

```java
@Bean
public NewTopictopic() {
return TopicBuilder.name("order-topic")
        .partitions(3)
        .replicas(1)
        .build();
}

```

---

## 🔁 Flow Diagram

```
order-topic
 ├─Partition0
 ├─Partition1
 └─Partition2

```

---

# 4️⃣ CONSUMER (Who reads data)

## 📘 Theory

A **Consumer** reads messages from Kafka and processes them.

---

## 💻 Code Example

```java
@Configuration
publicclassConsumerConfig {

@Bean
public Consumer<String>consumer() {
return msg -> System.out.println("Consumed: " + msg);
    }
}

```

### application.yml

```yaml
spring:
cloud:
function:
definition:consumer
stream:
bindings:
consumer-in-0:
destination:order-topic
group:order-group

```

---

## 🔁 Flow Diagram

```
order-topic ──▶ Consumer

```

## 🖨 Output

```
Consumed: OrderCreated

```

---

# 5️⃣ CONSUMER GROUP (MOST IMPORTANT)

## 📘 Theory

A **Consumer Group** defines **delivery rules**.

### Rule:

```
One message →One consumerPERgroup

```

---

## 💻 Code Example (Two consumers, SAME group)

```yaml
consumerA-in-0:
destination:order-topic
group:order-group

consumerB-in-0:
destination:order-topic
group:order-group

```

---

## 🔁 Flow Diagram (Load balancing)

```
order-topic
                │
        ┌───────┴────────┐
        │order-group   │
        │                │
    ConsumerA       ConsumerB

```

## 🖨 Output

```
ConsumerA:OrderCreated
ConsumerB:OrderCreated

```

(Distributed, not duplicated)

---

# 6️⃣ DIFFERENT CONSUMER GROUPS (Broadcast)

## 📘 Theory

Different groups → **same message delivered to all**

---

## 💻 Code Example

```yaml
consumerA-in-0:
destination:order-topic
group:billing-group

consumerB-in-0:
destination:order-topic
group:shipping-group

```

---

## 🔁 Flow Diagram

```
order-topic
                       │
          ┌────────────┴────────────┐
          │                         │
    billing-group              shipping-group

```

## 🖨 Output

```
Billing received:OrderCreated
Shipping received:OrderCreated

```

---

# 7️⃣ GROUP ID (Offset ownership)

## 📘 Theory

`group.id` decides:

- Who owns offsets
- Where consumer resumes from

Changing group ID = **new consumer**

---

## 💻 Code Example

```yaml
group:order-group-v2

```

---

## 🔁 Flow Diagram

```
Oldgroup →offset20
Newgroup →offset0

```

## 🖨 Output

```
Reprocessingold messages

```

---

# 8️⃣ DIFFERENT TOPICS (Isolation)

## 📘 Theory

Different topics = different data streams.

---

## 💻 Code Example

```yaml
producer-out-0:
destination:payment-topic

consumer-in-0:
destination:payment-topic

```

---

## 🔁 Flow Diagram

```
Producer ──▶ payment-topic ──▶ Payment Consumer

```

---

# 9️⃣ OFFSET (Kafka memory)

## 📘 Theory

Offset = message position in partition.

---

## 🔁 Flow Diagram

```
Partition0:
offset0 → msg A
offset1 → msg B
offset2 → msg C

```

---

# 🔟 REBALANCING (Automatic)

## 📘 Theory

Kafka reassigns partitions when:

- Consumer joins/leaves
- Partitions change

---

## 🔁 Flow Diagram

```
Consumer joins → Pause →Reassign → Resume

```

---

# 1️⃣1️⃣ DELIVERY GUARANTEES

| Type | Meaning |
| --- | --- |
| At-most-once | No duplicates |
| At-least-once | No loss (default) |
| Exactly-once | No loss, no duplicates |

---

# 1️⃣2️⃣ FINAL MASTER DIAGRAM (ALL TOGETHER)

```
Producer
   │
   ▼
Topic (Partitions)
   │
   ├── Consumer GroupA → ServiceA
   ├── Consumer GroupB → ServiceB
   └── Consumer Group C → Service C

```

## Architecture Diagram (Flow)

```
┌────────────────────┐
│ProducerApp│(port8070)
││
│Supplier<RiderLocation>
│sendRiderLocation│
└─────────┬──────────┘
│JSON
▼
┌───────────────┐
│Kafka│
│Topic:│
│my-topic│
└─────┬─────────┘
│
┌───────┴──────────┐
││
▼▼
processRiderLocationprocessRiderstatus
(Consumer)(Consumer)
Group:my-new-group
(port8060)

```

---

## 3️⃣ Producer Side – Spring Cloud Stream

### 📄 `KafkaProducerStream.java`

```java
@Bean
public Supplier<RiderLocation>sendRiderLocation() {
Randomrandom=newRandom();
return () -> {
RiderLocationlocation=
newRiderLocation("rider"+random.nextInt(20),16.7,88.2);
        System.out.println("Sending: " + location.getRiderId());
return location;
    };
}

```

### What happens:

- Every **1 second**, Spring calls this `Supplier`
- Creates a `RiderLocation`
- Publishes it to Kafka as **JSON**

---

### 📄 Producer `application.yml`

```yaml
spring:
cloud:
function:
definition:sendRiderLocation

stream:
poller:
fixed-delay:1000

bindings:
sendRiderLocation-out-0:
destination:my-topic
content-type:application/json

```

### Key properties explained

| Property | Meaning |
| --- | --- |
| `definition` | Activates `sendRiderLocation()` |
| `fixed-delay` | Emit every 1 second |
| `destination` | Kafka topic name |
| `content-type` | Auto JSON serialization |

---

## 4️⃣ Producer Side – REST KafkaTemplate

### 📄 `KafkaProducer.java`

```java
@PostMapping("/send")
public StringsendMessage(@RequestParam String message) {
    KafkaTemplate.send("my-topic", message);
return"message sent:" + message;
}

```

### Flow:

```
POST /api/send?message=hello
        ↓
KafkaTemplate
        ↓
Kafka topic:my-topic

```

⚠️ This sends **String messages**, not JSON `RiderLocation`.

---

## 5️⃣ Consumer Side – Spring Cloud Stream

### 📄 `KafkaConsumernew.java`

```java
@Bean
public Consumer<RiderLocation>processRiderLocation() {
return location -> {
        System.out.println(
"Received: " + location.getRiderId()
            +" @ " + location.getLatitude()
            +", " + location.getLongitude());
    };
}

@Bean
public Consumer<RiderLocation>processRiderstatus() {
return location -> {
        System.out.println(
"Received: " + location.getRiderId()
            +" @ " + location.getLatitude()
            +", " + location.getLongitude() +"completed");
    };
}

```

### Important

Both consumers:

- Read from **same Kafka topic**
- Belong to **same consumer group**

---

### 📄 Consumer `application.yml`

```yaml
spring:
cloud:
function:
definition:processRiderLocation;processRiderstatus

stream:
bindings:
processRiderLocation-in-0:
destination:my-topic
group:my-new-group

processRiderstatus-in-0:
destination:my-topic
group:my-new-group

```

### Key concepts

| Concept | Explanation |
| --- | --- |
| Multiple consumers | Both functions active |
| Same group | Kafka load-balances messages |
| `-in-0` | First input binding |
| JSON | Auto deserialization |

⚠️ **Kafka will deliver each message to only ONE consumer instance per group**, not both.

---

## 6️⃣ Kafka Topic Configuration

### 📄 `KafkaTopicConfig.java`

```java
@Bean
public NewTopicordersTopic() {
return TopicBuilder.name("orders")
        .partitions(3)
        .replicas(1)
        .build();
}

```

### Important

- This creates topic **`orders`**
- ❌ But your apps use **`my-topic`**
- So this bean is currently **unused**

✅ Either:

- Change topic to `my-topic`
- Or update bindings to `orders`

---

# SHARED DOMAIN MODEL

```java
publicclassRiderEvent {

private String riderId;
private String eventType;

publicRiderEvent() {}

publicRiderEvent(String riderId, String eventType) {
this.riderId = riderId;
this.eventType = eventType;
    }

// getters & setters
}

```

Kafka messages = **JSON**

---

# 3️⃣ PRODUCER (COMMON FOR ALL CASES)

### Producer Code

```java
@Configuration
publicclassRiderProducer {

@Bean
public Supplier<RiderEvent>sendRiderEvent() {
return () -> {
RiderEventevent=
newRiderEvent("rider-" +newRandom().nextInt(5),"LOCATION");

            System.out.println("Producing → " + event.getRiderId());
return event;
        };
    }
}

```

---

### Producer `application.yml`

```yaml
spring:
application:
name:producer

cloud:
function:
definition:sendRiderEvent

stream:
poller:
fixed-delay:2000

bindings:
sendRiderEvent-out-0:
destination:rider-topic
content-type:application/json

kafka:
binder:
brokers:localhost:9092

```

---

### Producer Output

```
Producing → rider-1
Producing → rider-3
Producing → rider-0

```

---

# 4️⃣ CONSUMERS (3 Consumers)

```java
@Configuration
publicclassRiderConsumers {

@Bean
public Consumer<RiderEvent>locationService() {
return e ->
            System.out.println("Location Service → " + e.getRiderId());
    }

@Bean
public Consumer<RiderEvent>statusService() {
return e ->
            System.out.println("Status Service → " + e.getRiderId());
    }

@Bean
public Consumer<RiderEvent>auditService() {
return e ->
            System.out.println("Audit Service → " + e.getRiderId());
    }
}

```

---

# 5️⃣ CASE 1 — SAME TOPIC, SAME CONSUMER GROUP

### (Load Balancing)

### Consumer `application.yml`

```yaml
spring:
cloud:
function:
definition:locationService;statusService

stream:
bindings:
locationService-in-0:
destination:rider-topic
group:rider-group

statusService-in-0:
destination:rider-topic
group:rider-group

```

### Architecture

```
              rider-topic
                   │
            ┌──────┴──────┐
            │ rider-group │
            │             │
    locationService   statusService

```

### Output

```
Location Service → rider-1
Status Service → rider-3
Location Service → rider-0

```

✔ One message → ONE consumer

✔ Used for scaling

---

# 6️⃣ CASE 2 — SAME TOPIC, DIFFERENT CONSUMER GROUPS

### (Broadcast)

### Consumer `application.yml`

```yaml
spring:
cloud:
function:
definition:locationService;statusService;auditService

stream:
bindings:
locationService-in-0:
destination:rider-topic
group:location-group

statusService-in-0:
destination:rider-topic
group:status-group

auditService-in-0:
destination:rider-topic
group:audit-group

```

### Architecture

```
                      rider-topic
                           │
     ┌──────────────┬──────┴──────┬──────────────┐
     │              │             │              │
location-group  status-group  audit-group

```

### Output

```
Location Service → rider-2
Status Service → rider-2
Audit Service → rider-2

```

✔ Same message → ALL groups

✔ Used for fan-out

---

# 7️⃣ CASE 3 — DIFFERENT TOPICS

### (Isolation)

### Producer

```yaml
sendRiderEvent-out-0:
destination:rider-location-topic

```

### Consumer

```yaml
locationService-in-0:
destination:rider-location-topic
group:location-group

statusService-in-0:
destination:rider-status-topic
group:status-group

```

### Architecture

```
rider-location-topic →Location Service
rider-status-topic   → Status Service

```

### Output

```
Location Service → rider-4
Status Service → rider-4

```

✔ Clean data separation

✔ Independent scaling

---

# 8️⃣ ALL CASES IN ONE DIAGRAM

```
                         Producer
                            │
                     ┌──────┴──────┐
                     │ Kafka Topic │
                     │  rider-topic│
                     └──────┬──────┘
                            │
        ┌──────────────┬────┴────┬──────────────┐
        │              │         │              │
location-group   status-group audit-group

```


