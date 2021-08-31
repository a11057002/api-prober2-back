package ntou.soselab.swagger.algo;

import ntou.soselab.swagger.neo4j.domain.service.Parameter;
import ntou.soselab.swagger.neo4j.domain.service.Response;
import org.neo4j.ogm.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class ParametersSimilarityOfTwoServices {

    @Autowired
    VSMScore vsmScore;

    Logger log = LoggerFactory.getLogger(ParametersSimilarityOfTwoServices.class);

    // HMS: start point = 以其parameter為主(Parameter也會回傳)
    public double calculateServiceInputScore(ArrayList<Parameter> startPointService,
                                             ArrayList<Parameter> service2) {
        // log.info("----calculate Service Inputs Score -- start point: {}", request.getResourceOperationPairs().getOperation().getPath());

        // 沒有必要之參數
        if(startPointService.size() == 0) {
            return 0.0;
        }

        for(Parameter p : new ArrayList<Parameter>(startPointService)){
            log.info("-- {}", p.getOriginalWord());
            log.info("1--ParameterAndParameterConcept的ArrayList裡面的startPointService: {}", p.getOriginalWord());
        }

        for(Parameter p :new ArrayList<Parameter>(service2)){
            log.info("-- {}", p.getOriginalWord());
            log.info("2--ParameterAndParameterConcept的ArrayList裡面的startPointService: {}", p.getOriginalWord());
        }

        double[][] SQSimilarityMatrix = null;
        SQSimilarityMatrix = calculateInputServiceScoreByService1AsStartPoint(startPointService, service2);
        HungarianAlgorithm findBestIOMapping = new HungarianAlgorithm(SQSimilarityMatrix);
        int[] mappingResult = findBestIOMapping.execute();
        //印出input配對結果
        log.info("----hungarium mapping result: {}", mappingResult);

        return this.calculateInputHungarianMappingScore(SQSimilarityMatrix, mappingResult, startPointService, service2);
    }

    // 計算service response和User output cache的Hungarian score
    public double calculateServiceOutputScore(ArrayList<Response> startPointService, ArrayList<Response> service2){
        // 沒有必要之參數
        if(startPointService.size() == 0) {
            return 0.0;
        }

        log.info("----calculate Service Outputs Score -- start point:");
        for(Response p :new ArrayList<Response>(startPointService)){
            log.info("-- {}", p.getOriginalWord());
        }
        log.info("----calculate Service Outputs Score -- service:");
        for(Response p :new ArrayList<Response>(service2)){
            log.info("-- {}", p.getOriginalWord());
        }

        double[][] SQSimilarityMatrix = null;
        SQSimilarityMatrix = calculateResponseServiceScoreByService1AsStartPoint(startPointService,service2);
        HungarianAlgorithm findBestIOMapping = new HungarianAlgorithm(SQSimilarityMatrix);
        int[] mappingResult = findBestIOMapping.execute();
        //印出output配對結果
        log.info("----hungarium mapping result: {}", mappingResult);

        return this.calculateOutputHungarianMappingScore(SQSimilarityMatrix, mappingResult, startPointService, service2);
    }

    // 計算 input output 的 Hungarian score
    public double calculateServiceInputOutputScore(ArrayList<Parameter> startPointService, ArrayList<Response> service2){
        // 沒有必要之參數
        if(startPointService.size() == 0) {
            return 0.0;
        }

        log.info("----calculate Service Outputs Score -- start point:");
        for(Parameter p :new ArrayList<Parameter>(startPointService)){
            log.info("-- {}", p.getOriginalWord());
        }
        log.info("----calculate Service Outputs Score -- service:");
        for(Response p :new ArrayList<Response>(service2)){
            log.info("-- {}", p.getOriginalWord());
        }

        double[][] SQSimilarityMatrix = null;
        SQSimilarityMatrix = calculateParameterResponseServiceScoreByService1AsStartPoint(startPointService,service2);
        HungarianAlgorithm findBestIOMapping = new HungarianAlgorithm(SQSimilarityMatrix);
        int[] mappingResult = findBestIOMapping.execute();
        //印出input配對結果
        log.info("----hungarium mapping result: {}", mappingResult);

        return this.calculateInputOutputHungarianMappingScore(SQSimilarityMatrix, mappingResult, startPointService, service2);
    }

    // 計算service response和User output cache的Hungarian score
    public double calculateServiceOutputInputScore(ArrayList<Response> startPointService, ArrayList<Parameter> service2){
        // 沒有必要之參數
        if(startPointService.size() == 0) {
            return 0.0;
        }

        log.info("----calculate Service Outputs Score -- start point:");
        for(Response p :new ArrayList<Response>(startPointService)){
            log.info("-- {}", p.getOriginalWord());
        }
        log.info("----calculate Service Outputs Score -- service:");
        for(Parameter p :new ArrayList<Parameter>(service2)){
            log.info("-- {}", p.getOriginalWord());
        }

        double[][] SQSimilarityMatrix = null;
        SQSimilarityMatrix = calculateResponseParameterServiceScoreByService1AsStartPoint(startPointService,service2);
        HungarianAlgorithm findBestIOMapping = new HungarianAlgorithm(SQSimilarityMatrix);
        int[] mappingResult = findBestIOMapping.execute();
        //印出input配對結果
        log.info("----hungarium mapping result: {}", mappingResult);

        return this.calculateOutputInputHungarianMappingScore(SQSimilarityMatrix, mappingResult, startPointService, service2);
    }

    public double[][] calculateInputServiceScoreByService1AsStartPoint(ArrayList<Parameter> service1,
                                                                       ArrayList<Parameter> service2) {
        //log.info(" service1 is start points...");
        int row = 0;
        int col = 0;
        double[][] outputQSSimilarityMatrix = new double[service1.size()][service2.size()];
        try{
            for (Parameter sp1 : new ArrayList<Parameter>(service1)) { // 以service1當star
                // point
                for (Parameter sp2 : new ArrayList<Parameter>(service2)) {
                    calculateOneOnOneParameterScore(row, col, outputQSSimilarityMatrix, sp1.getOriginalWord(),
                            sp2.getOriginalWord(), sp2.getWordnetWord());
                    log.info("sp1.getParameterConcept():{},sp2.getParameterConcept():{}",sp1.getOriginalWord(), sp2.getOriginalWord());
                    col++;
                }
                col = 0;
                row++;
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            log.info("-- array out of bounds on row: {}, col: {}",row, col);
        }
        return outputQSSimilarityMatrix;
    }

    public double[][] calculateResponseServiceScoreByService1AsStartPoint(ArrayList<Response> service1,
                                                                          ArrayList<Response> service2) {
        //log.info(" service1 is start points...");
        int row = 0;
        int col = 0;
        double[][] outputQSSimilarityMatrix = new double[service1.size()][service2.size()];
        try{
            for (Response sp1 : new ArrayList<Response>(service1)) { // 以service1當star
                // point
                for (Response sp2 : new ArrayList<Response>(service2)) {
                    calculateOneOnOneParameterScore(row, col, outputQSSimilarityMatrix, sp1.getOriginalWord(),
                            sp2.getOriginalWord(), sp2.getWordnetWord());
                    log.info("sp1.getParameterConcept():{},sp2.getParameterConcept():{}",sp1.getOriginalWord(), sp2.getOriginalWord());
                    col++;
                }
                col = 0;
                row++;
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            log.info("-- array out of bounds on row: {}, col: {}",row, col);
        }
        return outputQSSimilarityMatrix;
    }

    public double[][] calculateParameterResponseServiceScoreByService1AsStartPoint(ArrayList<Parameter> service1,
                                                                                   ArrayList<Response> service2) {
        //log.info(" service1 is start points...");
        int row = 0;
        int col = 0;
        double[][] outputQSSimilarityMatrix = new double[service1.size()][service2.size()];
        try{
            for (Parameter sp1 : new ArrayList<Parameter>(service1)) { // 以service1當star
                // point
                for (Response sp2 : new ArrayList<Response>(service2)) {
                    calculateOneOnOneParameterScore(row, col, outputQSSimilarityMatrix, sp1.getOriginalWord(),
                            sp2.getOriginalWord(), sp2.getWordnetWord());
                    log.info("sp1.getParameterConcept():{},sp2.getParameterConcept():{}",sp1.getOriginalWord(), sp2.getOriginalWord());
                    col++;
                }
                col = 0;
                row++;
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            log.info("-- array out of bounds on row: {}, col: {}",row, col);
        }
        return outputQSSimilarityMatrix;
    }

    public double[][] calculateResponseParameterServiceScoreByService1AsStartPoint(ArrayList<Response> service1,
                                                                                   ArrayList<Parameter> service2) {
        //log.info(" service1 is start points...");
        int row = 0;
        int col = 0;
        double[][] outputQSSimilarityMatrix = new double[service1.size()][service2.size()];
        try{
            for (Response sp1 : new ArrayList<Response>(service1)) { // 以service1當star
                // point
                for (Parameter sp2 : new ArrayList<Parameter>(service2)) {
                    calculateOneOnOneParameterScore(row, col, outputQSSimilarityMatrix, sp1.getOriginalWord(),
                            sp2.getOriginalWord(), sp2.getWordnetWord());
                    log.info("sp1.getParameterConcept():{},sp2.getParameterConcept():{}",sp1.getOriginalWord(), sp2.getOriginalWord());
                    col++;
                }
                col = 0;
                row++;
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            log.info("-- array out of bounds on row: {}, col: {}",row, col);
        }
        return outputQSSimilarityMatrix;
    }

    public void calculateOneOnOneParameterScore(int row, int col, double[][] inputForSQSimilarityMatrix,
                                                ArrayList<String> parameterOriginal1, ArrayList<String> parameterOriginal2, ArrayList<String> parameterWordnet2) throws ArrayIndexOutOfBoundsException{

        double maxScore = 0.0;

        double score = 0.0;
        try {
            score = vsmScore.parameterConceptScore(parameterOriginal1, parameterOriginal2, parameterWordnet2);
            log.info("similarity score of concepts {} and {} = {}", parameterOriginal1, parameterOriginal2, score);
            if (maxScore < score) {
                maxScore = score;
            }
        } catch (JSONException | IOException e) {
            log.error("vsm score error", e);
        }
        inputForSQSimilarityMatrix[row][col] = 0 - maxScore; // Hungarian
        // 為找最小權重配對,這裡我們要找權重最大者
        // 所以 0-maxScore
    }

    public double calculateInputHungarianMappingScore(double[][] SQSimilarityMatrix, int[] mappingResult,
                                                      ArrayList<Parameter> service1, ArrayList<Parameter> service2) {
        // sIn matching qIn 儲存最佳組至iom,與分數
        double resultScore = 0.0;
        for (int i = 0; i < mappingResult.length; i++) {
            Parameter sIn = service1.get(i);
            int mappingNum = mappingResult[i];
            if (mappingNum != -1) { // mappingNum ==-1 ,表該參數沒配對到
                Parameter mappingParameter = service2.get(mappingNum);
                double score = SQSimilarityMatrix[i][mappingNum];
                // 分數匹配太低
                if(Math.abs(score) < 0.4) {
                    return 0.0;
                }
                log.info("successful on mapping: {} with {} and {}", sIn.getOriginalWord(),
                        mappingParameter.getOriginalWord(), mappingParameter.getWordnetWord());
                resultScore += score;
            }else {
                log.info("fail on mapping: {}", sIn.getOriginalWord());
                return 0.0;
            }
        }
        resultScore = 0 - (resultScore / (mappingResult.length));
        log.info("pure Hungarium Mapping Score: {}, which is divided by {}", resultScore, mappingResult.length);

        return resultScore;
    }

    public double calculateOutputHungarianMappingScore(double[][] SQSimilarityMatrix, int[] mappingResult,
                                                       ArrayList<Response> service1, ArrayList<Response> service2) {
        // sIn matching qIn 儲存最佳組至iom,與分數
        double resultScore = 0.0;
        for (int i = 0; i < mappingResult.length; i++) {
            Response sIn = service1.get(i);
            int mappingNum = mappingResult[i];
            if (mappingNum != -1) { // mappingNum ==-1 ,表該參數沒配對到
                Response mappingParameter = service2.get(mappingNum);
                double score = SQSimilarityMatrix[i][mappingNum];
                // 分數匹配太低
                if(Math.abs(score) < 0.4) {
                    return 0.0;
                }
                log.info("successful on mapping: {} with {} and {}", sIn.getOriginalWord(),
                        mappingParameter.getOriginalWord(), mappingParameter.getWordnetWord());
                resultScore += score;
            }else {
                log.info("fail on mapping: {}", sIn.getOriginalWord());
                return 0.0;
            }
        }
        resultScore = 0 - (resultScore / (mappingResult.length));
        log.info("pure Hungarium Mapping Score: {}, which is divided by {}", resultScore, mappingResult.length);

        return resultScore;
    }

    public double calculateInputOutputHungarianMappingScore(double[][] SQSimilarityMatrix, int[] mappingResult,
                                                            ArrayList<Parameter> service1, ArrayList<Response> service2) {
        // sIn matching qIn 儲存最佳組至iom,與分數
        double resultScore = 0.0;
        for (int i = 0; i < mappingResult.length; i++) {
            Parameter sIn = service1.get(i);
            int mappingNum = mappingResult[i];
            if (mappingNum != -1) { // mappingNum ==-1 ,表該參數沒配對到
                Response mappingParameter = service2.get(mappingNum);
                double score = SQSimilarityMatrix[i][mappingNum];
                // 分數匹配太低
                if(Math.abs(score) < 0.4) {
                    return 0.0;
                }
                log.info("successful on mapping: {} with {} and {}", sIn.getOriginalWord(),
                        mappingParameter.getOriginalWord(), mappingParameter.getWordnetWord());
                resultScore += score;
            }else {
                log.info("fail on mapping: {}", sIn.getOriginalWord());
                return 0.0;
            }
        }
        resultScore = 0 - (resultScore / (mappingResult.length));
        log.info("pure Hungarium Mapping Score: {}, which is divided by {}", resultScore, mappingResult.length);

        return resultScore;
    }

    public double calculateOutputInputHungarianMappingScore(double[][] SQSimilarityMatrix, int[] mappingResult,
                                                            ArrayList<Response> service1, ArrayList<Parameter> service2) {
        // sIn matching qIn 儲存最佳組至iom,與分數
        double resultScore = 0.0;
        for (int i = 0; i < mappingResult.length; i++) {
            Response sIn = service1.get(i);
            int mappingNum = mappingResult[i];
            if (mappingNum != -1) { // mappingNum ==-1 ,表該參數沒配對到
                Parameter mappingParameter = service2.get(mappingNum);
                double score = SQSimilarityMatrix[i][mappingNum];
                // 分數匹配太低
                if(Math.abs(score) < 0.4) {
                    return 0.0;
                }
                log.info("successful on mapping: {} with {} and {}", sIn.getOriginalWord(),
                        mappingParameter.getOriginalWord(), mappingParameter.getWordnetWord());
                resultScore += score;
            }else {
                log.info("fail on mapping: {}", sIn.getOriginalWord());
                return 0.0;
            }
        }
        resultScore = 0 - (resultScore / (mappingResult.length));
        log.info("pure Hungarium Mapping Score: {}, which is divided by {}", resultScore, mappingResult.length);

        return resultScore;
    }
}