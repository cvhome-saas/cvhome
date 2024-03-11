package com.asrevo.cvhome.landing.views;

import com.asrevo.cvhome.landing.service.ProductService;
import com.asrevo.cvhome.landing.service.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.thymeleaf.context.LazyContextVariable;

@Controller
public class LandingView {

  @Autowired
  private ProductService productService;

  @GetMapping(value = {"", "/"})
  public Rendering index() {
    Rendering.Builder<?> builder = Rendering.view("store-15/index.html");


    builder.modelAttribute("landingProducts", new LazyContextVariable<>() {
      @Override
      protected Object loadValue() {
        System.out.println("get landingProducts");
        return productService.findAllProducts(10);
      }
    });

    builder.modelAttribute("product", new LazyContextVariable<Product>() {
      @Override
      protected Product loadValue() {
        System.out.println("get product");
        return productService.getProductById("10");
      }
    });

    return builder.build();
  }


  @GetMapping(value = {"/index.html"})
  public String redirectToIndex() {
    return "redirect:/";
  }

/*
    public String index(WebRequest request) {
        String ifNotMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (request.checkNotModified("wwwwwwwa")) {
            return null;
        }
*/

}
