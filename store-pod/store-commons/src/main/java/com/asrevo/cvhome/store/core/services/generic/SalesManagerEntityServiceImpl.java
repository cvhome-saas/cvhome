package com.asrevo.cvhome.store.core.services.generic;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 */
public abstract class SalesManagerEntityServiceImpl<
                K extends Serializable & Comparable<K>, E extends SalesManagerEntity<K, ?>>
        implements SalesManagerEntityService<K, E> {

    /**
     * Classe de l'entité, déterminé à partir des paramètres generics.
     */
    private final Class<E> objectClass;

    private final JpaRepository<E, K> repository;

    @SuppressWarnings("unchecked")
    public SalesManagerEntityServiceImpl(JpaRepository<E, K> repository) {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.objectClass = (Class<E>) genericSuperclass.getActualTypeArguments()[1];
        this.repository = repository;
    }

    public E getById(K id) {
        return repository.getOne(id);
    }

    public void save(E entity) {
        repository.saveAndFlush(entity);
    }

    public void saveAll(Iterable<E> entities) {
        repository.saveAll(entities);
    }

    public void create(E entity) throws ServiceException {
        save(entity);
    }

    public void update(E entity) throws ServiceException {
        save(entity);
    }

    public void delete(E entity) throws ServiceException {
        repository.delete(entity);
    }

    public List<E> list() {
        return repository.findAll();
    }

    protected E saveAndFlush(E entity) {
        return repository.saveAndFlush(entity);
    }
}
