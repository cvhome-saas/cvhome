package com.asrevo.cvhome.content.entity.menu;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class MenuItemDescriptionId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "MENU_ITEM_ID")
    private Long menuItemId;
    @Column(name = "LANGUAGE_CODE", length = 6)
    private String languageCode;
}
