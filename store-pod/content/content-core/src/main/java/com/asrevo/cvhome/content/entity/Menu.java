package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.asrevo.cvhome.content.model.MenuHandle;

import lombok.Getter;
import lombok.Setter;

/**
 * A storefront menu (MAIN or FOOTER) and its flat item rows; the tree is rebuilt from {@code parentId}.
 */
@Entity
@Table(name = "MENU", uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "HANDLE"}))
@Getter
@Setter
public class Menu implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)
    private String storeMerchantId;

    @Column(name = "HANDLE", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private MenuHandle handle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "NAMES")
    private String names;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("position asc, id asc")
    private List<MenuItem> items = new ArrayList<>();

}
