package ntou.soselab.swagger.neo4j;

import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SaveNeo4jTest {

    @Autowired
    ResourceRepository resourceRepository;

    //@Test
    public void saveResource() {

        for(Resource resource : resourceRepository.findAll()) {
            ArrayList<String> oldFeature = resource.getFeature();
            if(oldFeature.contains("At most 20 operations")) {
                oldFeature.remove("At most 20 operations");
            }else if(!oldFeature.contains("At most 20 operations")) {
                oldFeature.add("At most 20 operations");
            }
            resource.setFeatures(oldFeature);
            resourceRepository.save(resource);
        }

    }
}
