package com.asrevo.cvhome.landing.views;

import com.asrevo.cvhome.landing.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;

@Controller
public class LandingView {

    @Autowired
    private ProductService productService;

    @GetMapping(value = {"", "/"})
    public Rendering index() {
        Rendering.Builder<?> builder = Rendering.view("store-15/index.html");

/*
        builder.modelAttribute("landingProducts", new ReactiveDataDriverContextVariable(
                Flux.defer(() -> Flux.fromIterable(productService.findAllProducts(10)))));


        builder.modelAttribute("product", new ReactiveDataDriverContextVariable(
                Mono.defer(() -> Mono.just(productService.getProductById("10")))));
*/


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
