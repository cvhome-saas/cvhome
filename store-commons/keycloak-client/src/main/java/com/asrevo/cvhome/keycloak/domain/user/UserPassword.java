package com.asrevo.cvhome.keycloak.domain.user;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Object containing password information
 * for change password request
 *
 * @author carlsamson
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPassword implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String password = null;
    private String changePassword = null;
}
