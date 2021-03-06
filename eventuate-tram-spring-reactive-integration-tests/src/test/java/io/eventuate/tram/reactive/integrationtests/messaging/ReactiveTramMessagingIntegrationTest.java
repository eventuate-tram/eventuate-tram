package io.eventuate.tram.reactive.integrationtests.messaging;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.integrationtests.IdSupplier;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.spring.messaging.producer.jdbc.reactive.ReactiveTramMessageProducerJdbcConfiguration;
import io.eventuate.tram.spring.reactive.consumer.common.ReactiveTramConsumerCommonConfiguration;
import io.eventuate.tram.spring.reactive.consumer.kafka.EventuateTramReactiveKafkaMessageConsumerConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveTramMessagingIntegrationTest.Config.class)
public class ReactiveTramMessagingIntegrationTest {

  @Import({ReactiveTramMessageProducerJdbcConfiguration.class,
          EventuateTramReactiveKafkaMessageConsumerConfiguration.class,
          ReactiveTramConsumerCommonConfiguration.class})
  @EnableAutoConfiguration
  public static class Config {

  }

  @Autowired
  protected ReactiveMessageProducer messageProducer;

  @Autowired
  protected ReactiveMessageConsumer messageConsumer;

  private String destination;
  private String subscriberId;
  private String payload;

  @Before
  public void init() {
    destination = IdSupplier.get();
    subscriberId = IdSupplier.get();
    payload = "\"" + IdSupplier.get() + "\"";
  }

  @Test
  public void shouldSendAndReceiveMessage() throws InterruptedException {
    BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    messageConsumer.subscribe(subscriberId, Collections.singleton(destination), message ->
      Mono.defer(() -> Mono.just(messageQueue.add(message.getPayload()))));

    messageProducer.send(destination, MessageBuilder.withPayload(payload).build()).block();

    Assert.assertEquals(payload, messageQueue.poll(10, TimeUnit.SECONDS));
  }
}
