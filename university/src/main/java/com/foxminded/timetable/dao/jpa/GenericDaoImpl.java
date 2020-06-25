package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.GenericDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class GenericDaoImpl<T> implements GenericDao<T> {

    protected final Class<T> entityClass;
    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public long count() {

        log.debug("Counting {}", entityClass.getSimpleName());

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        query.select(criteriaBuilder.count(query.from(entityClass)));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<T> findAll() {

        log.debug("Retrieving {}", entityClass.getSimpleName());

        CriteriaQuery<T> query =
                entityManager.getCriteriaBuilder().createQuery(entityClass);
        query.select(query.from(entityClass));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<T> findById(long id) {

        log.debug("Looking for {} by ID {}", entityClass.getSimpleName(), id);

        T entity = entityManager.find(entityClass, id);
        return entity == null ? Optional.empty() : Optional.of(entity);
    }

    @Override
    public List<T> saveAll(List<T> entities) {

        log.debug("Persisting {}", entityClass.getSimpleName());
        for (T entity : entities) {
            entityManager.persist(entity);
        }
        return entities;
    }

    @Override
    public T save(T entity) {

        log.debug("Merging {}", entityClass.getSimpleName());
        return entityManager.merge(entity);
    }

}
