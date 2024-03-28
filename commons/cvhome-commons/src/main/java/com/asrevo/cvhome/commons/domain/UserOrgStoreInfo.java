package com.asrevo.cvhome.commons.domain;

import java.util.List;

public record UserOrgStoreInfo(IdentityId org, String store, List<Roles> roles) {
}
