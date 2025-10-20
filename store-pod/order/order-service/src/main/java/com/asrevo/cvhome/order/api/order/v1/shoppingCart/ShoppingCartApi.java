package com.asrevo.cvhome.order.api.order.v1.shoppingCart;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.model.shoppingcart.PersistableShoppingCartItem;
import com.asrevo.cvhome.order.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.order.service.facade.cart.ShoppingCartFacade;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Shopping cart resource", description = "Add, remove and retrieve shopping carts")
@Slf4j
public class ShoppingCartApi {

    /*

        @Autowired
        private com.salesmanager.shop.store.controller.shoppingCart.facade.v1.ShoppingCartFacade shoppingCartFacadev1;

        @Autowired
        private CustomerService customerService;

        @Autowired
        private CustomerFacade customerFacadev1;

        @Autowired
        private com.salesmanager.shop.store.controller.customer.facade.CustomerFacade customerFacade;
    */
    private final ShoppingCartFacade shoppingCartFacade;

    public ShoppingCartApi(ShoppingCartFacade shoppingCartFacade) {
        this.shoppingCartFacade = shoppingCartFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/cart")
    @Operation(
            method = "POST",
            description =
                    "Add product to shopping cart when no cart exists, this will create a new cart"
                            + " id",
            summary =
                    "No customer ID in scope. Add to cart for non authenticated users, as simple as"
                            + " {\"product\":1232,\"quantity\":1}")
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    public @ResponseBody ReadableShoppingCart addToCart(
            @Valid @RequestBody PersistableShoppingCartItem shoppingCartItem,
            @Parameter(hidden = true) StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {
        return shoppingCartFacade.addToCart(shoppingCartItem, merchantStore, language);
    }

    @PutMapping(value = "/cart/{code}")
    @Operation(
            method = "PUT",
            description = "Add to an existing shopping cart or modify an item quantity",
            summary =
                    "No customer ID in scope. Modify cart for non authenticated users, as simple as"
                        + " {\"product\":1232,\"quantity\":0} for instance will remove item 1234"
                        + " from cart")
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    public ResponseEntity<ReadableShoppingCart> modifyCart(
            @PathVariable String code,
            @Valid @RequestBody PersistableShoppingCartItem shoppingCartItem,
            @Parameter(hidden = true) StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        try {
            ReadableShoppingCart cart =
                    shoppingCartFacade.modifyCart(code, shoppingCartItem, merchantStore, language);

            if (cart == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(cart, HttpStatus.CREATED);

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e;
            } else {
                throw new ServiceRuntimeException(e);
            }
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/cart/{code}", method = RequestMethod.GET)
    @Operation(method = "GET", description = "Get a chopping cart by code")
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    public @ResponseBody ReadableShoppingCart getByCode(
            @PathVariable String code,
            @Parameter(hidden = true) StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language,
            HttpServletResponse response) {

        try {

            ReadableShoppingCart cart = shoppingCartFacade.getByCode(code, merchantStore, language);

            if (cart == null) {
                response.sendError(404, "No ShoppingCart found for customer code : " + code);
                return null;
            }

            return cart;

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e;
            } else {
                throw new ServiceRuntimeException(e);
            }
        }
    }

    @DeleteMapping(
            value = "/cart/{code}/product/{sku}",
            produces = {APPLICATION_JSON_VALUE})
    @Operation(
            method = "DELETE",
            description = "Remove a product from a specific cart",
            summary =
                    "If body set to true returns remaining cart in body, empty cart gives empty"
                            + " body. If body set to false no body ")
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE)),
        @Parameter(
                name = "body",
                schema = @Schema(name = "body", type = "boolean", defaultValue = "false"))
    })
    @ConditionalOnApiStatus
    public ResponseEntity<ReadableShoppingCart> deleteCartItem(
            @PathVariable("code") String cartCode,
            @PathVariable("sku") String sku,
            @Parameter(hidden = true) StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language,
            @RequestParam(defaultValue = "false") boolean body)
            throws Exception {

        ReadableShoppingCart updatedCart =
                shoppingCartFacade.removeShoppingCartItem(
                        cartCode, sku, merchantStore, language, body);
        if (body) {
            return new ResponseEntity<>(updatedCart, HttpStatus.OK);
        }
        return new ResponseEntity<>(updatedCart, HttpStatus.NO_CONTENT);
    }
}
