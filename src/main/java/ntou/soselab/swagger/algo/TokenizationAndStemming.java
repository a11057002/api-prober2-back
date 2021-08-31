package ntou.soselab.swagger.algo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TokenizationAndStemming {

    Logger log = LoggerFactory.getLogger(TokenizationAndStemming.class);

    final List<String> stopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if",
            "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there",
            "these", "they", "this", "to", "was", "will", "with", "part", "http", "put", "swagger", "set",
            "up", "url", "url2", "get", "api", "api2", "long", "understand", "app", "add", "people", "provide", "provid",
            "start", "restful", "tful", "make", "www", "run", "html", "servic", "endpoint", "user", "feed",
            "apitor", "googl", "cloud", "tier", "stackdriv", "android", "base","com", "href", "link", "call", "src",
            "report", "rang", "object", "search", "public", "open", "return", "adyen", "amazon", "aws", "azur", "microsoft", "echo", "variou",
            "web", "page", "manag", "list");

    public String stemTermsAndSaveOriginalTerm(String term, HashMap<String, String> table) throws IOException {
        log.info("- stemming for LDA");
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
                x = true; //必須重設, 不然只要有一個false, 其他就無法進入
                String t = new String(attr.buffer(), 0, attr.length()); //抓取斷詞完結果 不過是lucene分析後的斷詞
                log.info("-- {} --> {}", t, w);
                table.put(t, w);
                for(String s : stopWords) { //這邊是我自己寫的 比對stopword

                    if(s.equals(w) || s.equals(t)) {
                        x = false;
                        log.info("丟棄:{}", w);
                        break;
                    }
                }

                if(x == true) {
                    builder.append(t);
                    builder.append(" ");
                }
            }
            tokenStream.close();
        }
        analyzer.close();
        return builder.toString().trim();
    }

    public ArrayList<String> applyArrayListOnStemming(ArrayList<String> inputs) {

        HashMap<String, Boolean> repeated = new HashMap<String, Boolean>();
        ArrayList<String> tokens = new ArrayList<String>();
        // Define your attribute factory (or use the default) - same between 4.x
        // and 5.x
        Analyzer analyzer = new StopAnalyzer();
        TokenStream tokenStream;
        try {
            tokenStream = analyzer.tokenStream("contents",
                    new StringReader(this.changeDotsToSeperateTerms(this.replaceTagsToNone(String.join(" ", inputs)))));
            tokenStream.reset();

            // Then process tokens - same between 4.x and 5.x
            TokenStream stemTerm = new PorterStemFilter(tokenStream);
            CharTermAttribute attr = stemTerm.addAttribute(CharTermAttribute.class);

            while (stemTerm.incrementToken()) {
                // Grab the term
                String term = new String(attr.buffer(), 0, attr.length());
                if (!repeated.containsKey(term)) {
                    tokens.add(term);
                    repeated.put(term, true);
                }
            }
            tokenStream.end();
            stemTerm.end();
        } catch (IOException e) {
            log.error("Error on parsing Tokenization And Stemming", e);
        }
        return tokens;

    }

    public String changeDotsToSeperateTerms(String input) {
        return input.replaceAll("\\.", " ").trim();
    }

    public String replaceTagsToNone(String input) {
        return input.replaceAll("<.*?>", " ").trim();
    }
}
