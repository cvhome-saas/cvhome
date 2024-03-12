package com.asrevo.cvhome.product.controller;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("api/v1/public/products")
@AllArgsConstructor
@Slf4j
public class ProductsController {
  private final static Currency egp = Currency.getInstance("EGP");

  @GetMapping("find-all")
  public List<Product> findAll(@RequestParam StoreId storeId) {
    return List.of(
      new Product(ProductId.newId(), storeId, "p1", new ProductAmount(25), new ProductPrice(30.0, egp))
    );
  }
}

record Product(ProductId productId, StoreId storeId, String name, ProductAmount amount, ProductPrice productPrice) {

}

record ProductId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
  public ProductId(String id) {
    this(new ObjectId(id));
  }

  public static ProductId newId() {
    return new ProductId(new ObjectId());
  }

  @JsonSerialize(using = ToStringSerializer.class)
  @Override
  public ObjectId getId() {
    return this.id;
  }
}

record ProductAmount(Integer amount) {

}

record ProductPrice(Double price, Currency currency) {

}
