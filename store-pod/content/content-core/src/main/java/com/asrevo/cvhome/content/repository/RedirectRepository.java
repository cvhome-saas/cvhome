package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.Redirect;

public interface RedirectRepository extends JpaRepository<Redirect, Long> {

    Optional<Redirect> findByStoreMerchantIdAndFromPath(String store, String fromPath);

    List<Redirect> findByStoreMerchantIdOrderByCreatedAtDesc(String store);

    void deleteByStoreMerchantIdAndFromPath(String store, String fromPath);

}
