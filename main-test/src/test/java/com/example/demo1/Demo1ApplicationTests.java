package com.example.demo1;

import com.example.demo1.repository.UserRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Demo1ApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @org.junit.Test
    public void asf() {
        com.example.demo1.User test = new com.example.demo1.User();
        test.setId("123");
        test.setName("测试");
        userRepository.save(test);
        List listBySql = userRepository.getListBySql("select * from user where id = ?", new Object[]{"123"});
        System.out.println(listBySql);
    }

}

