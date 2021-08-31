package ntou.soselab.swagger.swagger;

import ntou.soselab.swagger.algo.CosineSimilarity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CSTest {
    Logger log = LoggerFactory.getLogger(CSTest.class);
    CosineSimilarity cosineSimilarity = new CosineSimilarity();
    //@Test
    public void CosineSimiliarityTest() {
        CosineSimilarity cosineSimilarity = new CosineSimilarity();
        double[] docVector1 = {1, 2, 2, 1, 1, 1, 0};
        double[] docVector2 = {1, 2, 2, 1, 1, 2, 1};

        log.info("分數 :{}", cosineSimilarity.cosineSimilarity(docVector1, docVector2));
    }

    //@Test
    public void CollectionWord() {
        // 416527 - 157782
        ArrayList<String> target = new ArrayList<>();
        ArrayList<String> compare = new ArrayList<>();
        target.add("softwar");
        target.add("configur");
        target.add("updat");
        compare.add("configur");
        compare.add("compon");
        compare.add("proactiv");
        compare.add("detect");
        compare.add("client");

        calculateTwoMatrixVectorsAndCosineSimilarity(target, compare);

        ArrayList<String> target2 = new ArrayList<>();
        ArrayList<String> compare2 = new ArrayList<>();
        target2.add("updat");
        target2.add("creat");
        target2.add("softwar");
        target2.add("configur");
        target2.add("uri");
        target2.add("singl");
        target2.add("configur");
        target2.add("softwar");
        target2.add("updat");
        target2.add("specif");
        target2.add("updat");
        target2.add("delet");
        target2.add("softwar");
        target2.add("configur");
        target2.add("softwar");
        target2.add("updat");
        target2.add("account");
        target2.add("configur");

        compare2.add("detect");
        compare2.add("compon");
        compare2.add("proactiv");
        compare2.add("applic");
        compare2.add("configur");
        compare2.add("insight");
        compare2.add("proactiv");
        compare2.add("detect");
        compare2.add("configur");
        compare2.add("updat");
        compare2.add("detect");
        compare2.add("configur");
        compare2.add("proactiv");

        calculateTwoMatrixVectorsAndCosineSimilarity(target2, compare2);
    }

    //@Test
    public void CollectionWord2() {
        ArrayList<String> target = new ArrayList<>();
        ArrayList<String> compare = new ArrayList<>();
        target.add("I");
        target.add("like");
//        target.add("see");
//        target.add("tv");
//        target.add("no");
//        target.add("like");
        target.add("see");
//        target.add("movie");

        compare.add("I");
//        compare.add("no");
        compare.add("like");
//        compare.add("see");
//        compare.add("tv");
//        compare.add("also");
//        compare.add("no");
//        compare.add("like");
        compare.add("see");
//        compare.add("movie");

        calculateTwoMatrixVectorsAndCosineSimilarity(target, compare);
    }

    //@Test
    public void CollectionWord3() {
        // 393223 - 258052
        ArrayList<String> target = new ArrayList<>();
        ArrayList<String> compare = new ArrayList<>();
        compare.add("center");
        compare.add("germani");
        compare.add("rest");
        compare.add("inform");
        compare.add("enabl");
        compare.add("travel");
        compare.add("queri");
        target.add("oper");
        target.add("fabric");
        target.add("locat");

        ArrayList<String> target3 = new ArrayList<>();
        ArrayList<String> compare3 = new ArrayList<>();
        target3.add("oper");
        target3.add("fabric");
        target3.add("locat");
        compare3.add("locat");
        compare3.add("chang");
        compare3.add("locomot");
        compare3.add("travel");
        compare3.add("remaind");
        compare3.add("period");
        compare3.add("rest");
        compare3.add("center");
        compare3.add("field");
        compare3.add("pith");
        compare3.add("shop");
        compare3.add("centr");
        compare3.add("midpoint");
        compare3.add("attent");
        compare3.add("gist");
        compare3.add("sum");
        compare3.add("heart");
        compare3.add("plaza");
        compare3.add("kernel");
        compare3.add("mall");
        compare3.add("substanc");
        compare3.add("snapper");
        compare3.add("nitti");
        compare3.add("gritti");
        compare3.add("marrow");
        compare3.add("soul");
        compare3.add("nub");
        compare3.add("meat");
        compare3.add("inward");
        compare3.add("nerv");
        compare3.add("core");
        compare3.add("centerfield");
        compare3.add("ey");
        compare3.add("middl");
        compare3.add("essenc");
        compare3.add("residu");
        compare3.add("relax");
        compare3.add("balanc");
        compare3.add("relief");
        compare3.add("quietu");
        compare3.add("residuum");
        compare3.add("eas");
        compare3.add("etern");
        compare3.add("sleep");
        compare3.add("respit");
        compare3.add("repos");

        // sim_rc
        // sim_rc(LDA, LDA)
        calculateTwoMatrixVectorsAndCosineSimilarity(target, compare);
        // sim_op(LDA, WordNet)
        calculateTwoMatrixVectorsAndCosineSimilarity(target3, compare3);

        ArrayList<String> target2 = new ArrayList<>();
        ArrayList<String> compare2 = new ArrayList<>();
        target2.add("locat");
        target2.add("request");
        target2.add("fabric");
        target2.add("fabric");
        target2.add("locat");
        target2.add("updat");
        target2.add("locat");
        target2.add("deploy");
        target2.add("creat");
        target2.add("fabric");
        target2.add("outsid");

        compare2.add("info");
        compare2.add("station");
        compare2.add("station");
        compare2.add("inform");
        compare2.add("locat");
        compare2.add("radiu");
        compare2.add("station");
        compare2.add("inform");
        compare2.add("station");
        compare2.add("specif");

        // sim_op
        // sim_op(LDA, LDA)
        // sim_op(LDA, wordnet) no same word
        calculateTwoMatrixVectorsAndCosineSimilarity(target2, compare2);
    }

    public void calculateTwoMatrixVectorsAndCosineSimilarity(ArrayList<String> targetVector, ArrayList<String> compareVector) {

        ArrayList<String> allWord = new ArrayList<>();

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
        double score = cosineSimilarity.cosineSimilarity(target, compare);
        log.info("target :{}", target);
        log.info("compare :{}", compare);
        log.info("score :{}", score);
    }
}
