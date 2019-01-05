package c.b.x.l.test;

import c.b.x.l.test.bean.User;
import c.b.x.l.test.repository.UserRepository;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationTests.class);

    @Autowired
    private UserRepository userRepository;

    @org.junit.Test
    public void save() {
        User test = new User();
        test.setId("123456");
        test.setScore("1000");
        userRepository.save(test);
        List listBySql = userRepository.getListBySql("select * from dev_user where id = ?", new Object[]{"123"});
        LOGGER.info(listBySql.toString());
    }

}

