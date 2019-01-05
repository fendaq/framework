package c.b.x.l.test;

import c.b.x.l.test.bean.User;
import c.b.x.l.test.repository.UserRepository;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationTests.class);

    @Autowired
    private UserRepository userRepository;

    @org.junit.Test
    public void save() {
        List<String> lists = new ArrayList<>();
        for (int i = 0; i < 100; i ++) {
            User test = new User();
            test.setId(String.valueOf(i));
            test.setScore("1000");
          //  userRepository.save(test);
            lists.add(String.valueOf(i));
        }
        userRepository.batchDelete(lists);
    }

}

