package ntou.soselab.swagger.swagger;

import ntou.soselab.swagger.algo.CosineSimilarity;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DecimalFormat;
import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DCSTest {

    Logger log = LoggerFactory.getLogger(DCSTest.class);

    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    OperationRepository operationRepository;

    CosineSimilarity cosineSimilarity = new CosineSimilarity();


    private double wordnetScore = 0.9;
    private double resourceP = 0.6;
    private double operationP = 0.4;

    //@Test
    public void compareService() {
        getDCS(416527L, 157782L);
        getDCS(233474L, 403456L);
        getDCS(233474L, 258052L);
        getDCS(296960L, 403456L);
        getDCS(296960L, 258052L);
    }

    public void getDCS(Long currentId, Long id) {
        double sumScore = 0.0;

        Resource currentResource = resourceRepository.findResourceById(currentId);
        Resource compareResource = resourceRepository.findResourceById(id);

        double resourceScore = 0.0;
        double operationScore = 0.0;

        // resource score sim_rc(LDA, LDA)
        if(currentResource.getOriginalWord() != null && compareResource.getOriginalWord() != null) {
            if(!currentResource.getOriginalWord().isEmpty() && !compareResource.getOriginalWord().isEmpty()) {
                resourceScore = resourceScore + calculateTwoMatrixVectorsAndCosineSimilarity(currentResource.getOriginalWord(),compareResource.getOriginalWord());
            }
        }
        // resource score sim_rc(LDA, LDA) = sim_rc(LDA, LDA) + ( sim_rc(LDA, Wordnet) * wordnetScore )
        if(currentResource.getWordnetWord() != null && compareResource.getWordnetWord() != null) {
            if(!currentResource.getWordnetWord().isEmpty() && !compareResource.getWordnetWord().isEmpty()) {

                // 判斷何者 wordnet 加權分數較高
                double s1 = calculateTwoMatrixVectorsAndCosineSimilarity(currentResource.getOriginalWord(), compareResource.getWordnetWord());
                double s2 = calculateTwoMatrixVectorsAndCosineSimilarity(compareResource.getOriginalWord(), currentResource.getWordnetWord());
                if(s1 >= s2) {
                    resourceScore = resourceScore + (s1 * wordnetScore);
                }else {
                    resourceScore = resourceScore + (s2 * wordnetScore);
                }
            }
        }

        log.info("Resource Score :{}", resourceScore);

        // operation score
        ArrayList<String> currentOperationLDA = new ArrayList<>();
        ArrayList<String> currentOperationWordnet = new ArrayList<>();
        ArrayList<String> compareOperationLDA = new ArrayList<>();
        ArrayList<String> compareOperationWordnet = new ArrayList<>();

        for(Operation operation : operationRepository.findOperationsByResource(currentResource.getNodeId())) {
            if(operation.getOriginalWord() != null) {
                if(!operation.getOriginalWord().isEmpty()) {
                    for(String word : operation.getOriginalWord()) {
                        currentOperationLDA.add(word);
                    }
                }
            }

            if(operation.getWordnetWord() != null) {
                if(!operation.getWordnetWord().isEmpty()) {
                    for(String word : operation.getWordnetWord()) {
                        currentOperationWordnet.add(word);
                    }
                }
            }
        }

        for(Operation operation : operationRepository.findOperationsByResource(id)) {
            if(operation.getOriginalWord() != null) {
                if(!operation.getOriginalWord().isEmpty()) {
                    for(String word : operation.getOriginalWord()) {
                        compareOperationLDA.add(word);
                    }
                }
            }

            if(operation.getWordnetWord() != null) {
                if(!operation.getWordnetWord().isEmpty()) {
                    for(String word : operation.getWordnetWord()) {
                        compareOperationWordnet.add(word);
                    }
                }
            }
        }
        // operation score sim_op(LDA, LDA) = sim_op(LDA, LDA)
        if(!currentOperationLDA.isEmpty() && !compareOperationLDA.isEmpty()) {
            operationScore = operationScore + calculateTwoMatrixVectorsAndCosineSimilarity(currentOperationLDA, compareOperationLDA);
        }

        // operation score sim_op(LDA, LDA) = sim_op(LDA, LDA) + ( sim_op(LDA, Wordnet) * wordnetScore )
        if(!currentOperationWordnet.isEmpty() && !compareOperationWordnet.isEmpty()) {

            // 判斷何者 wordnet 加權分數較高
            double s1 = calculateTwoMatrixVectorsAndCosineSimilarity(currentOperationLDA, compareOperationWordnet);
            double s2 = calculateTwoMatrixVectorsAndCosineSimilarity(compareOperationLDA, currentOperationWordnet);
            if(s1 >= s2) {
                operationScore = operationScore + (s1 * wordnetScore);
            }else {
                operationScore = operationScore + (s2 * wordnetScore);
            }
        }

        log.info("Operation Score :{}", operationScore);


        if(currentResource.getNodeId().equals(id)) {
            sumScore = 0;
        }else {
            sumScore = 1-((resourceScore * resourceP) + (operationScore * operationP));
            if(sumScore < 0) sumScore = 0;
        }

        DecimalFormat df = new DecimalFormat("0.00");
        String str = df.format(sumScore);

        log.info("Sum Score :{}", str);
    }

    public double calculateTwoMatrixVectorsAndCosineSimilarity(ArrayList<String> targetVector, ArrayList<String> compareVector) {

        ArrayList<String> allWord = new ArrayList<>();
        double cosineSimilarityScore = 0.0;

        // record all appear word
        for(String str : targetVector){
            if(!allWord.contains(str)) {
                allWord.add(str);
            }
        }
        for(String str : compareVector){
            if(!allWord.contains(str)) {
                allWord.add(str);
            }
        }


        // calculate two matrix vector
        double[] target = new double[allWord.size()];
        double[] compare = new double[allWord.size()];
        for(int i = 0;i < allWord.size();i++){
            boolean flag = true;
            for(String str : targetVector){
                if(allWord.get(i).equals(str)) {
                    target[i]++;
                    flag = false;
                }
            }
            if(flag) target[i] = 0.0;
        }

        for(int i = 0;i < allWord.size();i++){
            boolean flag = true;
            for(String str : compareVector){
                if(allWord.get(i).equals(str)) {
                    compare[i]++;
                    flag = false;
                }
            }
            if(flag) compare[i] = 0.0;
        }
        cosineSimilarityScore = cosineSimilarity.cosineSimilarity(target, compare);

        return cosineSimilarityScore;
    }
}
