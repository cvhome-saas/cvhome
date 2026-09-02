package com.asrevo.cvhome.uaa.domain;

/** Where a signing key is in its life: signing, verifying only, or gone from the JWKS. */
public enum SigningKeyStatus {
    ACTIVE, RETIRING, RETIRED
}
