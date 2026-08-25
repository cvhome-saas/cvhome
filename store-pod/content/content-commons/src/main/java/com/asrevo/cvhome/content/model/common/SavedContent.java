package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.content.model.ContentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What a write returns: the id, the status after the write and the new optimistic version.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavedContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private ContentStatus status;

    private Integer version;

}
