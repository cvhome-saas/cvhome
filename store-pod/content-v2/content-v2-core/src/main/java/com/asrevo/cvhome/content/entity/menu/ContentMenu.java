package com.asrevo.cvhome.content.entity.menu;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONTENT_MENU")
@Getter
@Setter
public class ContentMenu {
    @Id
    @Column(name = "CONTENT_ID")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "CONTENT_ID")
    private Content content;
    @Embedded
    @AttributeOverride(name = "storeMerchantId", column = @Column(name = "STORE_MERCHANT_ID", length = 50))
    private StoreMerchantId storeMerchantId;
    @Column(name = "HANDLE", nullable = false, length = 100)
    private String handle;
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> items = new ArrayList<>();

    public void addItem(MenuItem item) {
        item.setMenu(this);
        items.add(item);
    }

    public void replaceItems(List<MenuItem> replacements) {
        items.clear();
        replacements.forEach(this::addItem);
    }
}
