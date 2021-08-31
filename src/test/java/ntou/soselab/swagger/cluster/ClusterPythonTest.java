package ntou.soselab.swagger.cluster;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClusterPythonTest {
    Logger log = LoggerFactory.getLogger(ClusterPythonTest.class);

    @Test
    public void useClusterPython() {
        String path = "./src/main/resources";
        ProcessBuilder pb = new ProcessBuilder("python", path+"/cluster.py", path);
        pb.redirectErrorStream(true);
        try {
            pb.start();
        } catch (IOException e) {
            log.info(e.toString());
        }
    }
}
