package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.content.model.TranslationState;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Per-locale badge for list rows: the language code and how complete that locale is.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocaleState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private TranslationState state;

}
