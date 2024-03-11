package com.asrevo.cvhome.landing.service.impl;

import com.asrevo.cvhome.landing.service.ProductService;
import com.asrevo.cvhome.landing.service.product.Product;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Service
public class ProductServiceImpl implements ProductService {
  @Override
  public Product getProductById(String productId) {
    return new Product(productId, "p1", "p2");
  }

  @Override
  public Iterable<Product> findAllProducts(int limit) {
    return IntStream.range(0, 10).boxed().map(it ->
      new Product(it.toString(), "p" + it, "ppp" + it)).toList();
  }
}
