package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Redirect;
import com.asrevo.cvhome.content.repository.RedirectRepository;

import lombok.RequiredArgsConstructor;

/**
 * Storefront paths that moved. Paths are the storefront's own ({@code /content/<slug>} for pages,
 * {@code /blog/<slug>} for posts), locale-less.
 */
@Service
@RequiredArgsConstructor
public class RedirectService {

    private final RedirectRepository repository;

    public void moved(StoreMerchantId store, String fromPath, String toPath) {
        if (fromPath == null || fromPath.equals(toPath)) {
            return;
        }
        // the new path must never redirect anywhere itself
        repository.deleteByStoreMerchantIdAndFromPath(store.getId(), toPath);
        Redirect r = repository.findByStoreMerchantIdAndFromPath(store.getId(), fromPath).orElseGet(Redirect::new);
        r.setStoreMerchantId(store.getId());
        r.setFromPath(fromPath);
        r.setToPath(toPath);
        repository.save(r);
    }

    public Optional<String> resolve(StoreMerchantId store, String path) {
        return repository.findByStoreMerchantIdAndFromPath(store.getId(), path).map(Redirect::getToPath);
    }

    public List<Redirect> list(StoreMerchantId store) {
        return repository.findByStoreMerchantIdOrderByCreatedAtDesc(store.getId());
    }

}
