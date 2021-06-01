package io.eventuate.tram.consumer.common.reactive;

import io.eventuate.tram.messaging.common.SubscriberIdAndMessage;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class DecoratedReactiveMessageHandlerFactory {

  private List<ReactiveMessageHandlerDecorator> decorators;
  private List<ReactiveMessageHandlerDecorator> decoratorsWithReversedOrder;

  public DecoratedReactiveMessageHandlerFactory(List<ReactiveMessageHandlerDecorator> decorators) {
    this.decorators = decorators;
    decorators.sort(Comparator.comparingInt(ReactiveMessageHandlerDecorator::getOrder));

    decoratorsWithReversedOrder = new ArrayList<>(decorators);
    Collections.reverse(decoratorsWithReversedOrder);
  }

  public Function<SubscriberIdAndMessage, Mono<Void>> decorate(ReactiveMessageHandler reactiveMessageHandler) {
    return subscriberIdAndMessage -> {

      Mono<Void> processingFlow = Mono.defer(() -> reactiveMessageHandler.apply(subscriberIdAndMessage.getMessage()));

      for (ReactiveMessageHandlerDecorator decorator : decorators) {
        processingFlow = decorator.accept(subscriberIdAndMessage, processingFlow);
      }

      return processingFlow;
    };
  }
}
