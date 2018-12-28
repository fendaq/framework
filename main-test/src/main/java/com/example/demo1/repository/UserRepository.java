package com.example.demo1.repository;

import c.x.b.l.jpa.repository.BaseRepository;
import com.example.demo1.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface UserRepository extends BaseRepository<User, String> {
    User findByName(String userName);

    @Transactional
    @Modifying
    @Query("update User set name = ?1 where id = ?2")
    void updateName(String name, String id);
}
