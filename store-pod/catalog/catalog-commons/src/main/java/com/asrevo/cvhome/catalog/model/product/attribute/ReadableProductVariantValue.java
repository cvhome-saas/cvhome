package com.asrevo.cvhome.catalog.model.product.attribute;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductVariantValue implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private String code;

    private int order;

    private String description;

    private Long option; // option id

    private Long value; // option value id

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((option == null) ? 0 : option.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ReadableProductVariantValue other = (ReadableProductVariantValue) obj;
        return Objects.equals(code, other.code)
                && Objects.equals(name, other.name)
                && Objects.equals(option, other.option);
    }

}
