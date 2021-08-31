package ntou.soselab.swagger.engine;

import ntou.soselab.swagger.algo.CosineSimilarity;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class SearchEngine {

    Logger log = LoggerFactory.getLogger(SearchEngine.class);

    @Autowired
    ResourceRepository resourceRepository;

    EngineTokenizationAndStemming engineTokenizationAndStemming = new EngineTokenizationAndStemming();
    CosineSimilarity cosineSimilarity = new CosineSimilarity();

    public HashMap<String, Double> userQueryMatch(String query) {
        // record result
        HashMap<String, Double> result = new HashMap<>();

        // user query token
        String[] userQuery = getToken(query).split(" ");
        ArrayList<String> uesrWord = new ArrayList<>();
        for(String str : userQuery) {
            uesrWord.add(str);
        }
        log.info("User Query :{}", uesrWord);

        for(Resource resource : resourceRepository.findAll()) {
            // score
            double score = 0.0;
            String swaggerTitle = resource.getTitle();
            String swaggerDescription = resource.getDescription();

            if(swaggerTitle != null) {
                // title query token
                String[] title = getToken(swaggerTitle).split(" ");
                ArrayList<String> titleWord = new ArrayList<>();
                for(String str : title) {
                    titleWord.add(str);
                }
                log.info("Title :{}", titleWord);
                score = score + calculateTwoMatrixVectorsAndCosineSimilarity(uesrWord, titleWord);
            }

            if(swaggerDescription != null) {
                // description token
                String[] description = getToken(swaggerDescription).split(" ");
                ArrayList<String> descriptionWord = new ArrayList<>();
                for(String str : description) {
                    descriptionWord.add(str);
                }
                log.info("Description :{}", descriptionWord);
                score = score + calculateTwoMatrixVectorsAndCosineSimilarity(uesrWord, descriptionWord);
            }

            BigDecimal b = new BigDecimal(score);
            score = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(); // 取第三位

            if(score > 0.0) {
                result.put(String.valueOf(resource.getNodeId()), score);
            }
        }

        return result;
    }

    public String getToken (String originalStatement) {
        try {
            // For saving key: stemming term --> value: original term
            HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();

            String terms = change_ToSeperateTerms(
                    changeDotsToSeperateTerms(changeCamelWordsToSeperateTerms(replaceTagsToNone(originalStatement))));
            originalStatement = engineTokenizationAndStemming.stemTermsAndSaveOriginalTerm(terms, stemmingAndTermsTable);
            //writeTxt.inputTxt("['"+ResourceConcept[i]+"'],");
            log.info(" -- {}", originalStatement);

        } catch (IOException e) {
            log.info(e.toString());
        }
        return originalStatement;
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

    private String replaceTagsToNone(String input) {
        return input.replaceAll("<.*?>", " ").trim();
    }

    private String changeCamelWordsToSeperateTerms(String input) {
        String[] data = input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        StringBuilder builder = new StringBuilder();
        for (String w : data) {
            builder.append(w.toLowerCase());
            builder.append(" ");
        }
        return builder.toString().trim();
    }

    private String changeDotsToSeperateTerms(String input) {
        return input.replaceAll("\\.", " ").trim();
    }

    private String change_ToSeperateTerms(String input) {
        return input.replaceAll("_", " ").trim();
    }
}
