package ntou.soselab.swagger.cluster;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClusterTest {

    Logger log = LoggerFactory.getLogger(ClusterTest.class);

    @Autowired
    ReadClusterCSV readClusterCSV;
    @Autowired
    ClusterTopicJson clusterTopicJson;

    @Test
    public void importClusterResultTest() {
        HashMap<Long, String> cluster = readClusterCSV.readClusterResult();
        readClusterCSV.saveClusterToNeo4j(cluster);
    }

    @Test
    public void createJsonTest() {
        clusterTopicJson.saveClusterTopicJson(clusterTopicJson.collectionEachClusterGroupWord());

    }

    @Test
    public void updateJsonTest() {
        clusterTopicJson.updateClusterTopicJson(clusterTopicJson.collectionEachClusterGroupWord());
    }
}
