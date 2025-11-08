package com.asrevo.cvhome.commons.domain;

public record Domain(String domain) {
	// @TODO should validate domain

	public boolean equals(String other) {
		return this.domain.equals(other);
	}
}
