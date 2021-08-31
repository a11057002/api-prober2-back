package ntou.soselab.swagger.algo;

import org.neo4j.ogm.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class VSMScore {
    Logger log = LoggerFactory.getLogger(VSMScore.class);

    // 給ParameterConcept之間的VSM
    public double parameterConceptScore(ArrayList<String> parameterOriginal1, ArrayList<String> parameterOriginal2, ArrayList<String> parameterWordnet2) throws IOException, JSONException {

        // 若有參數為空，不論 比對 或 被比對，PCS 為 0
        if(parameterOriginal1 == null || parameterOriginal1.isEmpty()) {
            return 0.0;
        }

        if(parameterOriginal2 == null || parameterOriginal2.isEmpty()) {
            return 0.0;
        }

        HashMap<String, Token> originalTokensForPc1 = changeStringArrayToTfToken(parameterOriginal1);
        HashMap<String, Token> originalTokensForPc2 = changeStringArrayToTfToken(parameterOriginal2);
        HashMap<String, Token> wordnetTokensForPc2 = changeStringArrayToTfToken(parameterWordnet2);

        double originalDot = dotQS(new ArrayList<Token>(originalTokensForPc2.values()), originalTokensForPc1);
        double wordnetDot = dotQS(new ArrayList<Token>(wordnetTokensForPc2.values()), originalTokensForPc1);

        double allDot = originalDot + wordnetDot;
        // 取 Math.sqrt(p1.originalConcepts**2 + p2.originalConcept**2) 當分母
        double score = allDot / dotVectors(new ArrayList<Token>(originalTokensForPc1.values()), new ArrayList<Token>(originalTokensForPc2.values()));

        BigDecimal b = new BigDecimal(score);
        score = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue(); // 取第三位

        return score;
    }

    // 兩向量內積
    public double dotQS(ArrayList<Token> qV, HashMap<String, Token> sV) {
        double docValue = 0;
        for (Token qT : qV) {
            if (sV.containsKey(qT.getToken())) {// 如果有matching到的話再計算
                Token sToken = sV.get(qT.getToken());
                double qTW = 1 + Math.log10(qT.getTf());
                // log.info("--- match {} as score: {}", qT.getToken(), sToken.getTf() * qTW);
                docValue += sToken.getTf() * qTW;
            }
        }
        return docValue;
    }

    public HashMap<String, Token> changeStringArrayToTfToken(ArrayList<String> terms) {
        HashMap<String, Token> tokens = new HashMap<String, Token>();
        for (String term : terms) {
            if (!tokens.containsKey(term)) {
                Token t = new Token(term, 1);
                tokens.put(term, t);
            } else {
                tokens.get(term).setTf(tokens.get(term).getTf() + 1);
            }
        }
        return tokens;

    }

    public double dotVectors(ArrayList<Token> original, ArrayList<Token> userConcept){
        return (Math.sqrt(dotSelf(original)) * Math.sqrt(dotSelf(userConcept)));
    }

    public double dotSelf(ArrayList<Token> vector) {
        double powValue = 0;
        for (Token token : vector) {
            double weight = 1 + Math.log10(token.getTf());
            powValue += Math.pow(weight, 2);
        }
        return powValue;
    }

}
