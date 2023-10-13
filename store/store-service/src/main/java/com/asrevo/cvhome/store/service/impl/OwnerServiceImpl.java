package com.asrevo.cvhome.store.service.impl;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.asrevo.cvhome.store.entity.OwnerEntity;
import com.asrevo.cvhome.store.repository.OwnerRepository;
import com.asrevo.cvhome.store.service.OwnerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class OwnerServiceImpl implements OwnerService {
    private final OwnerRepository ownerRepository;


    @Transactional
    @Override
    public void addStore(StoreId storeId, IdentityId identityId) {
        ownerRepository.findById(identityId).ifPresent(it -> {
            it.addToStores(storeId);
            ownerRepository.save(it);
        });
    }

    @Transactional
    @Override
    public OwnerEntity getOwnerOrCreate(IdentityId identityId, Principal principal) {
        return ownerRepository.findById(identityId).orElseGet(() -> {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) principal;
            Email email = Optional.ofNullable(jwtToken.getToken().getClaim("email"))
                    .map(it -> new Email(String.valueOf(it))).orElse(new Email("sys-mail@gmail.com"));
            return ownerRepository.save(OwnerEntity.create(email, identityId));
        });
    }
}
