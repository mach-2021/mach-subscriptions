package com.mach.subscription.consumer;

import com.google.pubsub.v1.PubsubMessage;
import com.mach.subscription.service.SubscriptionService;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.products.messages.ProductPublishedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class CTProductConsumer implements Consumer<BasicAcknowledgeablePubsubMessage> {

    private final SubscriptionService subscriptionService;

    @Override
    public void accept(BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage) {
        final PubsubMessage pubsubMessage = basicAcknowledgeablePubsubMessage.getPubsubMessage();
        log.info("Start accept message. Message publish time: {}. Message id: {}",
                pubsubMessage.getPublishTime(), pubsubMessage.getMessageId());
        log.info(pubsubMessage.getData().toStringUtf8());
        try {
            ProductPublishedMessage productPublishedMessage = SphereJsonUtils.readObject(pubsubMessage.getData().toStringUtf8(), ProductPublishedMessage.class);
            log.info(productPublishedMessage.getType());
            if ("ProductPublished".equals(productPublishedMessage.getType())) {
                log.info("Product projection {}", productPublishedMessage.getProductProjection());
                subscriptionService.saveProductToAlgolia(productPublishedMessage.getProductProjection());
            }
            basicAcknowledgeablePubsubMessage.ack();
        } catch (final Exception exception) {
            log.error("Error from service ", exception);
        }
    }
}
