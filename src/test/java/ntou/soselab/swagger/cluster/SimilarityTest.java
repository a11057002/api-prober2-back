package ntou.soselab.swagger.cluster;

import ntou.soselab.swagger.algo.ParametersSimilarityOfTwoServices;
import ntou.soselab.swagger.neo4j.domain.service.Parameter;
import ntou.soselab.swagger.neo4j.domain.service.Response;
import ntou.soselab.swagger.neo4j.repositories.service.ParameterRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResponseRepository;
import ntou.soselab.swagger.web.recommand.ServiceRecommender;
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
public class SimilarityTest {

    @Autowired
    ServiceRecommender serviceRecommender;

    @Autowired
    ParameterRepository parameterRepository;

    @Autowired
    ResponseRepository responseRepository;

    @Autowired
    ParametersSimilarityOfTwoServices parametersSimilarityOfTwoServices;

    Logger log = LoggerFactory.getLogger(SimilarityTest.class);


    //@Test
    public void searchSameEndpoint() {
        Long resourceId1 = 234089L;
        Long resourceId2 = 7081L;
        serviceRecommender.runRecommendService(resourceId1);
    }

    //@Test
    public void similarServiceTest() {
        ArrayList<Parameter> parameters1 = new ArrayList<>(parameterRepository.findParametersByOperation(233716L));
        ArrayList<Response> responses1 = new ArrayList<>(responseRepository.findSuccessResponsesByOperation(233716L));

        ArrayList<Parameter> parameters2 = new ArrayList<>(parameterRepository.findParametersByOperation(42184L));
        ArrayList<Response> responses2 = new ArrayList<>(responseRepository.findSuccessResponsesByOperation(42184L));


        double inputMatchScore = parametersSimilarityOfTwoServices.calculateServiceInputScore(parameters1, parameters2);
        double outputMatchScore = parametersSimilarityOfTwoServices.calculateServiceOutputScore(responses1, responses2);

        log.info("Input Match Score :{}", inputMatchScore);
        log.info("Output Match Score :{}", outputMatchScore);
    }
}
