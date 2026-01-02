ood Delivery App (Swiggy / Zomato–style)
🧠 Problem Without Kafka (Real Life)

Imagine a food delivery app when an order is placed.

❌ Without Kafka (Synchronous – REST)
Order Service
   ↓ REST
Payment Service
   ↓ REST
Restaurant Service
   ↓ REST
Delivery Service

Problems:

If Payment Service is down → order fails

Slow response

Tight coupling

Hard to scale

✅ Solution With Kafka (Real Life)

Kafka acts like a NEWS CHANNEL / MESSAGE BOARD.

Order Service (Producer)
        ↓
   Kafka Topic: order-created
        ↓
 ┌──────────┬──────────┬──────────┐
 Payment   Restaurant   Delivery
 Service    Service     Service
 (Consumers)


✔ Order service doesn’t wait
✔ Services work independently
✔ System is scalable & fault-tolerant

🔹 Kafka Components (Mapped to Real Life)
Kafka Term	Real-Life Meaning
Producer	Order Service
Topic	Order Notice Board
Partition	Multiple lanes
Consumer	Payment / Delivery
Consumer Group	Team of workers
Offset	Read position
🧪 COMPLETE REAL EXAMPLE (Spring Boot)
1️⃣ Producer – Order Service

📌 When order is placed → publish event to Kafka

OrderEvent.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String orderId;
    private String status;
}

OrderProducer.java
@Configuration
public class OrderProducer {

    @Bean
    public Supplier<OrderEvent> orderProducer() {
        return () -> {
            OrderEvent event =
                new OrderEvent(UUID.randomUUID().toString(), "ORDER_CREATED");

            System.out.println("Order Created → " + event.getOrderId());
            return event;
        };
    }
}

application.yml (Producer)
spring:
  cloud:
    function:
      definition: orderProducer
    stream:
      poller:
        fixed-delay: 2000
      bindings:
        orderProducer-out-0:
          destination: order-created
          content-type: application/json


📤 Output

Order Created → 123-abc

2️⃣ Kafka Topic (Order Board)
@Bean
public NewTopic orderTopic() {
    return TopicBuilder.name("order-created")
            .partitions(3)
            .replicas(1)
            .build();
}


📌 Why partitions?

3 partitions → 3 orders processed in parallel

3️⃣ Consumer 1 – Payment Service
@Bean
public Consumer<OrderEvent> paymentService() {
    return event ->
        System.out.println("Payment done for → " + event.getOrderId());
}

paymentService-in-0:
  destination: order-created
  group: payment-group

4️⃣ Consumer 2 – Restaurant Service
@Bean
public Consumer<OrderEvent> restaurantService() {
    return event ->
        System.out.println("Restaurant preparing order → " + event.getOrderId());
}

restaurantService-in-0:
  destination: order-created
  group: restaurant-group

5️⃣ Consumer 3 – Delivery Service
@Bean
public Consumer<OrderEvent> deliveryService() {
    return event ->
        System.out.println("Delivery assigned for → " + event.getOrderId());
}

deliveryService-in-0:
  destination: order-created
  group: delivery-group

🔁 Message Flow (Broadcast Example)
order-created topic
      |
 ┌────┼────────┬────────┐
Payment  Restaurant  Delivery
 group      group        group


📥 Output

Payment done for → 123
Restaurant preparing → 123
Delivery assigned → 123


✔ Same message → ALL services
✔ Perfect for microservices

🔥 Consumer Group Explained (Real Life)
Same group = Team of workers
delivery-group
   |
 ┌─┴──┐
 D1  D2


✔ One order → only ONE delivery agent
✔ Used for scaling

🔁 Offset Explained (Real Life)
Partition 0
Order1 → offset 0
Order2 → offset 1
Order3 → offset 2


If Delivery Service crashes:

Kafka resumes from last offset

No data loss
