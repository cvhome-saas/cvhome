package com.asrevo.cvhome.sso.realm;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RealmRepository extends JpaRepository<Realm, String> {

    boolean existsByIdAndEnabledTrue(String id);

    List<Realm> findAllByEnabledTrue();

}
