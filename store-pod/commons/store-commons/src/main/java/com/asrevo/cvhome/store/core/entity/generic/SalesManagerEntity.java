package com.asrevo.cvhome.store.core.entity.generic;

import java.io.Serial;
import java.io.Serializable;
import java.text.Collator;
import java.util.Locale;

public abstract class SalesManagerEntity<K extends Serializable & Comparable<K>, E extends SalesManagerEntity<K, ?>>
        implements Serializable, Comparable<E> {

    public static final Collator DEFAULT_STRING_COLLATOR = Collator.getInstance(Locale.FRENCH);

    @Serial
    private static final long serialVersionUID = -3988499137919577054L;

    static {
        DEFAULT_STRING_COLLATOR.setStrength(Collator.PRIMARY);
    }

    public abstract K getId();

    public abstract void setId(K id);


    public boolean isNew() {
        return getId() == null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof SalesManagerEntity<?, ?> entity)) {
            return false;
        }

        if (object == this) {
            return true;
        }

        K id = getId();

        if (id == null) {
            return false;
        }

        return id.equals(entity.getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;

        K id = getId();
        hash = 31 * hash + ((id == null) ? 0 : id.hashCode());

        return hash;
    }

    public int compareTo(E o) {
        if (this == o) {
            return 0;
        }
        return this.getId().compareTo(o.getId());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("entity.");
        builder.append(getClass().getSimpleName());
        builder.append("<");
        builder.append(getId());
        builder.append("-");
        builder.append(super.toString());
        builder.append(">");

        return builder.toString();
    }

}
