package com.example.producer;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KafkaProducer {

    private final KafkaTemplate<String,String>KafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        KafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam String message)
    {
        KafkaTemplate.send("my-topic",message);
        return "message sent:"+message;
    }


}
