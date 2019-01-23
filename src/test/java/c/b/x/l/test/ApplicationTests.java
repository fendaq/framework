package c.b.x.l.test;

import c.b.x.l.test.repository.UserRepository;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationTests.class);

    @Autowired
    private UserRepository userRepository;

    @org.junit.Test
    public void save() {
        System.out.println(userRepository.countHql("select count(1) from User where id = ?1", new Object[]{"1"}));
    }

}

