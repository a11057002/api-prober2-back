package ntou.soselab.swagger.engine;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

public class EngineTokenizationAndStemming {

    Logger log = LoggerFactory.getLogger(EngineTokenizationAndStemming.class);


    public String stemTermsAndSaveOriginalTerm(String term, HashMap<String, String> table) throws IOException {
        log.info("- stemming for Swagger Information");
        StringBuilder builder = new StringBuilder();
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        TokenStream tokenStream = null;
        boolean x = true;
        for (String w : term.split(" +")) {
            w = w.replaceAll("-", ""); // 這邊是先避免掉有類似符號出現 例如:real-time
            tokenStream = analyzer.tokenStream("content", new StringReader(w));
            tokenStream.reset();
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()) {
                String t = new String(attr.buffer(), 0, attr.length()); //抓取斷詞完結果 不過是lucene分析後的斷詞
                log.info("-- {} --> {}", t, w);
                table.put(t, w);
                builder.append(t);
                builder.append(" ");
            }
            tokenStream.close();
        }
        analyzer.close();
        return builder.toString().trim();
    }

}
