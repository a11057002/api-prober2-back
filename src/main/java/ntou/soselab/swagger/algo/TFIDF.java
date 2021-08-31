package ntou.soselab.swagger.algo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TFIDF {
    public static void algo(ArrayList<ArrayList<String>> docList){
        HashMap<String, Double> resultThreahold = new HashMap<>();
        HashMap<String, Double> result = new HashMap<>();
        for(ArrayList<String> doc : docList){
            System.out.println(doc);
            for(String token : doc){
                Double TFScore = TF(doc, token);
                Double IDFScore = IDF(docList, token);
                if(IDFScore < 0){
                    IDFScore = IDFScore*-1;
                }
//                System.out.print("字詞: "+token);
//                System.out.print(" TF: ");
//                System.out.printf("%1.3f", TFScore);
//                System.out.print(" IDF: ");
//                System.out.printf("%1.3f", IDFScore);
//                System.out.print(" TF-IDF: ");
//                System.out.printf("%1.3f", TFScore*IDFScore);
//                System.out.println();
                Double resultScore = TFScore*IDFScore;
                DecimalFormat df = new DecimalFormat("##.000");
                resultScore = Double.parseDouble(df.format(resultScore));
                if(0.005>resultScore) {
                    if(!resultThreahold.containsKey(token)) {
                        resultThreahold.put(token, resultScore);
                    }
                }
                if(!result.containsKey(token)) {
                    result.put(token, resultScore);
                }
            }
        }

        for(String word : result.keySet()) {
            System.out.println(word + " " + result.get(word));
        }

        System.out.println("總共字詞數量有 :" +result.size());
        System.out.println("低於 0.005 分數的字詞有 :" + resultThreahold.size());

        Scanner input = new Scanner(System.in);

        while(input.hasNext()) {
            String query = input.next();

            if(result.containsKey(query)) {
                System.out.println("字詞 :" + query + " 分數 :" + result.get(query));
            }

        }
    }

    public static double TF(ArrayList<String> doc, String token){  //the token appears how many times in this document
        Double tokenSum = Double.valueOf(doc.size());
        Double appearTokenTime = 0.0;
        for(String key : doc){
            if(token.equals(key)){
                appearTokenTime++;
            }
        }
        return (appearTokenTime/tokenSum);
    }

    public static double IDF(ArrayList<ArrayList<String>> docList, String token){ //the token appears how many times in this document list
        Double docSum = Double.valueOf(docList.size());
        Double appearDocTime = 0.0;
        for(ArrayList<String> doc : docList){
            for(String key : doc){
                if(token.equals(key)){
                    appearDocTime++;
                    break;
                }
            }
        }
        return Math.log(docSum/(appearDocTime+1))/Math.log(10);
    }
}
