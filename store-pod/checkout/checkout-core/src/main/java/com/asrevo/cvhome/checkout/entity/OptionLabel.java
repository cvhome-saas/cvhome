package com.asrevo.cvhome.checkout.entity;

import java.io.Serializable;

/** One label a line renders for a combination variant: "Color" and "Red". */
public record OptionLabel(String option, String value) implements Serializable {
}
