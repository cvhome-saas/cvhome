package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReadableRevision implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer version;

    private String author;

    private Instant createdAt;

}
