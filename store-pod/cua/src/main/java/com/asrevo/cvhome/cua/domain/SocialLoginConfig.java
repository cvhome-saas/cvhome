package com.asrevo.cvhome.cua.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_login_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginConfig implements Serializable {

    @EmbeddedId
    private SocialLoginConfigId id;

    @Column(name = "app_id", nullable = false)
    private String appId;

    @Column(name = "app_secret", nullable = false)
    private String appSecret;

    @Column(name = "enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean enabled = true;

}
