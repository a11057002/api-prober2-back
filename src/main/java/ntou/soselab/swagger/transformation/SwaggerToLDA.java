package ntou.soselab.swagger.transformation;

import ntou.soselab.swagger.algo.LDA;
import ntou.soselab.swagger.algo.TokenizationAndStemming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SwaggerToLDA {

    Logger log = LoggerFactory.getLogger(SwaggerToLDA.class);

    TokenizationAndStemming tokenizationAndStemming = new TokenizationAndStemming();

    private int LDATopicNumber = 4;

    public ArrayList swaggerParseLDA(String[] swaggerInfo, HashMap<String, String> stemmingAndTermsTable) throws IOException {

        LDA lda = new LDA();
        // For filtering out repeated concept in original concept list and wordnet concept list
        HashMap<String, Boolean> filterRepeatedTerm = new HashMap<String, Boolean>();

        // pre-processing for inputs on LDA
        log.info("pre-processing for inputs on LDA");

        for (int i = 0; i < swaggerInfo.length; i++) {
            String terms = change_ToSeperateTerms(
                    changeDotsToSeperateTerms(changeCamelWordsToSeperateTerms(replaceTagsToNone(swaggerInfo[i]))));
            swaggerInfo[i] = tokenizationAndStemming.stemTermsAndSaveOriginalTerm(terms, stemmingAndTermsTable);
            //writeTxt.inputTxt("['"+ResourceConcept[i]+"'],");
            log.info(" -- {}", swaggerInfo[i]);
        }

        // Apply LDA and get ArrayList
        ArrayList<String> map = new ArrayList<String>(); // original concepts
        // from LDA

        List<Map<String, Integer>> l = lda.apply(swaggerInfo, LDATopicNumber);
        for(Map<String, Integer> temp :l){
            int index = 1;
            log.info("---- LDA Topic response ----");
            for (String key : temp.keySet()) {
                if (!filterRepeatedTerm.containsKey(key)) {
                    log.info(key);
                    map.add(key);
                    filterRepeatedTerm.put(key, new Boolean(true));
                    if (index < 2) {
                        index++;
                    } else {
                        break;
                    }
                }
            }
        }

        log.info("RC LDA 結果:{}", map); //RC LDA結果
        return map;
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
