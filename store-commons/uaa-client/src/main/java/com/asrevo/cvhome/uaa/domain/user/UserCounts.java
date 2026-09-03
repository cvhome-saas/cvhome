package com.asrevo.cvhome.uaa.domain.user;

import java.io.Serializable;

/** How many accounts are in each state. A snapshot, not a subscription: it is true when the call returns. */
public record UserCounts(long total, long active, long pending, long locked, long disabled) implements Serializable {
}
