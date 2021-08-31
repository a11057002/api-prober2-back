package ntou.soselab.swagger.cluster;

import ntou.soselab.swagger.algo.CosineSimilarity;
import ntou.soselab.swagger.neo4j.domain.service.ClusterGroupList;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.ClusterGroupListRepository;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClusterMashup {

    Logger log = LoggerFactory.getLogger(ClusterMashup.class);

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    ClusterGroupListRepository clusterGroupListRepository;

    CosineSimilarity cosineSimilarity = new CosineSimilarity();

//    private static final double mashupClusterThreshold = 0.2;

    // 收集 群集相關資訊 並進行群集間比較 包裝成 JSON 後回傳
    public String compareTagetClusterAndOtherCluster(Long id, double threshold) {

        Resource resource = resourceRepository.findResourceById(id);

        // 先判斷群集是否獨立
        if(resourceRepository.findCountBySameCluster(resource.getClusterGroup()) == 1) {
            JSONObject clusterWeb = new JSONObject();
            clusterWeb.put("name", "cluster");
            JSONArray children = new JSONArray();

            JSONObject currentCluster = new JSONObject();
            currentCluster.put("name", "Location_Cluster");
            JSONArray currentChildren = new JSONArray();

            JSONObject object = new JSONObject();
            object.put("name", resource.getTitle());
            object.put("id", resource.getNodeId());
            object.put("size", "3");
            currentChildren.put(object);

            currentCluster.put("children", currentChildren);
            children.put(currentCluster);
            clusterWeb.put("children", children);
            return clusterWeb.toString();
        }

        HashMap<String, ArrayList<String>> groupList = new HashMap<>();
        String groupListJson = "";

        for(ClusterGroupList clusterGroupList : clusterGroupListRepository.findAll()) {
            groupListJson = clusterGroupList.getGroupJson();
        }

        // 解析群集前十名詞語之 JSON
        try {
            JSONObject jsonObject = new JSONObject(groupListJson);
            JSONArray jsonArray = jsonObject.getJSONArray("clusterGroupList");

            for(int x=0;x<jsonArray.length();x++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(x);

                String group = jsonObject1.optString("group");
                ArrayList<String> word = new ArrayList<>();

                JSONArray jsonArray1 = jsonObject1.optJSONArray("word");
                for(int y = 0;y<jsonArray1.length();y++) {
                    word.add(jsonArray1.getString(y));
                }

                groupList.put(group, word);
            }

        } catch(JSONException e) {
            log.info("JSON Exception :{}", e.toString());
        }

        // 獲得可 Mashup 群集編號
        ArrayList<String> mashupsNumber = getMashupsClusterNumber(groupList, id, threshold);

        // 獲得目標群集之群集編號
        String targetClusterNumber = resource.getClusterGroup();

        // 介面回傳 群集 json
        JSONObject clusterWeb = new JSONObject();
        clusterWeb.put("name", "cluster");

        JSONArray children = new JSONArray();

        // 設定 OAS 所在群集之相關 JSON 資訊
        JSONObject currentCluster = new JSONObject();
        currentCluster.put("name", "Location_Cluster");
        JSONArray currentChildren = new JSONArray();
        for(Resource resource1 : resourceRepository.findResourcesBySameCluster(targetClusterNumber)) {
            if(String.valueOf(resource1.getNodeId()).equals(String.valueOf(resource.getNodeId()))) {
                JSONObject object = new JSONObject();
                object.put("name", resource1.getTitle());
                object.put("id", resource1.getNodeId());
                object.put("size", "3");
                currentChildren.put(object);
            }else {
                JSONObject object = new JSONObject();
                object.put("name", resource1.getTitle());
                object.put("id", resource1.getNodeId());
                object.put("size", "1");
                currentChildren.put(object);
            }
        }
        currentCluster.put("children", currentChildren);
        children.put(currentCluster);

        // 設定 OAS 可以 Mashup 相關 JSON 資訊
        int clusterNo = 1;
        for(String mashupNumber : mashupsNumber) {
            JSONObject compareCluster = new JSONObject();
            compareCluster.put("name", "Correlation_Cluster-"+clusterNo);
            JSONArray compareChildren = new JSONArray();
            for(Resource resource1 : resourceRepository.findResourcesBySameCluster(mashupNumber)) {
                JSONObject object = new JSONObject();
                object.put("name", resource1.getTitle());
                object.put("id", resource1.getNodeId());
                object.put("size", "1");
                compareChildren.put(object);
            }
            compareCluster.put("children", compareChildren);
            children.put(compareCluster);
            clusterNo++;
        }

        clusterWeb.put("children", children);

        return clusterWeb.toString();
    }

    public ArrayList<String> getMashupsClusterNumber(HashMap<String, ArrayList<String>> groupList, Long targetId, double threshold) {
        // 獲得目標群集之群集字詞
        Resource resource = resourceRepository.findResourceById(targetId);
        ArrayList<String> targetWord = groupList.get(resource.getClusterGroup());

        ArrayList<String> result = new ArrayList<>();

        for(String groupNumber : groupList.keySet()) {
            if(!groupNumber.equals(resource.getClusterGroup())) {
                double score = calculateTwoMatrixVectorsAndCosineSimilarity(targetWord, groupList.get(groupNumber));

                if(score > threshold) {
                    result.add(groupNumber);
                }
            }
        }

        return result;
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
}