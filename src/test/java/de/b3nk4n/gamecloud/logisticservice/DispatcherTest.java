package de.b3nk4n.gamecloud.logisticservice;

import de.b3nk4n.gamecloud.logisticservice.message.OrderAcceptedMessage;
import de.b3nk4n.gamecloud.logisticservice.message.OrderDispatchedMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@FunctionalSpringBootTest
@Tag("IntegrationTest")
class DispatcherTest {
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private FunctionCatalog functionCatalog;

    @Test
    void orderPackaging() {
        final Function<OrderAcceptedMessage, Long> packaging = functionCatalog
                .lookup(FunctionCatalog.class, "packaging");
        final long orderId = 123L;

        final var acceptedOrderId = packaging.apply(new OrderAcceptedMessage(orderId));

        assertThat(acceptedOrderId).isEqualTo(orderId);
    }

    @Test
    void orderLabeling() {
        final Function<Flux<Long>, Flux<OrderDispatchedMessage>> labeling = functionCatalog
                .lookup(FunctionCatalog.class, "labeling");
        final long orderId = 123L;

        StepVerifier.create(labeling.apply(Flux.just(orderId)))
                .assertNext(orderDispatchedMessage -> assertThat(orderDispatchedMessage.orderId()).isEqualTo(orderId))
                .verifyComplete();
    }

    @Test
    void orderPackagingAndLabeling() {
        final Function<OrderAcceptedMessage, Flux<OrderDispatchedMessage>> packagingAndLabeling = functionCatalog
                .lookup(FunctionCatalog.class, "packaging|labeling");
        final long orderId = 123L;

        StepVerifier.create(packagingAndLabeling.apply(new OrderAcceptedMessage(orderId)))
                .assertNext(orderDispatchedMessage -> assertThat(orderDispatchedMessage.orderId()).isEqualTo(orderId))
                .verifyComplete();
    }
}
