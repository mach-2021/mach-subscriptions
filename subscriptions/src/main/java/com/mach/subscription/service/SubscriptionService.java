package com.mach.subscription.service;

import com.algolia.search.models.indexing.UpdateObjectResponse;
import com.mach.core.model.ProductSearchModel;
import com.mach.subscription.repository.AlgoliaProductRepository;
import com.mach.subscription.repository.SubscriptionRepository;
import io.sphere.sdk.categories.Category;
import io.sphere.sdk.categories.CategoryTree;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.products.Image;
import io.sphere.sdk.products.ProductProjection;
import io.sphere.sdk.products.ProductVariant;
import io.sphere.sdk.products.attributes.Attribute;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {

    private static final String PRODUCT_PUBLISHED_SUBSCRIPTION_KEY = "sub-product-published";
    private final SubscriptionRepository subscriptionRepository;
    private final GcpProjectIdProvider gcpProjectIdProvider;
    private final AlgoliaProductRepository productRepository;
    private final Locale defaultLocale;

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

    public void saveProductToAlgolia(ProductProjection productProjection) {
        ProductVariant productVariant = productProjection.getMasterVariant();
        if (productVariant == null) {
            log.error("With out variant: {}", productProjection.getId());
            return;
        }
        final String image = productVariant.getImages()
                .stream()
                .findFirst()
                .map(Image::getUrl).orElse(null);
        final BigDecimal value = Optional.ofNullable(productVariant.getAttribute("general-trending"))
                .map(Attribute::getValueAsDouble)
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
        String categoryId = productProjection.getCategories()
                .stream().findFirst().map(Reference::getId)
                .orElse(null);
        final ProductSearchModel searchModel = ProductSearchModel.builder()
                .objectID(productProjection.getId())
                .name(productProjection.getName().get(defaultLocale))
                .description(Optional.ofNullable(productProjection.getDescription())
                        .map(desc -> desc.get(defaultLocale))
                        .orElse(""))
                .picture(image)
                .sku(productVariant.getSku())
                .trending(value)
                .build();
        if (categoryId != null) {
            final List<Category> categories = subscriptionRepository.findAllCategories();
            Category category = categories.stream()
                    .filter(cat -> cat.getId().equals(categoryId))
                    .findFirst()
                    .get();
            final CategoryTree tree = CategoryTree.of(categories);
            Category rootCategory = tree.getRootAncestor(category);
            String categoryName = rootCategory.getName().get(defaultLocale);
            searchModel.setCategory(categoryName);
            searchModel.setProductCategory(category.getName().get(defaultLocale));
        }

        log.info("Model search: {}", searchModel);
        final UpdateObjectResponse updateObjectResponse = productRepository.updateAndCreat(searchModel);
        log.info("Updated object id: {}", updateObjectResponse.getObjectID());
    }
}
