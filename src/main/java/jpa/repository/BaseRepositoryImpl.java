package jpa.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
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
    public List<T> listHql(String hql) {
        return entityManager.createQuery(hql).getResultList();
    }

    @Override
    public List<T> listPageHql(String hql, int page, int size) {
        return entityManager.createQuery(hql)
                .setFirstResult(page)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<T> listPageHql(String hql, int page, int size, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
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
    public List<T> listPageHql(String hql, int page, int size, Object[] params) {
        return this.listPageHql(hql, page, size, Arrays.asList(params));
    }

    @Override
    public List<T> listPageSql(String sql, int page, int size) {
        return entityManager.createNativeQuery(sql)
                .setFirstResult(page)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<T> listPageSql(String sql, int page, int size, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
        Query query = entityManager.createNativeQuery(sql);
        params.forEach(parm -> {
            query.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        });
        return query.setFirstResult(page)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<T> listPageSql(String sql, int page, int size, Object[] params) {
        Assert.notNull(params, "参数不能为NULL");
        return this.listPageSql(sql, page, size, Arrays.asList(params));
    }

    @Override
    public T findSql(String sql, Object[] obj) {
        return this.findSql(sql, Arrays.asList(obj));
    }

    @Override
    public T findSql(String sql, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
        Query nativeQuery = entityManager.createNativeQuery(sql);
        params.forEach(parm -> {
            nativeQuery.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        });
        return (T) nativeQuery.getSingleResult();
    }

    @Override
    public List<T> findListSql(String sql, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
        Query nativeQuery = entityManager.createNativeQuery(sql);
        params.forEach(parm -> {
            nativeQuery.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        });
        return nativeQuery.getResultList();
    }

    @Override
    public List<T> findListSql(String sql, Object[] params) {
        Assert.notNull(params, "参数不能为NULL");
        return this.findListSql(sql, Arrays.asList(params));
    }

    @Override
    public List<Object[]> getListSql(String sql, Object[] params) {
        return this.getListSql(sql, Arrays.asList(params));
    }

    @Override
    public List<Object[]> getListSql(String sql, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
        Query nativeQuery = entityManager.createNativeQuery(sql);
        params.forEach(parm -> {
            nativeQuery.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        });
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
    public int executeUpdateSql(String sql) {
        return entityManager.createNativeQuery(sql).executeUpdate();
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateSql(String sql, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
        Query nativeQuery = entityManager.createNativeQuery(sql);
        params.forEach(parm -> {
            nativeQuery.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        });
        return nativeQuery.executeUpdate();
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateSql(String sql, Object[] params) {
        return this.executeUpdateSql(sql, Arrays.asList(params));
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateHql(String hql) {
        return entityManager.createQuery(hql).executeUpdate();
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateHql(String hql, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
        Query query = entityManager.createQuery(hql);
        params.forEach(parm -> {
            query.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        });
        return query.executeUpdate();
    }

    @Override
    @Transactional
    @Modifying
    public int executeUpdateHql(String hql, Object[] params) {
        return this.executeUpdateHql(hql, Arrays.asList(params));
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
    public Long countHql(String hql) {
        return this.countHql(hql, new ArrayList<>());
    }

    @Override
    public Long countHql(String hql, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
        Query query = entityManager.createQuery(hql);
        params.forEach(parm -> {
            query.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        });
        return Long.parseLong(query.getSingleResult().toString());
    }

    @Override
    public Long countHql(String hql, Object[] params) {
        return this.countHql(hql, Arrays.asList(params));
    }

    @Override
    public Long countSql(String sql) {
        return this.countHql(sql, new ArrayList<>());
    }

    @Override
    public Long countSql(String sql, List<Object> params) {
        Assert.notNull(params, "参数不能为NULL");
        Query query = entityManager.createNativeQuery(sql);
        params.forEach(parm -> {
            query.setParameter(params.indexOf(parm) + 1, parm);
            LOGGER.info("binding parms index : " + params.indexOf(parm) + " value : " + parm);
        });
        return Long.parseLong(query.getSingleResult().toString());
    }

    @Override
    public Long countSql(String sql, Object[] params) {
        return this.countSql(sql, Arrays.asList(params));
    }

    @Override
    public boolean support(String modelType) {
        return domainClass.getName().equals(modelType);
    }
}