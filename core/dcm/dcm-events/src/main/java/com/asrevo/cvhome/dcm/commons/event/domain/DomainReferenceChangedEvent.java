package com.asrevo.cvhome.dcm.commons.event.domain;

import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainId;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.commons.domain.ReferenceType;

import java.util.Map;

public record DomainReferenceChangedEvent(EventId eventId, DomainId domainId, Domain domain,
                                          Map<String, String> data) implements DomainEvent {
    private static final String OLD_REFERENCE_REFERENCE_KEY = "oldReferenceKey";
    private static final String OLD_REFERENCE_REFERENCE_TYPE_KEY = "oldReferenceTypeKey";
    private static final String NEW_REFERENCE_REFERENCE_KEY = "newReferenceKey";
    private static final String NEW_REFERENCE_REFERENCE_TYPE_KEY = "newReferenceTypeKey";


    public static DomainReferenceChangedEvent from(DomainId domainId, Domain domain, Reference oldReference, Reference newReference) {
        Map<String, String> data = Map.of(
                OLD_REFERENCE_REFERENCE_KEY, oldReference == null ? "" : oldReference.reference(), OLD_REFERENCE_REFERENCE_TYPE_KEY, oldReference == null ? "" : oldReference.referenceType().name(),
                NEW_REFERENCE_REFERENCE_KEY, newReference == null ? "" : newReference.reference(), NEW_REFERENCE_REFERENCE_TYPE_KEY, newReference == null ? "" : newReference.referenceType().name()
        );
        return new DomainReferenceChangedEvent(EventId.newId(), domainId, domain, data);
    }

    public Reference oldReference() {
        if (this.data.containsKey(OLD_REFERENCE_REFERENCE_KEY)
                && !this.data.get(OLD_REFERENCE_REFERENCE_KEY).isEmpty()
                && this.data.containsKey(OLD_REFERENCE_REFERENCE_TYPE_KEY)
                && !this.data.get(OLD_REFERENCE_REFERENCE_TYPE_KEY).isEmpty()) {
            return new Reference(this.data.get(OLD_REFERENCE_REFERENCE_KEY), ReferenceType.valueOf(this.data.get(OLD_REFERENCE_REFERENCE_TYPE_KEY)));
        } else {
            return null;
        }
    }

    public Reference newReference() {
        if (this.data.containsKey(NEW_REFERENCE_REFERENCE_KEY) &&
                !this.data.get(NEW_REFERENCE_REFERENCE_KEY).isEmpty() &&
                this.data.containsKey(NEW_REFERENCE_REFERENCE_TYPE_KEY) &&
                !this.data.get(NEW_REFERENCE_REFERENCE_TYPE_KEY).isEmpty()) {
            return new Reference(this.data.get(NEW_REFERENCE_REFERENCE_KEY), ReferenceType.valueOf(this.data.get(NEW_REFERENCE_REFERENCE_TYPE_KEY)));
        } else {
            return null;
        }
    }

    @Override
    public String eventType() {
        return "DomainReferenceChangedEvent";
    }
}
