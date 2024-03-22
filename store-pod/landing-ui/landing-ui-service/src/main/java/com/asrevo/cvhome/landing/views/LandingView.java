package com.asrevo.cvhome.landing.views;

import com.asrevo.cvhome.commons.utils.CustomPageImpl;
import com.asrevo.cvhome.landing.service.ProductService;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
@RequestMapping(params = "storeId")
public class LandingView {

    @Autowired
    private ProductService productService;

    @GetMapping(value = {"", "/"})
    public Rendering index(@RequestParam("storeId") StoreId storeId) {
        Rendering.Builder<?> builder = Rendering.view(storeId.getId().toString() + "/index.html");
        Mono<CustomPageImpl<ProductDto>> all = productService.findAll(storeId, PageRequest.of(0, 10));
        ReactiveDataDriverContextVariable value = new ReactiveDataDriverContextVariable(all.map(it -> it.getContent()).flatMapMany(Flux::fromIterable));
        builder.modelAttribute("landingProducts", value);
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
