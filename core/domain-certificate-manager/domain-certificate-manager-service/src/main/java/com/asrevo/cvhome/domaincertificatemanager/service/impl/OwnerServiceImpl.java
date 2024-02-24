package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.domaincertificatemanager.entity.OwnerEntity;
import com.asrevo.cvhome.domaincertificatemanager.repository.OwnerRepository;
import com.asrevo.cvhome.domaincertificatemanager.service.OwnerService;
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

    @Transactional
    @Override
    public OwnerEntity getOwnerOrCreate(IdentityId identityId, Principal principal) {
        return identityId.equals(IdentityId.ofSys()) ? getSystemOwner() : getOwner(principal);
    }

    @Transactional
    @Override
    public void addDomain(IdentityId identityId, DomainId domainId) {
        OwnerEntity owner = ownerRepository.findById(identityId).orElseThrow(() -> new RuntimeException("owner not registered yet to own domains"));
        ownerRepository.save(owner.addToDomains(domainId));
    }

    private OwnerEntity getSystemOwner() {
        return ownerRepository.findById(IdentityId.ofSys()).orElseGet(this::createSysAccount);
    }

    private OwnerEntity createSysAccount() {
        Email email = new Email("sys-mail@gmail.com");
        return ownerRepository.save(OwnerEntity.create(email, IdentityId.ofSys()));
    }

    private OwnerEntity getOwner(Principal principal) {
        return ownerRepository.findById(IdentityId.of(principal.getName())).orElseGet(() -> createOwnerAccount(principal));
    }

    private OwnerEntity createOwnerAccount(Principal principal) {
        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) principal;
        Email email = Optional.ofNullable(jwtToken.getToken().getClaim("email")).map(it -> new Email(String.valueOf(it))).orElse(new Email("sys-mail@gmail.com"));
        OwnerEntity entity = OwnerEntity.create(email, new IdentityId(principal.getName()));
        return ownerRepository.save(entity);
    }
}
