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

    List<T> listHql(String hql);

    List<T> listPageHql(String hql, int page, int size);

    List<T> listPageHql(String hql, int page, int size, List<Object> params);

    List<T> listPageHql(String hql, int page, int size, Object[] params);

    List<T> listPageSql(String sql, int page, int size);

    List<T> listPageSql(String sql, int page, int size, List<Object> params);

    List<T> listPageSql(String sql, int page, int size, Object[] params);

    T findSql(String sql, Object[] params);

    T findSql(String sql, List<Object> params);

    List<T> findListSql(String sql, Object[] params);

    List<T> findListSql(String sql, List<Object> params);

    List<Object[]> getListSql(String sql, Object[] params);

    List<Object[]> getListSql(String sql, List<Object> params);

    Page<T> pageList(Pageable pageable);

    Page<T> pageList(Pageable pageable, Specification specifications);

    int executeUpdateSql(String sql);

    int executeUpdateSql(String sql, List<Object> params);

    int executeUpdateSql(String sql, Object[] params);

    int executeUpdateHql(String hql);

    int executeUpdateHql(String hql, List<Object> params);

    int executeUpdateHql(String hql, Object[] params);

    void batchDelete(List<ID> ids);

    Long countHql(String hql);

    Long countHql(String hql, List<Object> params);

    Long countHql(String hql, Object[] params);

    Long countSql(String sql);

    Long countSql(String sql, List<Object> params);

    Long countSql(String sql,  Object[] params);
}