package ntou.soselab.swagger.javaparser;

import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.JavaRepoRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AddAPIExampleFeature {

    Logger log = LoggerFactory.getLogger(AddAPIExampleFeature.class);

    @Autowired
    JavaRepoRepository javaRepoRepository;
    @Autowired
    ResourceRepository resourceRepository;

    @Test
    public void addExampleFeature() {

        for(Resource resource : javaRepoRepository.findResourceByHaveJavaRepo()) {

            ArrayList<String> oldFeature = resource.getFeature();
            oldFeature.add("Example API conversations");
            resource.setFeatures(oldFeature);
            resourceRepository.save(resource);
            
        }

    }

}
