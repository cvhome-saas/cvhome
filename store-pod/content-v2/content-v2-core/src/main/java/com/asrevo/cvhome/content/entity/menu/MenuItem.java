package com.asrevo.cvhome.content.entity.menu;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.content.model.menu.MenuTargetKind;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MENU_ITEM")
@Getter
@Setter
public class MenuItem {
    @Id
    @Column(name = "MENU_ITEM_ID")
    @TableGenerator(name = "menu_item_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "MENU_ITEM_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "menu_item_gen")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MENU_CONTENT_ID", nullable = false)
    private ContentMenu menu;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ITEM_ID")
    private MenuItem parent;
    @Column(name = "POSITION", nullable = false)
    private int position;
    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_KIND", nullable = false, length = 20)
    private MenuTargetKind targetKind;
    @Column(name = "TARGET_VALUE", length = 1000)
    private String targetValue;
    @Column(name = "OPEN_NEW_TAB", nullable = false)
    private boolean openNewTab;
    @Column(name = "VISIBLE", nullable = false)
    private boolean visible;
    @Column(name = "LOGIN_REQUIRED", nullable = false)
    private boolean loginRequired;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> children = new ArrayList<>();
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItemDescription> descriptions = new ArrayList<>();

    public void addChild(MenuItem child) {
        child.setParent(this);
        child.setMenu(menu);
        children.add(child);
    }

    public void addDescription(MenuItemDescription description) {
        description.setItem(this);
        descriptions.add(description);
    }
}
