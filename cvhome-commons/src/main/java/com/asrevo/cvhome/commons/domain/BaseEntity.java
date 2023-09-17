package com.asrevo.cvhome.commons.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
public abstract class BaseEntity<E extends AbstractAggregateRoot<E>, T extends Identifier> extends AbstractAggregateRoot<E> implements Persistable<T> {
    @Id
    protected T id;
    @Transient
    public boolean isNew;

    protected abstract T generateId();

    public void setNew() {
        this.id = generateId();
        this.isNew = true;
    }

}
