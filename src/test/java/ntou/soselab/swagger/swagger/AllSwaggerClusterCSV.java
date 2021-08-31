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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AllSwaggerClusterCSV {
    Logger log = LoggerFactory.getLogger(AllSwaggerClusterCSV.class);

    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    OperationRepository operationRepository;

    CosineSimilarity cosineSimilarity = new CosineSimilarity();

    private double wordnetScore = 0.9;
    private double resourceP = 0.6;
    private double operationP = 0.4;

    @Test
    public void collection_Resource_Operation_Parameter_Response() {
        try {
            PrintWriter pw = new PrintWriter(new File("./src/main/resources/1253-Swagger.csv"));
            StringBuilder sb = new StringBuilder();

            // 收集所有 Resource 的 id
            HashMap<Long, String> allResource = new HashMap<>();
            //ArrayList<String> allResourceId = new ArrayList<>();

            for(Resource resource : resourceRepository.findAll()) {
                //log.error(resource.getNodeId()+resource.getTitle());
                //allResourceId.add(resource.getId().toString()+" "+resource.getTitle());
                allResource.put(resource.getNodeId(), resource.getTitle().replaceAll(","," "));
                /*for(Long key : allResource.keySet()) {
                    log.error(key.toString()+"-"+allResource.get(key));
                }*/
            }

            // 將所有 Swagger Title and NodeId 寫入到 csv first row (不重複)
            sb.append(",");
            for(Long key : allResource.keySet()) {
                sb.append("'"+key.toString()+"-"+allResource.get(key)+"'" + ",");
            }
            log.error(String.valueOf(sb.length()));
            sb.append("\n");
            pw.write(sb.toString());
            sb.delete(0, sb.length());


            // catch all swagger in neo4j
            for(Long currentId : allResource.keySet()) {
                double sumScore = 0.0;

                Resource currentResource = resourceRepository.findResourceById(currentId);
                sb.append(currentResource.getTitle().replaceAll(","," ") + ",");

                // compare other swagger cosine similarity
                for(Long id : allResource.keySet()) {
                    log.info("----"+id);
                    Resource compareResource = resourceRepository.findResourceById(id);

                    double resourceScore = 0.0;
                    double operationScore = 0.0;

                    // resource score
                    if(currentResource.getOriginalWord() != null && compareResource.getOriginalWord() != null) {
                        if(!currentResource.getOriginalWord().isEmpty() && !compareResource.getOriginalWord().isEmpty()) {
                            resourceScore = resourceScore + calculateTwoMatrixVectorsAndCosineSimilarity(currentResource.getOriginalWord(),compareResource.getOriginalWord());
                        }
                    }

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

                    if(!currentOperationLDA.isEmpty() && !compareOperationLDA.isEmpty()) {
                        operationScore = operationScore + calculateTwoMatrixVectorsAndCosineSimilarity(currentOperationLDA, compareOperationLDA);
                    }
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

                    sb.append(str + ",");
                }

                sb.append("\n");
                pw.write(sb.toString());
                sb.delete(0, sb.length());
            }
            pw.close();

        } catch (FileNotFoundException e) {
            log.info("File not found :{}", e.toString());
        } catch (Exception e) {

            log.info("Error :{}", e.toString());
        }

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
