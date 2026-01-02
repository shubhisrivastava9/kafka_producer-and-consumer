package com.example.producer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.function.Supplier;

@Configuration
public class KafkaProducerStream {
    @Bean
    public Supplier<RiderLocation> sendRiderLocation() {
        Random random = new Random();
        return () -> {

            RiderLocation location = new RiderLocation("rider"+random.nextInt(20),
                    16.7, 88.2);
            System.out.println("Sending: " + location.getRiderId());
            return location;
        };

    }


}
