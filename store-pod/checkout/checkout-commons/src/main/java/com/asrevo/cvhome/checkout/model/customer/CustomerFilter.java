package com.asrevo.cvhome.checkout.model.customer;

/**
 * The console's customer list filters. Every field optional.
 */
public record CustomerFilter(String name, String firstName, String lastName, String email, String country) {

    public static CustomerFilter none() {
        return new CustomerFilter(null, null, null, null, null);
    }
}
