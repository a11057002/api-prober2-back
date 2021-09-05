package ntou.soselab.swagger.algo;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import ntou.soselab.swagger.web.ProberPathConfig;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class WordNetExpansion {

    private ProberPathConfig proberPathConfig;

    private IDictionary dict = null;
    private WordnetStemmer wordNetStemming;
    Logger log = LoggerFactory.getLogger(WordNetExpansion.class);
    TokenizationAndStemming tokenizationAndStemming = new TokenizationAndStemming();

    private String jwnlPropertiesPath = "./src/main/resources/wordnet_config.xml"; // https://github.com/RolandKluge/de.rolandkluge.blog.java.jwnltut/blob/master/src/main/resources/properties.xml.template
    // private String wordNetPath = "/home/andy/Desktop/api-prober/WordNet-3.0/dict"; // "/home/mis101bird/WordNet/dict"; C:\Program Files (x86)\WordNet\2.1\dict /home/mingjen/Documents/WordNet-3.0/dict /Users/xumingjen/WordNet-3.0/dict
    private String wordNetPath;

    private String[] stackOverflowWords = {"entity", "check", "rule", "limit",
            "hold", "control", "restrict", "train", "suppress", "lock", "draw", "thermostat"};

    private double threshold = 0.9;
    File jwiFile;
    File jwnlFile;
    FileInputStream propertiesStream;

    @Autowired
    public WordNetExpansion(ProberPathConfig proberPathConfig) {
        this.proberPathConfig = proberPathConfig;
        wordNetPath = proberPathConfig.wordNetPath;
        jwiFile = new File(wordNetPath);
        jwnlFile = new File(jwnlPropertiesPath);
        try {
            dict = new edu.mit.jwi.Dictionary(jwiFile);
            dict.open();
            propertiesStream = new FileInputStream(jwnlFile);
            JWNL.initialize(propertiesStream);
            wordNetStemming = new WordnetStemmer(dict);
            propertiesStream.close();
        } catch (Exception e) {
            log.error("Constructing WordNetExpansion failure!", e);
        }
    }

    public void openDic() throws IOException {
        if (dict != null) {
            dict.open();
        }
    }

    private boolean isStackOverflowBug(String word) {
        for (String cw : stackOverflowWords) {
            if (cw.equals(word)) {
                return true;
            }
        }
        return false;
    }

    public Hashtable<String, Double> getHypernymsByNounOrVerb(String str, edu.mit.jwi.item.POS pos) {

        Hashtable<String, Double> strScoreHT = new Hashtable<String, Double>();
        List<String> strStemmedList = wordNetStemming.findStems(str, pos);

        String strStemmed = "";
        if (strStemmedList.size() > 0) {
            strStemmed = strStemmedList.get(0);
        } else { // 表示在wordNet中查無此字，且沒有辦法stemming
            return strScoreHT;
        }
        if (!isStackOverflowBug(strStemmed)) {
            try {

                net.didion.jwnl.data.POS type = null;
                if (pos == edu.mit.jwi.item.POS.VERB) {
                    type = net.didion.jwnl.data.POS.VERB;
                } else if (pos == edu.mit.jwi.item.POS.NOUN) {
                    type = net.didion.jwnl.data.POS.NOUN;
                }

                IndexWord iWord = net.didion.jwnl.dictionary.Dictionary.getInstance()
                        .getIndexWord(type, strStemmed);

                if (iWord != null) {
                    for (Synset s : iWord.getSenses()) {
                        Synset nowSynset = s; // 同義字集
                        List l = PointerUtils.getInstance().getHypernymTree(nowSynset).toList();
                        for (Object o : l) {

                            PointerTargetNodeList pt = (PointerTargetNodeList) o;
                            int depth = pt.size();
                            int length = 0;
                            for (Object o1 : pt) {
                                PointerTargetNode ptn = (PointerTargetNode) o1;

                                Synset ps = ptn.getSynset();
                                --depth;

                                for (Word w : ps.getWords()) {

                                    double simScore = similarityFormula(length, depth);

                                    threshold = 0.9;
                                    if (simScore >= threshold) {
                                        if (!w.getLemma().equals(strStemmed)) {
                                            String tmpLemma = w.getLemma();
                                            String lemma = tmpLemma;
                                            /*
                                             * String[] tmpLemmaSplit =
                                             * tmpLemma.split("[_]"); String
                                             * lemma ="";
                                             * if(tmpLemmaSplit.length>0){
                                             * //會有複合字的情況name_and_address，把它合起來!
                                             * for(String tmpStr :
                                             * tmpLemmaSplit)
                                             * lemma+=tmpStr.toLowerCase();
                                             * }else{ lemma =
                                             * tmpLemmaSplit[0].toLowerCase(); }
                                             */

                                            if (!strScoreHT.containsKey(lemma)) {
                                                strScoreHT.put(lemma, simScore);
                                            } else { // 如果有重複的字，則查看simScore是否比原本在hashtable裡面的大，如果比較大則替換掉；如果沒有，則不動。
                                                double value = strScoreHT.get(lemma);
                                                if (simScore > value)
                                                    strScoreHT.put(lemma, simScore);
                                            }
                                        }
                                    }
                                }
                                length++;
                            }
                        }
                    }
                }
                // return strScoreHT;
            } catch (Exception e) {
                log.error("Error from WordNetExpansion.getHypernymsByNoun!!!", e);
            }
        }
        return strScoreHT;
    }

    public Hashtable<String, Double> getHyponymsByNounOrVerb(String str, edu.mit.jwi.item.POS pos) {
        Hashtable<String, Double> strScoreHT = new Hashtable<String, Double>();
        List<String> strStemmedList = wordNetStemming.findStems(str, pos);
        String strStemmed = "";
        if (strStemmedList.size() > 0) {
            strStemmed = strStemmedList.get(0);
        } else { // 表示在wordNet中查無此字，且沒有辦法stemming
            return strScoreHT;
        }
        if (!isStackOverflowBug(strStemmed)) {
            try {
                net.didion.jwnl.data.POS type = null;
                if (pos == edu.mit.jwi.item.POS.VERB) {
                    type = net.didion.jwnl.data.POS.VERB;
                } else if (pos == edu.mit.jwi.item.POS.NOUN) {
                    type = net.didion.jwnl.data.POS.NOUN;
                }

                IndexWord iWord = net.didion.jwnl.dictionary.Dictionary.getInstance()
                        .getIndexWord(type, strStemmed);
                // System.out.println(iWord.getSenseCount());
                // 先算各個Synset到root(entity)的深度
                if (iWord != null) {
                    ArrayList<Integer> synsetDepthList = new ArrayList<Integer>();
                    for (Synset s : iWord.getSenses()) {
                        Synset sourceSynset = s;

                        List l = PointerUtils.getInstance().getHypernymTree(sourceSynset).toList();
                        for (Object o : l) {
                            PointerTargetNodeList pt = (PointerTargetNodeList) o;
                            int depth = 0;
                            // int length = 0;
                            for (Object o1 : pt) {
                                PointerTargetNode ptn = (PointerTargetNode) o1;
                                Synset ps = ptn.getSynset();
                                depth++;

                            }
                            synsetDepthList.add((--depth));
                        }
                    }

                    // 再開始探索hyponyms

                    // IndexWord iWord =
                    // net.didion.jwnl.dictionary.Dictionary.getInstance().getIndexWord(net.didion.jwnl.data.POS.NOUN,
                    // strStemmed);
                    // System.out.println(iWord.getSenseCount());
                    for (Synset s : iWord.getSenses()) {
                        Synset sourceSynset = s; // 同義字集

                        // IndexWord tIWord =
                        // Dictionary.getInstance().getIndexWord(POS.NOUN,
                        // "abstract entity");

                        // Relationship r =
                        // RelationshipFinder.getInstance().findRelationships(sourceSynset,
                        // tIWord.getSense(1) ,
                        // PointerType.HYPERNYM).getShallowest();
                        List l = PointerUtils.getInstance().getHyponymTree(sourceSynset).toList();
                        int depth2RootIndex = 0;
                        for (Object o : l) {
                            PointerTargetNodeList pt = (PointerTargetNodeList) o;
                            int depth = synsetDepthList.get(depth2RootIndex);
                            // depth2RootIndex++;
                            int length = 0;
                            for (Object o1 : pt) {
                                PointerTargetNode ptn = (PointerTargetNode) o1;
                                // System.out.println(ptn);
                                Synset ps = ptn.getSynset();
                                // System.out.print("Depth "+ depth +"\t,");
                                for (Word w : ps.getWords()) {
                                    // System.out.print("length from
                                    // \""+strStemmed+"\" to
                                    // \""+w.getLemma()+"\"="+length+", ");
                                    double simScore = similarityFormula(length, depth);

                                    // double threshold =
                                    // Double.parseDouble((String)appProperties.get("similirtyThreshold"));
                                    if (simScore >= threshold) {
                                        if (!w.getLemma().equals(strStemmed)) {
                                            String tmpLemma = w.getLemma();
                                            String lemma = tmpLemma;
                                            /*
                                             * String[] tmpLemmaSplit =
                                             * tmpLemma.split("[_]"); String
                                             * lemma ="";
                                             * if(tmpLemmaSplit.length>0){
                                             * //會有複合字的情況name_and_address，把它合起來!
                                             * for(String tmpStr :
                                             * tmpLemmaSplit)
                                             * lemma+=tmpStr.toLowerCase();
                                             * }else{ lemma =
                                             * tmpLemmaSplit[0].toLowerCase(); }
                                             */

                                            if (!strScoreHT.containsKey(lemma)) {
                                                strScoreHT.put(lemma, simScore);
                                            } else { // 如果有重複的字，則查看simScore是否比原本在hashtable裡面的大，如果比較大則替換掉；如果沒有，則不動。
                                                double value = strScoreHT.get(lemma);
                                                if (simScore > value)
                                                    strScoreHT.put(lemma, simScore);
                                            }
                                        }
                                    }
                                }
                                depth++;
                                length++;
                                // System.out.println();
                            }
                        }
                        // System.out.println();
                    }
                }
                // return strScoreHT;
            } catch (Exception e) {
                log.error("Error from WordNetExpansion.getHyponymsByNoun OR Verb!!!", e);
            }
        }
        return strScoreHT;

    }

    private double similarityFormula(double length, double depth) {
        // double lw =
        // Double.parseDouble((String)appProperties.getProperty("lengthWeight"));
        // double dw =
        // Double.parseDouble((String)appProperties.getProperty("depthWeight"));
        double lw = 0.2;
        double dw = 0.6;

        // length的分數公式
        double lFunc = Math.exp((0 - lw) * length);
        // System.out.println("lFunc:"+lFunc);
        double posDExp = Math.exp(dw * depth);
        // System.out.println("posDExp:"+posDExp);
        double negDExp = Math.exp((0 - dw) * depth);
        // System.out.println("negDExp:"+negDExp);
        // depth的分數公式
        double dFunc = (posDExp - negDExp) / (posDExp + negDExp);
        // System.out.println("dFunc:"+dFunc);
        // s = f(l)*f(d)
        double score = lFunc * dFunc;
        // System.out.println("f(l) = "+lFunc);
        // System.out.println("f(d) = "+dFunc);
        // System.out.println("score = "+score);
        BigDecimal b = new BigDecimal(score);
        double afterRound = b.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
        return afterRound;
    }

    public ArrayList<String> getWordNetExpansion(ArrayList<String> LDAWord, HashMap<String, String> stemmingAndTermsTable) {
        ArrayList<String> result = new ArrayList<>();
        for(String word : LDAWord) {
            try {
                // 查詢上義詞
                Hashtable<String, Double> Hypernyms = getHypernymsByNounOrVerb(word, POS.NOUN);
                for(String wordExpansion : Hypernyms.keySet()) {
                    if(!result.contains(wordExpansion)) {
                        result.add(wordExpansion);
                    }
                }
            } catch(IllegalArgumentException e) {
                String originWord = stemmingAndTermsTable.get(word);
                log.info("上義詞無法擴充 stem :{} --> origin :{}", word, originWord);
                if(originWord != null) {
                    Hashtable<String, Double> Hypernyms = getHypernymsByNounOrVerb(originWord, POS.NOUN);
                    for(String wordExpansion : Hypernyms.keySet()) {
                        if(!result.contains(wordExpansion)) {
                            result.add(wordExpansion);
                        }
                    }
                }
            }

            try {
                // 查詢下義詞
                Hashtable<String, Double> Hyponyms = getHyponymsByNounOrVerb(word, POS.NOUN);
                for(String wordExpansion : Hyponyms.keySet()) {
                    if(!result.contains(wordExpansion)) {
                        result.add(wordExpansion);
                    }
                }
            } catch(IllegalArgumentException e) {
                String originWord = stemmingAndTermsTable.get(word);
                log.info("下義詞無法擴充 stem :{} --> origin :{}", word, originWord);
                if(originWord != null) {
                    Hashtable<String, Double> Hyponyms = getHyponymsByNounOrVerb(originWord, POS.NOUN);
                    for(String wordExpansion : Hyponyms.keySet()) {
                        if(!result.contains(wordExpansion)) {
                            result.add(wordExpansion);
                        }
                    }
                }
            }
        }
        result = tokenizationAndStemming.applyArrayListOnStemming(result);
        return result;
    }
}