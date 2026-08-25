package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private boolean ok;

    private String errorCode;

    private String message;

}
