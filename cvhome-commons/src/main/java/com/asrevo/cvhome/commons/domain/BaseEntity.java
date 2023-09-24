package com.asrevo.cvhome.commons.domain;

import com.asrevo.cvhome.commons.event.Event;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class BaseEntity<E extends AbstractAggregateRoot<E>, T extends Identifier> extends AbstractAggregateRoot<E> implements Persistable<T> {
    @Id
    protected T id;
    @Transient
    public boolean isNew;

    protected abstract T generateId();

    protected void setNew() {
        this.id = generateId();
        this.isNew = true;
    }

    @Override
    protected Collection<Object> domainEvents() {
        return super.domainEvents().stream().peek(it -> {
            if (it instanceof Event) {
                Event<T> event = (Event<T>) it;
                if (id != null) {
                    event.setId(id);
                }
            }
        }).collect(Collectors.toList());
    }

}
