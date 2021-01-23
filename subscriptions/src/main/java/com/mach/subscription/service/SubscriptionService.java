package com.mach.subscription.service;

import com.mach.subscription.repository.SubscriptionRepository;
import io.sphere.sdk.products.messages.ProductPublishedMessage;
import io.sphere.sdk.subscriptions.MessageSubscription;
import io.sphere.sdk.subscriptions.PubSubDestination;
import io.sphere.sdk.subscriptions.Subscription;
import io.sphere.sdk.subscriptions.SubscriptionDraft;
import io.sphere.sdk.subscriptions.SubscriptionDraftBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {

    private static final String PRODUCT_PUBLISHED_SUBSCRIPTION_KEY = "sub-product-published";
    private final SubscriptionRepository subscriptionRepository;
    private final GcpProjectIdProvider gcpProjectIdProvider;

    @EventListener(ApplicationStartedEvent.class)
    public void createSubscription() {
        PubSubDestination pubSubDestination = PubSubDestination.of(gcpProjectIdProvider.getProjectId(),
                "ct-products");
        final Subscription subscription = createProductPublishedSubscription(pubSubDestination);
        log.info("Subscription was created {}", subscription.getId());
    }

    public Subscription createProductPublishedSubscription(PubSubDestination pubSubDestination) {
        Subscription subscription = subscriptionRepository.findSubscription(PRODUCT_PUBLISHED_SUBSCRIPTION_KEY)
                .toCompletableFuture().join();
        if (subscription != null) {
            log.info("Subscription {} exists", PRODUCT_PUBLISHED_SUBSCRIPTION_KEY);
            return subscription;
        }
        SubscriptionDraft draft = SubscriptionDraftBuilder.of(pubSubDestination)
                .key(PRODUCT_PUBLISHED_SUBSCRIPTION_KEY)
                .messages(List.of(MessageSubscription.of("product", List.of(ProductPublishedMessage.MESSAGE_TYPE))))
                .build();

        log.info("Start create subscription {}", PRODUCT_PUBLISHED_SUBSCRIPTION_KEY);
        return subscriptionRepository.createSubscription(draft)
                .toCompletableFuture().join();
    }
}
