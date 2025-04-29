package com.asrevo.cvhome.commons.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
public abstract class BaseEntity<E extends AbstractAggregateRoot<E>, T extends Identifier>
        extends AbstractAggregateRoot<E> implements Persistable<T> {
    @Id protected T id;
    @Version private Integer version;

    protected abstract T generateId();

    @Override
    public boolean isNew() {
        return version == null;
    }
}
