package ntou.soselab.swagger.swagger;

import ntou.soselab.swagger.algo.CosineSimilarity;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Parameter;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.domain.service.Response;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ParameterRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResponseRepository;
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
public class ClusteringSwaggerCSV {
    Logger log = LoggerFactory.getLogger(ClusteringSwaggerCSV.class);

    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    OperationRepository operationRepository;
    @Autowired
    ParameterRepository parameterRepository;
    @Autowired
    ResponseRepository responseRepository;

    CosineSimilarity cosineSimilarity = new CosineSimilarity();

    //@Test
    public void collection_Resource_Operation_Parameter_Response(double wordnetScore, int resourceP, int operationP) {
        try {
            PrintWriter pw = new PrintWriter(new File("C:/Users/surpr/Desktop/real-100-ClusterLDA/"+Double.toString(wordnetScore)+"/LDA-16/LDA-not-InputOutput-"+String.valueOf(resourceP)+String.valueOf(operationP)+".csv"));
            StringBuilder sb = new StringBuilder();

            // 收集所有 Resource 的 id
            HashMap<Long, String> allResource = new HashMap<>();
            //ArrayList<String> allResourceId = new ArrayList<>();

            for(Resource resource : resourceRepository.findAll()) {
                //allResourceId.add(resource.getId().toString()+" "+resource.getTitle());
                allResource.put(resource.getNodeId(), resource.getTitle().replaceAll(","," "));
            }

            // 將所有 Swagger Title and NodeId 寫入到 csv first row (不重複)
            sb.append(",");
            for(Long key : allResource.keySet()) {
                sb.append("'"+key.toString()+"-"+allResource.get(key)+"'" + ",");
            }
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
                        // (resourceScore * 0.1) (parameterScore * 0.2) (responseScore * 0.2) (operationScore * 0.4)
                        double x = Double.valueOf(resourceP)/10;
                        double y = Double.valueOf(operationP)/10;
                        sumScore = 1-((resourceScore * x) + (operationScore * y));
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

    //@Test
    public void calculateTwoMatrixVectorsTest() {
        ArrayList<String> str1 = new ArrayList<>();
        ArrayList<String> str2 = new ArrayList<>();
        str1.add("trash");
        str1.add("trash");
        str1.add("page");
        str1.add("trashnoth");
        str1.add("regist");
        str1.add("develop");
        str2.add("vestorli");
        str2.add("develop");
        System.out.println(calculateTwoMatrixVectorsAndCosineSimilarity(str1, str2));
    }

    //@Test
    public void setProportion() {
        double[] wordnetList = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        for(double wordnetScore : wordnetList) {
            int resource = 10;
            int operation = 0;

            while(resource >= 0) {
//            double x = Double.valueOf(resource)/10;
//            double y = Double.valueOf(operation)/10;
//            log.info("resource :{}", x);
//            log.info("operation :{}", y);
                collection_Resource_Operation_Parameter_Response(wordnetScore, resource, operation);
                resource--;
                operation++;
            }
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
