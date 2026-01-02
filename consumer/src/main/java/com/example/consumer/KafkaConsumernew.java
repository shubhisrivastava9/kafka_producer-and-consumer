package com.example.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class KafkaConsumernew {
    @Bean
    public Consumer<RiderLocation> processRiderLocation() {
        return location -> {
            System.out.println("Received: " + location.getRiderId()
                    + " @ " + location.getLatitude() + ", " + location.getLongitude());
        };
    }
    @Bean
    public Consumer<RiderLocation> processRiderstatus() {
        return location -> {
            System.out.println("Received: " + location.getRiderId()
                    + " @ " + location.getLatitude() + ", " + location.getLongitude()+"completed");
        };
    }

}
