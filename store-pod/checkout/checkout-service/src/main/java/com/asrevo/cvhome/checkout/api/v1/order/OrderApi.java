package com.asrevo.cvhome.checkout.api.v1.order;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.errors.IllegalOrderTransitionException;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.order.OrderFilter;
import com.asrevo.cvhome.checkout.model.order.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The console's orders: list, detail, status trail, and the one write — moving an order along.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Orders (console)")
@RequiredArgsConstructor
public class OrderApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')";

    private final OrderService orderService;

    @GetMapping("/private/orders")
    @PreAuthorize(MANAGE)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableOrderList list(@ModelAttribute OrderFilter filter, StoreMerchantId merchantStore, LanguageCode language,
                                  Pageable pageable) {
        return orderService.list(merchantStore, language, filter, pageable);
    }

    @GetMapping("/private/orders/{id}")
    @PreAuthorize(MANAGE)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableOrder get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws OrderNotFoundException {
        return orderService.get(merchantStore, language, id);
    }

    @GetMapping("/private/orders/{id}/history")
    @PreAuthorize(MANAGE)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public List<ReadableOrderStatusHistory> history(@PathVariable Long id, StoreMerchantId merchantStore)
            throws OrderNotFoundException {
        return orderService.history(merchantStore, id);
    }

    /**
     * Moves the order to the requested status. An illegal step — shipping a cancelled order, delivering one never
     * confirmed — is a 409 {@code CHECKOUT.ORDER.ILLEGAL_TRANSITION}.
     */
    @PostMapping("/private/orders/{id}/history")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(MANAGE)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableOrderStatusHistory transition(@PathVariable Long id,
                                                 @Valid @RequestBody PersistableOrderStatusHistory change,
                                                 StoreMerchantId merchantStore, Authentication authentication)
            throws OrderNotFoundException, IllegalOrderTransitionException {
        return orderService.transition(merchantStore, id, change, authentication == null ? null
                : authentication.getName());
    }
}
