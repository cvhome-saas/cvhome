package com.asrevo.cvhome.checkout.api.order.v1.shoppingCart;

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

import com.asrevo.cvhome.checkout.errors.CartQuantityOutOfRangeException;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.errors.ShoppingCartNotFoundException;
import com.asrevo.cvhome.checkout.model.shoppingcart.PersistableShoppingCartItem;
import com.asrevo.cvhome.checkout.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.checkout.service.facade.cart.ShoppingCartFacade;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Shopping cart resource", description = "Add, remove and retrieve shopping carts")
@Slf4j
public class ShoppingCartApi {

    private final ShoppingCartFacade shoppingCartFacade;

    public ShoppingCartApi(ShoppingCartFacade shoppingCartFacade) {
        this.shoppingCartFacade = shoppingCartFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/cart")
    @Operation(method = "POST",
            description = "Add product to shopping cart when no cart exists, this will create a new cart id",
            summary = "No customer ID in scope. Add to cart for non authenticated users")
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    public ReadableShoppingCart addToCart(@Valid @RequestBody PersistableShoppingCartItem shoppingCartItem,
                                          StoreMerchantId merchantStore, LanguageCode language)
            throws ProductNotPurchasableException, CartQuantityOutOfRangeException {
        return shoppingCartFacade.addToCart(shoppingCartItem, merchantStore, language);
    }

    @PutMapping(value = "/cart/{code}")
    @Operation(method = "PUT", description = "Add to an existing shopping cart or modify an item quantity",
            summary = "No customer ID in scope. Modify cart for non authenticated users")
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    public ResponseEntity<ReadableShoppingCart> modifyCart(@PathVariable String code,
                                                           @Valid @RequestBody PersistableShoppingCartItem shoppingCartItem,
                                                           StoreMerchantId merchantStore,
                                                           LanguageCode language)
            throws ShoppingCartNotFoundException, ProductNotPurchasableException, CartQuantityOutOfRangeException {

        ReadableShoppingCart cart = shoppingCartFacade.modifyCart(code, shoppingCartItem, merchantStore, language);

        if (cart == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(cart, HttpStatus.CREATED);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/cart/{code}")
    @Operation(method = "GET", description = "Get a chopping cart by code")
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    public ReadableShoppingCart getByCode(@PathVariable String code, StoreMerchantId merchantStore,
                                          LanguageCode language)
            throws ShoppingCartNotFoundException {

        ReadableShoppingCart cart = shoppingCartFacade.getByCode(code, merchantStore, language);

        if (cart == null) {
            // Previously a raw response.sendError(404), which bypassed the advice and returned an HTML error page
            // instead of a problem document.
            throw ShoppingCartNotFoundException.byCode(code);
        }

        return cart;
    }

    @DeleteMapping(value = "/cart/{code}/product/{sku}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "DELETE", description = "Remove a product from a specific cart",
            summary = "If body set to true returns remaining cart in body, empty cart gives empty body. If body set to false no body ")
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Parameter(name = "body", schema = @Schema(name = "body", type = "boolean", defaultValue = "false"))

    public ResponseEntity<ReadableShoppingCart> deleteCartItem(@PathVariable("code") String cartCode,
                                                               @PathVariable("sku") String sku, StoreMerchantId merchantStore,
                                                               LanguageCode language,
                                                               @RequestParam(defaultValue = "false") boolean body)
            throws ShoppingCartNotFoundException {

        ReadableShoppingCart updatedCart = shoppingCartFacade.removeShoppingCartItem(cartCode, sku, merchantStore,
                language, body);
        if (body) {
            return new ResponseEntity<>(updatedCart, HttpStatus.OK);
        }
        return new ResponseEntity<>(updatedCart, HttpStatus.NO_CONTENT);
    }

}
