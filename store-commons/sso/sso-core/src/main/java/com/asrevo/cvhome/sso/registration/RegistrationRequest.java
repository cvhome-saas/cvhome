package com.asrevo.cvhome.sso.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * What someone signing themselves up supplies.
 *
 * <p>
 * Deliberately no realm: the realm is the one the request arrived in, resolved at the edge from the store's own
 * host. Letting the body name it would let one form create accounts in any store the deployment serves.
 * </p>
 */
public record RegistrationRequest(@NotBlank @Size(max = 190) String username,
                                  @NotBlank @Email @Size(max = 254) String email,
                                  @Size(max = 50) String firstName,
                                  @Size(max = 50) String lastName,
                                  @NotBlank String password) {
}
