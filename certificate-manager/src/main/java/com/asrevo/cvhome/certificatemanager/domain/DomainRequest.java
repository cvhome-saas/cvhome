package com.asrevo.cvhome.certificatemanager.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainRequest {

    private String domain;

    private String file;

    private URL location;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            return ((DomainRequest) o).getDomain().equals(this.getDomain());
        }
    }

    @Override
    public int hashCode() {
        return domain != null ? domain.hashCode() : 0;
    }
}
