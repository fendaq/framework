package jpa.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseRepositoryImpl.class);

    private EntityManager entityManager;
    private final Class<T> domainClass;

    public BaseRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.domainClass = domainClass;
        this.entityManager = em;
    }

    @Override
    public List<T> listByHql(String hql) {
        return entityManager.createQuery(hql).getResultList();
    }

    @Override
    public List<T> listPageByHql(String hql, int page, int size) {
        return entityManager.createQuery(hql)
                .setFirstResult(page)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<T> listPageByHql(String hql, int page, int size, List<Object> params) {
        Query query = entityManager.createQuery(hql);
        for (Object parm : params) {
            query.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        }
        return query.setFirstResult(page)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<T> listPageByHql(String hql, int page, int size, Object[] params) {
        return this.listPageByHql(hql, page, size, Arrays.asList(params));
    }

    @Override
    public List<T> listPageBySql(String sql, int page, int size) {
        return entityManager.createNativeQuery(sql)
                .setFirstResult(page)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<T> listPageBySql(String sql, int page, int size, List<Object> params) {
        Query query = entityManager.createNativeQuery(sql);
        for (Object parm : params) {
            query.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        }
        return query.setFirstResult(page)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<T> listPageBySql(String sql, int page, int size, Object[] params) {
        return this.listPageBySql(sql, page, size, Arrays.asList(params));
    }

    @Override
    public T findBySql(String sql, Object[] obj) {
        return this.findBySql(sql, Arrays.asList(obj));
    }

    @Override
    public T findBySql(String sql, List<Object> params) {
        Query nativeQuery = entityManager.createNativeQuery(sql);
        for (Object parm : params) {
            nativeQuery.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        }
        return (T) nativeQuery.getSingleResult();
    }

    @Override
    public List<T> findListBySql(String sql, List<Object> params) {
        Query nativeQuery = entityManager.createNativeQuery(sql);
        for (Object parm : params) {
            nativeQuery.setParameter(params.indexOf(parm) + 1, parm);
        }
        return nativeQuery.getResultList();
    }

    @Override
    public List<T> findListBySql(String sql, Object[] params) {
        return this.findListBySql(sql, Arrays.asList(params));
    }

    @Override
    public List<Object[]> getListBySql(String sql, Object[] params) {
        return this.getListBySql(sql, Arrays.asList(params));
    }

    @Override
    public List<Object[]> getListBySql(String sql, List<Object> params) {
        Query nativeQuery = entityManager.createNativeQuery(sql);
        for (Object parm : params) {
            nativeQuery.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        }
        return nativeQuery.getResultList();
    }

    @Override
    public Page<T> pageList(Pageable pageable) {
        return this.findAll(pageable);
    }

    @Override
    public Page<T> pageList(Pageable pageable, Specification specifications) {
        return this.findAll(specifications, pageable);
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateBySql(String sql) {
        return entityManager.createNativeQuery(sql).executeUpdate();
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateBySql(String sql, List<Object> params) {
        Query nativeQuery = entityManager.createNativeQuery(sql);
        for (Object parm : params) {
            nativeQuery.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        }
        return nativeQuery.executeUpdate();
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateBySql(String sql, Object[] params) {
        return this.executeUpdateBySql(sql, Arrays.asList(params));
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateByHql(String hql) {
        return entityManager.createQuery(hql).executeUpdate();
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateByHql(String hql, List<Object> params) {
        Query query = entityManager.createQuery(hql);
        for (Object parm : params) {
            query.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        }
        return query.executeUpdate();
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateByHql(String hql, Object[] params) {
        return this.executeUpdateByHql(hql, Arrays.asList(params));
    }

    @Override
    @Transactional
    @Modifying
    public void batchDelete(List<ID> ids) {
        ids.forEach(id -> {
            this.deleteById(id);
            LOGGER.info("delete ID " + id);
        });
    }

    @Override
    public boolean support(String modelType) {
        return domainClass.getName().equals(modelType);
    }
}