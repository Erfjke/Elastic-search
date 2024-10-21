package com.example.elasticproductsearch.repository;

import com.example.elasticproductsearch.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByBrand(String brand);
}
