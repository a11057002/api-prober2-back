package ntou.soselab.swagger.cluster;

import ntou.soselab.swagger.neo4j.domain.service.ClusterGroupList;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.ClusterGroupListRepository;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ClusterTopicJson {

    Logger log = LoggerFactory.getLogger(ClusterTopicJson.class);

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    OperationRepository operationRepository;

    @Autowired
    ClusterGroupListRepository clusterGroupListRepository;

    public HashMap<String, HashMap<String, Integer>> collectionEachClusterGroupWord() {
        // record all finish search group
        ArrayList<String> finishSearchCluster = new ArrayList<>();

        // record cluster represent word
        HashMap<String, HashMap<String, Integer>> clusterWord = new HashMap<>();

        for(Resource resource : resourceRepository.findAll()) {
            // avoid same cluster search twice
            if(finishSearchCluster.contains(resource.getClusterGroup())) {
                log.info("This OAS is finish search");
            }else {
                String cluster = resource.getClusterGroup();

                // record same OAS resource and operation appear word and time
                HashMap<String, Integer> OASWord = new HashMap<>();

                // search this resource all same cluster id
                for(Resource resource1 : resourceRepository.findResourcesBySameCluster(cluster)) {

                    log.info("size :{}", resourceRepository.findResourcesBySameCluster(cluster).size());

                    if(resourceRepository.findResourcesBySameCluster(cluster).size() == 1) {
                        // not search no cluster OAS
                        log.info("this OAS only has itself");
                        break;
                    }

                    Long resource1Id = resource1.getNodeId();
                    if(resource1.getOriginalWord() != null && !resource1.getOriginalWord().isEmpty()) {
                        ArrayList<String> resourceLDA = resource1.getOriginalWord();
                        for(String word : resourceLDA) {
                            if(OASWord.containsKey(word)) {
                                OASWord.put(word, OASWord.get(word)+1);
                            }else {
                                OASWord.put(word, 1);
                            }
                        }
                    }

                    for(Operation operation : operationRepository.findOperationsByResource(resource1Id)) {
                        if(operation.getOriginalWord() != null && !operation.getOriginalWord().isEmpty()) {
                            ArrayList<String> operationLDA = operation.getOriginalWord();
                            for(String word : operationLDA) {
                                if(OASWord.containsKey(word)) {
                                    OASWord.put(word, OASWord.get(word)+1);
                                }else {
                                    OASWord.put(word, 1);
                                }
                            }
                        }
                    }
                }

                if(OASWord.size() > 0) {
                    // add OAS cluster add word
                    clusterWord.put(cluster, OASWord);
                }
                // add OAS cluster number
                finishSearchCluster.add(cluster);
            }
        }

        return clusterWord;
    }

    // 第一次儲存 群集清單節點
    public void saveClusterTopicJson(HashMap<String, HashMap<String, Integer>> clusterWord) {

        // JSON
        ArrayList<ClusterGroup> clusterGroups = new ArrayList<>();

        for(String clusterNumber : clusterWord.keySet()) {
            //get other top 10 word
            ArrayList<String> topicWord = getMoreUseWord(clusterWord.get(clusterNumber), 10);
            log.info("Group Id :{} --> Word :{}", clusterNumber, topicWord);

            ClusterGroup clusterGroup = new ClusterGroup();
            clusterGroup.setGroup(clusterNumber);
            clusterGroup.setWord(topicWord);
            clusterGroups.add(clusterGroup);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("clusterGroupList",clusterGroups);

        ClusterGroupList clusterGroupList = new ClusterGroupList();
        clusterGroupList.setGroupJson(jsonObject.toString());

        log.info("Json format :{}", jsonObject.toString());
        clusterGroupListRepository.save(clusterGroupList);
    }

    // 更新 群集清單字詞
    public void updateClusterTopicJson(HashMap<String, HashMap<String, Integer>> clusterWord) {

        // JSON
        ArrayList<ClusterGroup> clusterGroups = new ArrayList<>();

        for(String clusterNumber : clusterWord.keySet()) {
            //get other top 10 word
            ArrayList<String> topicWord = getMoreUseWord(clusterWord.get(clusterNumber), 20);
            log.info("Group Id :{} --> Word :{}", clusterNumber, topicWord);

            ClusterGroup clusterGroup = new ClusterGroup();
            clusterGroup.setGroup(clusterNumber);
            clusterGroup.setWord(topicWord);
            clusterGroups.add(clusterGroup);
        }

        for(ClusterGroupList clusterGroupList : clusterGroupListRepository.findAll()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("clusterGroupList",clusterGroups);

            clusterGroupList.setGroupJson(jsonObject.toString());

            log.info("Json format :{}", jsonObject.toString());
            clusterGroupListRepository.save(clusterGroupList);
        }
    }

    public ArrayList<String> getMoreUseWord(HashMap<String, Integer> list, int rank) {
        ArrayList<String> rankList = new ArrayList<>();

        // sort hashmap
        HashMap<String, Integer> sortedByCount = sortByValue(list);

        for(String word : sortedByCount.keySet()) {
//            log.info("Word :{}, Time :{}", word, list.get(word));
            if(rank>=1) rankList.add(word);
            if(rank<1) break;
            rank--;
        }
        return rankList;
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> wordCounts) {
        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

}
