package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.Setter;

/**
 * An ordered group of FAQ entries ("Ordering", "Shipping & delivery"…). Names per locale as a JSON map.
 */
@Entity
@Table(name = "FAQ_GROUP", uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "GROUP_KEY"}))
@Getter
@Setter
public class FaqGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)
    private String storeMerchantId;

    @Column(name = "GROUP_KEY", nullable = false, length = 60)
    private String key;

    @Column(name = "POSITION", nullable = false)
    private Integer position = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "NAMES", nullable = false)
    private String names;

}
