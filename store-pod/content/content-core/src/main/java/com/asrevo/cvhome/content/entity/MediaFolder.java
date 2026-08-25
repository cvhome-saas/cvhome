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

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MEDIA_FOLDER", uniqueConstraints = @UniqueConstraint(name = "media_folder_store_key_unique",
        columnNames = {"STORE_MERCHANT_ID", "FOLDER_KEY"}))
@Getter
@Setter
public class MediaFolder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "content_seq", sequenceName = "content_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "content_seq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)
    private String storeMerchantId;

    @Column(name = "NAME", nullable = false, length = 60)
    private String name;

    @Column(name = "FOLDER_KEY", nullable = false, length = 60)
    private String key;

    @Column(name = "POSITION", nullable = false)
    private Integer position = 0;

    @Column(name = "SYSTEM_FOLDER", nullable = false)
    private boolean system;

}
