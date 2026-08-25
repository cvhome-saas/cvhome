package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;

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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.asrevo.cvhome.content.model.MenuTargetKind;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MENU_ITEM")
@Getter
@Setter
public class MenuItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MENU_ID", nullable = false)
    private Menu menu;

    @Column(name = "PARENT_ID")
    private Long parentId;

    @Column(name = "POSITION", nullable = false)
    private Integer position = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "LABELS", nullable = false)
    private String labels;

    @Column(name = "TARGET_KIND", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private MenuTargetKind targetKind;

    @Column(name = "TARGET_VALUE", length = 255)
    private String targetValue;

    @Column(name = "OPEN_IN_NEW_TAB", nullable = false)
    private boolean openInNewTab;

    @Column(name = "VISIBLE", nullable = false)
    private boolean visible = true;

}
