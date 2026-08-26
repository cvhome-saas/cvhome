package com.asrevo.cvhome.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.SiteSettings;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, String> {
}
