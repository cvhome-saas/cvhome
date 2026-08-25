package com.asrevo.cvhome.store.core.services.generic;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

/**
 *
 */
public abstract class SalesManagerEntityServiceImpl<K extends Serializable & Comparable<K>, E extends SalesManagerEntity<K, ?>>
        implements SalesManagerEntityService<K, E> {


    private final JpaRepository<E, K> repository;

    @SuppressWarnings("unchecked")
    protected SalesManagerEntityServiceImpl(JpaRepository<E, K> repository) {
        this.repository = repository;
    }

    public E getById(K id) {
        return repository.getReferenceById(id);
    }

    public void save(E entity) {
        repository.saveAndFlush(entity);
    }

    public void saveAll(Iterable<E> entities) {
        repository.saveAll(entities);
    }

    public void create(E entity) {
        save(entity);
    }

    public void update(E entity) {
        save(entity);
    }

    public void delete(E entity) {
        repository.delete(entity);
    }

    public List<E> list() {
        return repository.findAll();
    }

    protected E saveAndFlush(E entity) {
        return repository.saveAndFlush(entity);
    }

}
