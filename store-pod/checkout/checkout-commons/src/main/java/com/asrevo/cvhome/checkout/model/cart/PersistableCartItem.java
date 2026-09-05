package com.asrevo.cvhome.checkout.model.cart;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * One line as the storefront sends it: the sku (historically named {@code product}) and an absolute quantity. A
 * quantity of zero on an update removes the line.
 */
@Getter
@Setter
public class PersistableCartItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String product;

    @Min(0)
    private int quantity;

    private String promoCode;
}
