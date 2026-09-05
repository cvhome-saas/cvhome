package com.asrevo.cvhome.checkout.api.v1.cart;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.config.CurrentShopper;
import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.errors.CartAlreadyConvertedException;
import com.asrevo.cvhome.checkout.errors.CartNotFoundException;
import com.asrevo.cvhome.checkout.errors.CartQuantityOutOfRangeException;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.model.cart.PersistableCartItem;
import com.asrevo.cvhome.checkout.model.cart.ReadableCart;
import com.asrevo.cvhome.checkout.services.cart.CartService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The shopper's cart. Public: the cart code the browser holds is the whole credential, and {@code store} is the
 * tenant boundary.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Cart")
@RequiredArgsConstructor
public class CartApi {

    private final CartService cartService;

    @PostMapping("/cart")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableCart create(@Valid @RequestBody PersistableCartItem item, StoreMerchantId merchantStore,
                               LanguageCode language, @CurrentShopper ShopperId shopper)
            throws ProductNotPurchasableException, CartQuantityOutOfRangeException {
        return cartService.create(merchantStore, language, item, shopper);
    }

    @PutMapping("/cart/{code}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableCart upsert(@PathVariable String code, @Valid @RequestBody PersistableCartItem item,
                               StoreMerchantId merchantStore, LanguageCode language)
            throws CartNotFoundException, CartAlreadyConvertedException, ProductNotPurchasableException,
            CartQuantityOutOfRangeException {
        return cartService.upsert(merchantStore, language, CartCode.of(code), item);
    }

    @GetMapping("/cart/{code}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableCart get(@PathVariable String code, StoreMerchantId merchantStore, LanguageCode language)
            throws CartNotFoundException {
        return cartService.get(merchantStore, language, CartCode.of(code));
    }

    /**
     * Answers 204 by default; {@code ?body=true} returns the remaining cart instead, which is what the storefront's
     * drawer asks for.
     */
    @DeleteMapping("/cart/{code}/product/{sku}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ResponseEntity<ReadableCart> removeLine(@PathVariable String code, @PathVariable String sku,
                                                   @RequestParam(defaultValue = "false") boolean body,
                                                   StoreMerchantId merchantStore, LanguageCode language)
            throws CartNotFoundException, CartAlreadyConvertedException {
        ReadableCart cart = cartService.removeLine(merchantStore, language, CartCode.of(code), sku);
        return body ? ResponseEntity.ok(cart) : ResponseEntity.noContent().build();
    }
}
