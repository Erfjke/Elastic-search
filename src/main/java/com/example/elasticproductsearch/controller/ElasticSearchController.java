package com.example.elasticproductsearch.controller;

import com.example.elasticproductsearch.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ElasticSearchController {

    private final ElasticSearchService elasticSearchService;

    @Autowired
    public ElasticSearchController(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    @GetMapping("/upload-to-elastic")
    public String uploadDataToElastic(@RequestParam(required = false) String brand) {
        try {
            elasticSearchService.uploadDataToElastic(brand);
            return "Данные успешно загружены в Elasticsearch!";
        } catch (IOException e) {
            return "Ошибка при загрузке данных: " + e.getMessage();
        }
    }
    @GetMapping("/search")
    public List<Map<String, Object>> searchProducts(@RequestParam String query) {
        try {
            return elasticSearchService.searchProducts(query);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка поиска: " + e.getMessage());
        }
    }
}