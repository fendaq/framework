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

    /**
     *  查询集合
     * @param hql hql语句
     * @return 集合
     */
    List<T> listHql(String hql);

    /**
     * 查询集合
     * @param sql sql语句
     * @return 集合
     */
    List<T> listSql(String sql);

    /**
     * 分页查询
     * @param hql hql语句
     * @param page 页码从0开始
     * @param size 一页大小
     * @return 集合
     */
    List<T> listPageHql(String hql, int page, int size);

    /**
     * 分页查询
     * @param hql hql语句
     * @param page 页码从0开始
     * @param size 一页大小
     * @param params 参数
     * @return 集合
     */
    List<T> listPageHql(String hql, int page, int size, List<Object> params);

    /**
     * 分页查询
     * @param hql hql语句
     * @param page 页码从0开始
     * @param size 一页大小
     * @param params 参数
     * @return 集合
     */
    List<T> listPageHql(String hql, int page, int size, Object[] params);

    /**
     * 分页查询
     * @param sql sql
     * @param page 页码从0开始
     * @param size 一页大小
     * @return 集合
     */
    List<T> listPageSql(String sql, int page, int size);

    /**
     * 分页查询
     * @param sql sql
     * @param page 页码从0开始
     * @param size 一页大小
     * @param params 参数
     * @return 集合
     */
    List<T> listPageSql(String sql, int page, int size, List<Object> params);

    /**
     * 分页查询
     * @param sql sql
     * @param page 页码从0开始
     * @param size 一页大小
     * @param params 参数
     * @return 集合
     */
    List<T> listPageSql(String sql, int page, int size, Object[] params);

    /**
     * 查询对象
     * @param sql sql语句
     * @param params 参数
     * @return 对象
     */
    T findSql(String sql, Object[] params);

    /**
     * 查询对象
     * @param sql sql语句
     * @param params 参数
     * @return 对象
     */
    T findSql(String sql, List<Object> params);

    /**
     * 查询对象
     * @param hql hql语句
     * @param params 参数
     * @return 对象
     */
    T findHql(String hql, Object[] params);

    /**
     * 查询对象
     * @param hql hql
     * @param params 参数
     * @return 对象
     */
    T findHql(String hql, List<Object> params);


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