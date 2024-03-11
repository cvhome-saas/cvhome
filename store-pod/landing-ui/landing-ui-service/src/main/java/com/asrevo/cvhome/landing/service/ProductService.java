package com.asrevo.cvhome.landing.service;

import com.asrevo.cvhome.landing.service.product.Product;


public interface ProductService {
    Product getProductById(String productId);

    Iterable<Product> findAllProducts(int limit);
}
