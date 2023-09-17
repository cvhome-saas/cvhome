package com.asrevo.cvhome.commons.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
public abstract class BaseEntity<T extends Identifier> implements Persistable<T> {
    @Transient
    public boolean isNew;
    @Id
    protected T id;

    protected abstract T generateId();

    public void setNew() {
        this.id = generateId();
        this.isNew = true;
    }

}
