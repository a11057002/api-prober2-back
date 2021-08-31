package ntou.soselab.swagger.web.recommand;

import ntou.soselab.swagger.algo.CosineSimilarity;
import ntou.soselab.swagger.algo.ParametersSimilarityOfTwoServices;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Parameter;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.domain.service.Response;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ParameterRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResponseRepository;
import org.neo4j.ogm.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ServiceRecommender {

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    OperationRepository operationRepository;

    @Autowired
    ParameterRepository parameterRepository;

    @Autowired
    ResponseRepository responseRepository;

    @Autowired
    ParametersSimilarityOfTwoServices parametersSimilarityOfTwoServices;

    CosineSimilarity cosineSimilarity = new CosineSimilarity();

    private double wordnetScore = 0.9;
    private double operationThreshold = 0.2;

    Logger log = LoggerFactory.getLogger(ServiceRecommender.class);


    public String runRecommendService(Long resourceId) {

        ExecutorService executor = Executors.newFixedThreadPool(200);

        ServiceRecommendation service = new ServiceRecommendation();
        ArrayList<Similarity> similaritys = new ArrayList<>();
        ArrayList<Mashup> mashups = new ArrayList<>();

        // 獲得當前服務之相關操作
        for(Operation operation1 : operationRepository.findOperationsByResource(resourceId)) { //7081
            ArrayList<Parameter> parameters1 = new ArrayList<>(parameterRepository.findParametersByOperation(operation1.getNodeId()));
            ArrayList<Response> responses1 = new ArrayList<>(responseRepository.findSuccessResponsesByOperation(operation1.getNodeId()));

            // 獲得相同群集內其他服務
            Resource resource = resourceRepository.findResourceById(resourceId);
            for(Resource resource1 : resourceRepository.findResourcesBySameCluster(resource.getClusterGroup())) {

                // 計算當前服務與被比較服務之操作
                for(Operation operation2 : operationRepository.findOperationsByResource(resource1.getNodeId())) {

                    // 判斷非同一個服務
                    if(!resource.getNodeId().equals(resource1.getNodeId())) {
                        if(getOperationCSScore(operation1, operation2)) {
                            Thread thread = new CalculationThread(similaritys, mashups, operation1, operation2, parameters1, responses1, operationRepository, parameterRepository, responseRepository, parametersSimilarityOfTwoServices);
                            executor.execute(thread);
                        }else {
                            log.info("Operation CS too low");
                        }
                    }

                }

            }
        }

        executor.shutdown();
        try {
            if(executor.awaitTermination(1, TimeUnit.MINUTES)) {
                service.setSimilaritys(sortSimilaritysRank(similaritys));
                service.setMashups(sortMashupsRank(mashups));
                JSONObject jsonObjectMary = new JSONObject(service);
                log.info("ans :{}",jsonObjectMary);
                return jsonObjectMary.toString();
            }else {
                executor.shutdownNow();
                service.setSimilaritys(sortSimilaritysRank(similaritys));
                service.setMashups(sortMashupsRank(mashups));
                JSONObject jsonObjectMary = new JSONObject(service);
                log.info("事先回傳 ans :{}",jsonObjectMary);
                return jsonObjectMary.toString();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 比較服務間操作的相似度 高於門檻才會進行操作配對
    public boolean getOperationCSScore(Operation operation1, Operation operation2) {
        double operationScore = 0.0;

        if(operation1.getOriginalWord() != null && operation2.getOriginalWord() != null) {
            if(!operation1.getOriginalWord().isEmpty() && !operation2.getOriginalWord().isEmpty()) {
                operationScore = operationScore + calculateTwoMatrixVectorsAndCosineSimilarity(operation1.getOriginalWord(),operation2.getOriginalWord());
            }
        }

        if(operation1.getWordnetWord() != null && operation2.getWordnetWord() != null) {
            if(!operation1.getWordnetWord().isEmpty() && !operation2.getWordnetWord().isEmpty()) {

                double s1 = calculateTwoMatrixVectorsAndCosineSimilarity(operation1.getOriginalWord(), operation2.getWordnetWord());
                operationScore = operationScore + (s1 * wordnetScore);
            }
        }

        if(operationScore > operationThreshold) {
            return true;
        }
        return false;
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

    public ArrayList<Similarity> sortSimilaritysRank(ArrayList<Similarity> notSortSimilaritys) {
        Similarity[] similarityArray = new Similarity[notSortSimilaritys.size()];

        for(int x = 0;x<notSortSimilaritys.size();x++) {
            similarityArray[x] = notSortSimilaritys.get(x);
        }

        // 氣泡排序
        for(int x = 0;x < similarityArray.length;x++) {
            for(int y = similarityArray.length-1;y>x;y--) {
                if(similarityArray[y].score >= similarityArray[y-1].score) {
                    Similarity tmp = similarityArray[y-1];
                    similarityArray[y-1] = similarityArray[y];
                    similarityArray[y] = tmp;
                }
            }
        }
        ArrayList<Similarity> sortSimilaritys = new ArrayList<Similarity>(Arrays.asList(similarityArray));

        return sortSimilaritys;
    }

    public ArrayList<Mashup> sortMashupsRank(ArrayList<Mashup> notSortMashups) {
        Mashup[] mashupArray = new Mashup[notSortMashups.size()];

        for(int x = 0;x<notSortMashups.size();x++) {
            mashupArray[x] = notSortMashups.get(x);
        }

        // 氣泡排序
        for(int x = 0;x < mashupArray.length;x++) {
            for(int y = mashupArray.length-1;y>x;y--) {
                if(mashupArray[y].score >= mashupArray[y-1].score) {
                    Mashup tmp = mashupArray[y-1];
                    mashupArray[y-1] = mashupArray[y];
                    mashupArray[y] = tmp;
//                    log.info("交換 :{} --> :{}", mashupArray[y].score, mashupArray[y-1].score);
                }
            }
        }
        ArrayList<Mashup> sortMashups = new ArrayList<Mashup>(Arrays.asList(mashupArray));

        return sortMashups;
    }
}

class CalculationThread extends Thread {

    Logger log = LoggerFactory.getLogger(CalculationThread.class);

    ArrayList<Similarity> similaritys;
    ArrayList<Mashup> mashups;
    Operation operation1;
    Operation operation2;
    ArrayList<Parameter> parameters1;
    ArrayList<Response> responses1;

    OperationRepository operationRepository;
    ParameterRepository parameterRepository;
    ResponseRepository responseRepository;
    ParametersSimilarityOfTwoServices parametersSimilarityOfTwoServices;


    public CalculationThread (ArrayList<Similarity> similaritys, ArrayList<Mashup> mashups, Operation operation1, Operation operation2, ArrayList<Parameter> parameters1, ArrayList<Response> responses1,  OperationRepository operationRepository, ParameterRepository parameterRepository, ResponseRepository responseRepository, ParametersSimilarityOfTwoServices parametersSimilarityOfTwoServices){
        this.similaritys = similaritys;
        this.mashups = mashups;
        this.operation1 = operation1;
        this.operation2 = operation2;
        this.parameters1 = parameters1;
        this.responses1 = responses1;
        this.operationRepository = operationRepository;
        this.parameterRepository = parameterRepository;
        this.responseRepository = responseRepository;
        this.parametersSimilarityOfTwoServices = parametersSimilarityOfTwoServices;
    }

    @Override
    public void run() {
        log.info("operation ID :{}", operation2.getNodeId());
        log.info("parameterRepository FIND :{}", parameterRepository.findParametersByOperation(operation2.getNodeId()));
        ArrayList<Parameter> parameters2 = new ArrayList<>(parameterRepository.findParametersByOperation(operation2.getNodeId()));
        ArrayList<Response> responses2 = new ArrayList<>(responseRepository.findSuccessResponsesByOperation(operation2.getNodeId()));
        //log.info("service1 :{} ---- service2 :{}", operation1.getOperationAction(), operation2.getOperationAction());
        double inputMatchScore = parametersSimilarityOfTwoServices.calculateServiceInputScore(parameters1, parameters2);
        // 計算 C 輸出是否能夠滿足 T 輸入
//        double inputMashupMatchScore = parametersSimilarityOfTwoServices.calculateServiceInputOutputScore(parameters1, responses2);
        // 計算 T 輸出是否能夠滿足 C 輸入
//        double outputMashupMatchScore = parametersSimilarityOfTwoServices.calculateServiceInputOutputScore(parameters2, responses1);
        double similarityMaxScore = 0.0;
        double mashupMaxScore = 0.0;
        log.info("Input Similarity Score :{}", inputMatchScore);
//        log.info("Input Mashup Score :{}", inputMashupMatchScore);
//        log.info("Output Mashup Score :{}", outputMashupMatchScore);

        // 判斷 Input 或是 Output 相似
        int flag = 0;

        // 每個 input 參數皆有配對到，才會去算 output 分數，若不相似則去計算 mashup
        if(inputMatchScore != 0.0) {
            double outputMatchScore = parametersSimilarityOfTwoServices.calculateServiceOutputScore(responses1, responses2);

            // 若 output 都有配對到則放入到 similarity 的結果內，若不相同 則放入到 mashup 的結果內
            if(outputMatchScore != 0.0) {
                double sumScore = (inputMatchScore + outputMatchScore) / 2;
                if (sumScore > similarityMaxScore) {
                    similarityMaxScore = sumScore;
                }
            }else if(outputMatchScore == 0.0) {
                double sumScore = inputMatchScore;
                if (sumScore > mashupMaxScore) {
                    mashupMaxScore = sumScore;
                }
                // input 相同
                flag = 1;
            }
        }else if(inputMatchScore == 0.0) {
            double outputMatchScore = parametersSimilarityOfTwoServices.calculateServiceOutputScore(responses1, responses2);

            if(outputMatchScore != 0.0) {
                double sumScore = outputMatchScore;
                if (sumScore > mashupMaxScore) {
                    mashupMaxScore = sumScore;
                }
                // output 相同
                flag = 2;
            }
        }

        // similarity 分數高於 0.4 則放入結果列表
        if(similarityMaxScore > 0.0) {
            Similarity similarity = new Similarity();
            similarity.setScore(similarityMaxScore);
            similarity.setTargetEndpoint(operationRepository.findPathByOperation(operation1.getNodeId()).getPath());
            similarity.setTargetOperation(operation1.getOperationAction());
            Resource resource2 = operationRepository.findResourceByOperation(operation2.getNodeId());
            similarity.setCompareOASId(resource2.getNodeId());
            similarity.setCompareOASName(resource2.getTitle());
            similarity.setCompareEndpoint(operationRepository.findPathByOperation(operation2.getNodeId()).getPath());
            similarity.setCompareOperation(operation2.getOperationAction());
//            similaritys.add(similarity);
            saveSimilaritys(similarity);
        }

        if(mashupMaxScore > 0.0) {
            Mashup mashup = new Mashup();
            if(flag == 1) {
                mashup.setCategory("Similar Input");
            }else if(flag == 2) {
                mashup.setCategory("Similar Output");
            }
            mashup.setScore(mashupMaxScore);
            mashup.setTargetEndpoint(operationRepository.findPathByOperation(operation1.getNodeId()).getPath());
            mashup.setTargetOperation(operation1.getOperationAction());
            Resource resource2 = operationRepository.findResourceByOperation(operation2.getNodeId());
            mashup.setCompareOASId(resource2.getNodeId());
            mashup.setCompareOASName(resource2.getTitle());
            mashup.setCompareEndpoint(operationRepository.findPathByOperation(operation2.getNodeId()).getPath());
            mashup.setCompareOperation(operation2.getOperationAction());
//            mashups.add(mashup);
            saveMashups(mashup);
        }

//        if(inputMashupMatchScore > 0.0) {
//            Mashup mashup = new Mashup();
//            mashup.setCategory("Input Match");
//            mashup.setScore(inputMashupMatchScore);
//            mashup.setTargetEndpoint(operationRepository.findPathByOperation(operation1.getNodeId()).getPath());
//            mashup.setTargetOperation(operation1.getOperationAction());
//            Resource resource2 = operationRepository.findResourceByOperation(operation2.getNodeId());
//            mashup.setCompareOASId(resource2.getNodeId());
//            mashup.setCompareOASName(resource2.getTitle());
//            mashup.setCompareEndpoint(operationRepository.findPathByOperation(operation2.getNodeId()).getPath());
//            mashup.setCompareOperation(operation2.getOperationAction());
//            mashups.add(mashup);
//            saveMashups(mashup);
//        }
//
//        if(outputMashupMatchScore > 0.0) {
//            Mashup mashup = new Mashup();
//            mashup.setCategory("Output Match");
//            mashup.setScore(outputMashupMatchScore);
//            mashup.setTargetEndpoint(operationRepository.findPathByOperation(operation1.getNodeId()).getPath());
//            mashup.setTargetOperation(operation1.getOperationAction());
//            Resource resource2 = operationRepository.findResourceByOperation(operation2.getNodeId());
//            mashup.setCompareOASId(resource2.getNodeId());
//            mashup.setCompareOASName(resource2.getTitle());
//            mashup.setCompareEndpoint(operationRepository.findPathByOperation(operation2.getNodeId()).getPath());
//            mashup.setCompareOperation(operation2.getOperationAction());
//            mashups.add(mashup);
//            saveMashups(mashup);
//        }
    }

    public void saveSimilaritys(Similarity similarity) {
        synchronized(similaritys) {
            similaritys.add(similarity);
        }
    }

    public void saveMashups(Mashup mashup) {
        synchronized(mashups) {
            mashups.add(mashup);
        }
    }
}
