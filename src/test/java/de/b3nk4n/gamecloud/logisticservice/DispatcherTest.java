package de.b3nk4n.gamecloud.logisticservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.b3nk4n.gamecloud.logisticservice.message.OrderAcceptedMessage;
import de.b3nk4n.gamecloud.logisticservice.message.OrderDispatchedMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@FunctionalSpringBootTest
@Tag("IntegrationTest")
class DispatcherTest {
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private FunctionCatalog functionCatalog;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private ObjectMapper objectMapper;

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
        /*
         * Remarks: It looks like with Spring Cloud Stream 4.0.x, once a pipe (|) of the cloud functions is used,
         *          the result will always be of type GenericMessage instead.
         *          This was however not the case in Spring Cloud Stream 3.x.
         */
        final Function<OrderAcceptedMessage, Flux<GenericMessage<byte[]>>> packagingAndLabeling = functionCatalog
                .lookup(FunctionCatalog.class, "packaging|labeling");
        final long orderId = 123L;

        StepVerifier.create(packagingAndLabeling.apply(new OrderAcceptedMessage(orderId)))
                .assertNext(orderDispatchedMessage -> {
                    final var actualDispatchedOrder = deserializeGenericMessage(orderDispatchedMessage, OrderDispatchedMessage.class);
                    assertThat(actualDispatchedOrder.orderId()).isEqualTo(orderId);
                })
                .verifyComplete();
    }

    private <T> T deserializeGenericMessage(GenericMessage<byte[]> message, Class<T> clazz) {
        try {
            return objectMapper.readValue(message.getPayload(), clazz);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }
}
