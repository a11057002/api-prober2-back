package ntou.soselab.swagger.cluster;

import ntou.soselab.swagger.algo.CosineSimilarity;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CosineSimilarityTest {

    Logger log = LoggerFactory.getLogger(CosineSimilarityTest.class);

    CosineSimilarity cosineSimilarity = new CosineSimilarity();

    //@Test
    public void cosineSimilarityScoreTest() {
        double[] target = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        double[] compare = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double score = cosineSimilarity.cosineSimilarity(target, compare);
        log.info("CS Score :{}", score);

        double[] target2 = {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        double[] compare2 = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double score2 = cosineSimilarity.cosineSimilarity(target2, compare2);
        log.info("CS Score :{}", score2);

        double[] target3 = {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        double[] compare3 = {0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        double score3 = cosineSimilarity.cosineSimilarity(target3, compare3);
        log.info("CS Score :{}", score3);

        double[] target4 = {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        double[] compare4 = {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double score4 = cosineSimilarity.cosineSimilarity(target4, compare4);
        log.info("CS Score :{}", score4);
    }

}