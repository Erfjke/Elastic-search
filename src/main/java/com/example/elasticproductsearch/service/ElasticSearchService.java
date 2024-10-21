package com.example.elasticproductsearch.service;

import com.example.elasticproductsearch.config.ElasticSearchConfig;
import com.example.elasticproductsearch.model.Product;
import com.example.elasticproductsearch.model.SKU;
import com.example.elasticproductsearch.repository.ProductRepository;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchService {

    private final RestHighLevelClient client;
    private final ProductRepository productRepository;
    private final ElasticSearchConfig elasticSearchConfig;

    @Autowired
    public ElasticSearchService(RestHighLevelClient client, ProductRepository productRepository, ElasticSearchConfig elasticSearchConfig) {
        this.client = client;
        this.productRepository = productRepository;
        this.elasticSearchConfig = elasticSearchConfig;
    }


    public void uploadDataToElastic(String brand) throws IOException {
        // Удаление существующего индекса
        if (client.indices().exists(new GetIndexRequest("products_sku"), RequestOptions.DEFAULT)) {
            client.indices().delete(new DeleteIndexRequest("products_sku"), RequestOptions.DEFAULT);
        }
        List<Product> products;

        if (brand != null) {
            products = productRepository.findByBrand(brand);
        } else {
            products = productRepository.findAll();
        }

        for (Product product : products) {
            IndexRequest request = new IndexRequest("products_sku");
            request.id(product.getId().toString());
            request.source(convertProductToJson(product), XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        }
    }
    private Map<String, Object> convertProductToJson(Product product) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("model", product.getModel());
        jsonMap.put("brand", product.getBrand());
        jsonMap.put("batteryCapacity", product.getBatteryCapacity());
        jsonMap.put("startDate", product.getReleaseDate().toString());


        List<Map<String, Object>> skuList = new ArrayList<>();
        for (SKU sku : product.getSkus()) {
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("color", sku.getColor());
            skuMap.put("availability", sku.getAvailability());
            skuList.add(skuMap);
        }
        jsonMap.put("skus", skuList);

        return jsonMap;
    }



    public List<Map<String, Object>> searchProducts(String searchTerm) throws IOException {
        SearchRequest searchRequest = new SearchRequest("products_sku");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(searchTerm, "model", "brand", "skus.color"));


        if (elasticSearchConfig.isFilterEnabled()) {

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.termQuery("skus.color", "red"));
            boolQuery.must(QueryBuilders.termQuery("skus.availability", true));
            searchSourceBuilder.postFilter(boolQuery);
        }

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            results.add(hit.getSourceAsMap());
        }
        return results;
    }
}
