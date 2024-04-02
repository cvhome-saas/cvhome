package com.asrevo.cvhome.store.controller.v1.shoppingCart;

import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.shoppingcart.PersistableShoppingCartItem;
import com.asrevo.cvhome.store.core.model.shoppingcart.ReadableShoppingCart;
import com.asrevo.cvhome.store.service.facade.shoppingCart.ShoppingCartFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Shopping cart resource", description = "Add, remove and retrieve shopping carts")
public class ShoppingCartApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingCartApi.class);
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
    @Autowired
    private ShoppingCartFacade shoppingCartFacade;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/cart")
    @Operation(method = "POST", description = "Add product to shopping cart when no cart exists, this will create a new cart id", summary = "No customer ID in scope. Add to cart for non authenticated users, as simple as {\"product\":1232,\"quantity\":1}")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableShoppingCart addToCart(
            @Valid @RequestBody PersistableShoppingCartItem shoppingCartItem,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        return shoppingCartFacade.addToCart(shoppingCartItem, merchantStore, language);
    }

    @PutMapping(value = "/cart/{code}")
    @Operation(method = "PUT", description = "Add to an existing shopping cart or modify an item quantity", summary = "No customer ID in scope. Modify cart for non authenticated users, as simple as {\"product\":1232,\"quantity\":0} for instance will remove item 1234 from cart")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ResponseEntity<ReadableShoppingCart> modifyCart(
            @PathVariable String code,
            @Valid @RequestBody PersistableShoppingCartItem shoppingCartItem,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response) {

        try {
            ReadableShoppingCart cart = shoppingCartFacade.modifyCart(code, shoppingCartItem, merchantStore, language);

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


    @PostMapping(value = "/cart/{code}/promo/{promo}")
    @Operation(method = "POST", description = "Add promo / coupon to an existing cart")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ResponseEntity<ReadableShoppingCart> modifyCart(
            @PathVariable String code,//shopping cart code
            @PathVariable String promo,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response) {

        try {
            ReadableShoppingCart cart = shoppingCartFacade.modifyCart(code, promo, merchantStore, language);

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


    @PostMapping(value = "/cart/{code}/multi", consumes = {"application/json"}, produces = {"application/json"})
    @Operation(method = "POST", description = "Add to an existing shopping cart or modify an item quantity", summary = "No customer ID in scope. Modify cart for non authenticated users, as simple as {\"product\":1232,\"quantity\":0} for instance will remove item 1234 from cart")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ResponseEntity<ReadableShoppingCart> modifyCart(
            @PathVariable String code,
            @Valid @RequestBody PersistableShoppingCartItem[] shoppingCartItems,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        try {
            ReadableShoppingCart cart = shoppingCartFacade.modifyCartMulti(code, Arrays.asList(shoppingCartItems),
                    merchantStore, language);

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
    @Operation(method = "GET", description = "Get a chopping cart by code", summary = "")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableShoppingCart getByCode(@PathVariable String code,
                                                        @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language, HttpServletResponse response) {

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

    @Deprecated
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/customers/{id}/cart", method = RequestMethod.POST)
    @Operation(method = "POST", description = "Add product to a specific customer shopping cart", summary = "")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableShoppingCart addToCart(@PathVariable Long id,
                                                        @Valid @RequestBody PersistableShoppingCartItem shoppingCartItem, @Parameter(hidden = true) MerchantStore merchantStore,
                                                        @Parameter(hidden = true) Language language, HttpServletResponse response) {

        throw new OperationNotAllowedException("API is no more supported. Authenticate customer first then get customer cart");

    }

    @DeleteMapping(value = "/cart/{code}/product/{sku}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "DELETE", description = "Remove a product from a specific cart", summary = "If body set to true returns remaining cart in body, empty cart gives empty body. If body set to false no body ")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)),
            @Parameter(name = "body", schema = @Schema(name = "body", type = "boolean", defaultValue = "false"))
    })
    public ResponseEntity<ReadableShoppingCart> deleteCartItem(@PathVariable("code") String cartCode,
                                                               @PathVariable("sku") String sku,
                                                               @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language,
                                                               @RequestParam(defaultValue = "false") boolean body) throws Exception {

        ReadableShoppingCart updatedCart = shoppingCartFacade.removeShoppingCartItem(cartCode, sku, merchantStore,
                language, body);
        if (body) {
            return new ResponseEntity<>(updatedCart, HttpStatus.OK);
        }
        return new ResponseEntity<>(updatedCart, HttpStatus.NO_CONTENT);
    }
}
/*
	@Deprecated
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/auth/customer/{id}/cart", method = RequestMethod.GET)
	@Operation(method = "GET", description = "Get a shopping cart by customer id. Customer must be authenticated", summary = "")
	    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
	public @ResponseBody ReadableShoppingCart getByCustomer(@PathVariable Long id, // customer
																					// id
			@RequestParam Optional<String> cart, // cart code
			@Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language, HttpServletRequest request,
			HttpServletResponse response) {

		Principal principal = request.getUserPrincipal();

		// lookup customer
		Customer customer = customerService.getById(id);

		if (customer == null) {
			throw new ResourceNotFoundException("No Customer found for id [" + id + "]");
		}

		customerFacadev1.authorize(customer, principal);

		ReadableShoppingCart readableCart = shoppingCartFacadev1.get(cart, id, merchantStore, language);

		if (readableCart == null) {
			throw new ResourceNotFoundException("No cart found for customerid [" + id + "]");
		}

		return readableCart;

	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/auth/customer/cart", method = RequestMethod.GET)
	@Operation(method = "GET", description = "Get a shopping cart by authenticated customer", summary = "")
	    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
	public @ResponseBody ReadableShoppingCart getByCustomer(
			@RequestParam Optional<String> cart, // cart code
			@Parameter(hidden = true) MerchantStore merchantStore, 
			@Parameter(hidden = true) Language language, 
			HttpServletRequest request,
			HttpServletResponse response) {

		Principal principal = request.getUserPrincipal();
		Customer customer = null;
		try {
			customer = customerFacade.getCustomerByUserName(principal.getName(), merchantStore);
		} catch (Exception e) {
			throw new ServiceRuntimeException("Exception while getting customer [ " + principal.getName() + "]");
		}
		
		if (customer == null) {
			throw new ResourceNotFoundException("No Customer found for principal[" + principal.getName() + "]");
		}
		
		customerFacadev1.authorize(customer, principal);
		ReadableShoppingCart readableCart = shoppingCartFacadev1.get(cart, customer.getId(), merchantStore, language);

		if (readableCart == null) {
			throw new ResourceNotFoundException("No cart found for customer [" + principal.getName() + "]");
		}

		return readableCart;

	}

*/
