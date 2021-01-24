package com.mach.subscription.repository;

import com.mach.commercetools.repository.BaseRepository;
import io.sphere.sdk.categories.Category;
import io.sphere.sdk.categories.expansion.CategoryExpansionModel;
import io.sphere.sdk.categories.queries.CategoryByIdGet;
import io.sphere.sdk.categories.queries.CategoryQuery;
import io.sphere.sdk.subscriptions.Subscription;
import io.sphere.sdk.subscriptions.SubscriptionDraft;
import io.sphere.sdk.subscriptions.commands.SubscriptionCreateCommand;
import io.sphere.sdk.subscriptions.queries.SubscriptionByKeyGet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletionStage;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepository {
    private final BaseRepository ctBaseRepository;

    public CompletionStage<Subscription> createSubscription(SubscriptionDraft subscriptionDraft) {
        return ctBaseRepository.executeWithThrowing(SubscriptionCreateCommand.of(subscriptionDraft));
    }

    public CompletionStage<Subscription> findSubscription(String key) {
        return ctBaseRepository.executeWithThrowing(SubscriptionByKeyGet.of(key));
    }

    public Category findCategoryById(String id) {
        return ctBaseRepository.executeBlocking(CategoryByIdGet.of(id)
                .plusExpansionPaths(CategoryExpansionModel::parent)
                .plusExpansionPaths(CategoryExpansionModel::ancestors));
    }

    public List<Category> findAllCategories() {
        return ctBaseRepository.executeBlocking(CategoryQuery.of().withLimit(500)
                .plusExpansionPaths(CategoryExpansionModel::parent)
                .plusExpansionPaths(CategoryExpansionModel::ancestors))
                .getResults();
    }
}
