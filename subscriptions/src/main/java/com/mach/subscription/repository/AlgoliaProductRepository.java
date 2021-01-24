package com.mach.subscription.repository;

import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.algolia.search.models.indexing.UpdateObjectResponse;
import com.mach.core.model.ProductSearchModel;
import org.springframework.stereotype.Repository;

@Repository
public class AlgoliaProductRepository {

    private static final String PRODUCT_INDEX_NAME = "machaton";

    private final SearchIndex<ProductSearchModel> productIndex;

    public AlgoliaProductRepository(SearchClient searchClient) {
        this.productIndex = searchClient.initIndex(PRODUCT_INDEX_NAME, ProductSearchModel.class);
    }

    public UpdateObjectResponse updateAndCreat(ProductSearchModel categoryModel) {
        return productIndex.partialUpdateObject(categoryModel, true);
    }

    public ProductSearchModel get(String id) {
        return productIndex.getObject(id);
    }
}
