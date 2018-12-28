package c.x.b.l.jpa;

import c.x.b.l.jpa.repository.BaseRepositoryImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * eg， main函数引入@EnableJpaRepositories(repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class)
 * eg, spring:
 *   jpa:
 *     show-sql: true
 *     database: mysql
 *     generate-ddl: true
 *     hibernate:
 *       ddl-auto: update
 *   datasource:
 *     url: jdbc:mysql://localhost:3306/toolkit?serverTimezone=GMT%2B8
 *     username: root
 *     password: admin
 *     type: com.alibaba.druid.pool.DruidDataSource
 *
 */


/**
 * @Author:LiuBingXu
 * @Description: 创建自定义工厂
 * @Date: 2018/7/28.
 * @Modified by
 */
public class BaseRepositoryFactoryBean<T extends JpaRepository<S, ID>, S, ID extends Serializable>
        extends JpaRepositoryFactoryBean<T, S, ID> {

    public BaseRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        return new MyRepositoryFactory(em);
    }

    private static class MyRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {

        private final EntityManager em;

        public MyRepositoryFactory(EntityManager em) {
            super(em);
            this.em = em;
        }

        @Override
        protected BaseRepositoryImpl getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
            return new BaseRepositoryImpl<T, I>((Class<T>) information.getDomainType(), entityManager);
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return BaseRepositoryImpl.class;
        }
    }
}
