package jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID>
        , JpaSpecificationExecutor<T> {

    boolean support(String modelType);

    List<T> listByHql(String hql);

    List<T> listPageByHql(String hql, int page, int size);

    List<T> listPageByHql(String hql, int page, int size, List<Object> params);

    List<T> listPageByHql(String hql, int page, int size, Object[] params);

    List<T> listPageBySql(String sql, int page, int size);

    List<T> listPageBySql(String sql, int page, int size, List<Object> params);

    List<T> listPageBySql(String sql, int page, int size, Object[] params);

    T findBySql(String sql, Object[] params);

    T findBySql(String sql, List<Object> params);

    List<T> findListBySql(String sql, Object[] params);

    List<T> findListBySql(String sql, List<Object> params);

    List<Object[]> getListBySql(String sql, Object[] params);

    List<Object[]> getListBySql(String sql, List<Object> params);

    Page<T> pageList(Pageable pageable);

    Page<T> pageList(Pageable pageable, Specification specifications);

    int executeUpdateBySql(String sql);

    int executeUpdateBySql(String sql, List<Object> params);

    int executeUpdateBySql(String sql, Object[] params);

    int executeUpdateByHql(String hql);

    int executeUpdateByHql(String hql, List<Object> params);

    int executeUpdateByHql(String hql, Object[] params);

    void batchDelete(List<ID> ids);
}