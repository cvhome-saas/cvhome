package com.asrevo.cvhome.content.model.policy;

import java.time.LocalDate;
import java.util.Set;

import com.asrevo.cvhome.content.model.ContentView;

public record PolicyView(ContentView content, PolicyType policyType, String policyVersion,
                         LocalDate effectiveDate, boolean acceptanceRequired, String jurisdiction,
                         boolean active, Set<PolicyDisplayLocation> displayLocations) {
}
