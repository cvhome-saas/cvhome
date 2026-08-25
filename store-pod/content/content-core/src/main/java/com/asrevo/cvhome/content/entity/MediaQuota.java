package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * Bytes a store currently holds in the media library, kept in the same transaction as every upload and delete.
 */
@Entity
@Table(name = "MEDIA_QUOTA")
@Getter
@Setter
public class MediaQuota implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STORE_MERCHANT_ID", length = 50)
    private String storeMerchantId;

    @Column(name = "BYTES_USED", nullable = false)
    private long bytesUsed;

    @Column(name = "FILE_COUNT", nullable = false)
    private long fileCount;

}
