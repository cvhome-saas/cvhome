package com.asrevo.cvhome.domainownership.service.Impl;

import com.asrevo.cvhome.domainownership.commons.domain.Email;
import com.asrevo.cvhome.domainownership.commons.domain.IdentityId;
import com.asrevo.cvhome.domainownership.domain.OwnerEntity;
import com.asrevo.cvhome.domainownership.repository.OwnerRepository;
import com.asrevo.cvhome.domainownership.service.OwnerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Optional;

@AllArgsConstructor
@Slf4j
@Service
public class OwnerServiceImpl implements OwnerService {
    private final OwnerRepository ownerRepository;

    @Override
    public OwnerEntity getSystemOwner() {
        return ownerRepository.findByIdentity(IdentityId.ofSys()).orElseGet(this::createSysAccount);
    }

    private OwnerEntity createSysAccount() {
        Email email = new Email("sys-mail@gmail.com");
        return ownerRepository.save(OwnerEntity.create(email, IdentityId.ofSys()));
    }

    @Transactional
    @Override
    public OwnerEntity getOwner(Principal principal) {
        return ownerRepository.findByIdentity(IdentityId.of(principal.getName())).orElseGet(() -> createOwnerAccount(principal));
    }

    @Transactional
    @Override
    public OwnerEntity save(OwnerEntity owner) {
        return ownerRepository.save(owner);
    }

    private OwnerEntity createOwnerAccount(Principal principal) {
        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) principal;
        Email email = Optional.ofNullable(jwtToken.getToken().getClaim("email")).map(it -> new Email(String.valueOf(it))).orElse(new Email("sys-mail@gmail.com"));
        OwnerEntity entity = OwnerEntity.create(email, new IdentityId(principal.getName()));
        return ownerRepository.save(entity);
    }
}
