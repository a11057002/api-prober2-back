package ntou.soselab.swagger.swagger;

import ntou.soselab.swagger.transformation.UpdateLDAWordNetWord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UpdateLDAWordNetTest {

    Logger log = LoggerFactory.getLogger(UpdateLDAWordNetTest.class);

    @Autowired
    UpdateLDAWordNetWord updateLDAWordNetWord;

    @Test
    public void updateResourceTest() {
        updateLDAWordNetWord.updateResource();
    }

    @Test
    public void updateOperationTest() {
        updateLDAWordNetWord.updateOperation();
    }

    @Test
    public void updateParameterTest() {
        updateLDAWordNetWord.updateParameter();
    }

    @Test
    public void updateResponseTest() {
        updateLDAWordNetWord.updateResponse();
    }
}
