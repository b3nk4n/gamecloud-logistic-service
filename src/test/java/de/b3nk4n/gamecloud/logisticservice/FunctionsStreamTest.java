package de.b3nk4n.gamecloud.logisticservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.b3nk4n.gamecloud.logisticservice.message.OrderAcceptedMessage;
import de.b3nk4n.gamecloud.logisticservice.message.OrderDispatchedMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@Tag("IntegrationTest")
public class FunctionsStreamTest {
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private InputDestination inputDestination;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private OutputDestination outputDestination;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private ObjectMapper objectMapper;

    @Test
    void whenOrderAcceptedThenDispatched() throws IOException {
        final long orderId = 123L;

        final var inputMessage = MessageBuilder
                .withPayload(new OrderAcceptedMessage(orderId))
                .build();
        final var expectedOutputMessage = MessageBuilder
                .withPayload(new OrderDispatchedMessage(orderId))
                .build();

        inputDestination.send(inputMessage);

        final var actualOutputMessage = objectMapper
                .readValue(outputDestination.receive().getPayload(), OrderDispatchedMessage.class);
        assertThat(actualOutputMessage).isEqualTo(expectedOutputMessage.getPayload());
    }
}
