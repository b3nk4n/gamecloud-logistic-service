package de.b3nk4n.gamecloud.logisticservice;

import de.b3nk4n.gamecloud.logisticservice.message.OrderAcceptedMessage;
import de.b3nk4n.gamecloud.logisticservice.message.OrderDispatchedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    @Bean
    public Function<OrderAcceptedMessage, Long> packaging() {
        return orderAccepted -> {
            log.info("Order with ID={} is packed", orderAccepted.orderId());
            return orderAccepted.orderId();
        };
    }

    @Bean
    public Function<Flux<Long>, Flux<OrderDispatchedMessage>> labeling() {
        return orderFlux -> orderFlux.map(orderId -> {
            log.info("The order with ID={} is labeled.", orderId);
            return new OrderDispatchedMessage(orderId);
        });
    }
}
