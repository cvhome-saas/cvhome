package com.asrevo.cvhome.content.entity.menu;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import com.asrevo.cvhome.commons.domain.LanguageCode;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MENU_ITEM_DESCRIPTION")
@Getter
@Setter
public class MenuItemDescription {
    @EmbeddedId
    private MenuItemDescriptionId id = new MenuItemDescriptionId();
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("menuItemId")
    @JoinColumn(name = "MENU_ITEM_ID")
    private MenuItem item;
    @Column(name = "LABEL", nullable = false, length = 255)
    private String label;

    public LanguageCode getLanguageCode() {
        return new LanguageCode(id.getLanguageCode());
    }

    public void setLanguageCode(LanguageCode languageCode) {
        id.setLanguageCode(languageCode.code());
    }
}
