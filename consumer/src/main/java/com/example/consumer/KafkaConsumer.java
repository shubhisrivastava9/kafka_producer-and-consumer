//package com.example.consumer;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KafkaConsumer {
//
//    @KafkaListener(topics="my-topic",groupId = "my-new-group")
//public  void listen(String message)
//    {
//        System.out.println("Recieved Message"+message);
//    }
//    @KafkaListener(topics="my-topic",groupId = "my-new-group")
//    public  void listen2(String message)
//    {
//        System.out.println("Recieved Message"+message);
//    }
//
//}
