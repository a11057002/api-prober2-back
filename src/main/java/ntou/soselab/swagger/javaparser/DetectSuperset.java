package ntou.soselab.swagger.javaparser;

import ntou.soselab.swagger.neo4j.domain.service.GitHub;
import ntou.soselab.swagger.neo4j.domain.service.JavaRepo;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.GitHubRepository;
import ntou.soselab.swagger.neo4j.repositories.service.PathRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class DetectSuperset {

    Logger log = LoggerFactory.getLogger(DetectSuperset.class);

    @Autowired
    PathRepository pathRepository;

    @Autowired
    GitHubRepository gitHubRepository;

    @Autowired
    JavaMethodParser javaMethodParser;

    public void findSuperset(Long resourceId, Long pathId, Resource resource, String targetEndpoint, String filePathName) {
        HashMap<String, HashMap<String, Double>> allResult = new HashMap<String, HashMap<String, Double>>();

        String targetEndpointArray[] = targetEndpoint.replaceAll("[\\pP\\p{Punct}]"," ").split(" ");

        ArrayList<String> containTargetEndpoints = new ArrayList<>();

        for(Path path : pathRepository.findPathsByResource(resourceId)) {
            String apiEndpoint = getEndpoint(resource, path);
            String compareEndpointArray[] = apiEndpoint.replaceAll("[\\pP\\p{Punct}]"," ").split(" ");
            // 與 target endpoint 不同的 endpoint
            if(!targetEndpoint.equals(apiEndpoint)) {
                // 確定 儲存的陣列中沒有重複 Endpoint
                if(!containTargetEndpoints.contains(apiEndpoint)) {
                    // 判斷 compare 是否有包含 target 所有元素
                    if(compareCode(targetEndpointArray, compareEndpointArray)) {
                        containTargetEndpoints.add(apiEndpoint);
                    }
                }
            }
        }

        for(String str : containTargetEndpoints) {
            log.info("fake endpoint :{}", str);
        }

        // 紀錄 api id 去根據檔案搜尋底下的 java document
        File file = new File(filePathName+"/"+pathId);

        for(String javaDocument : file.list()) {

            String java = readLocalFile(filePathName+"/"+pathId+"/"+javaDocument);

            if(java != null) {
                try {
                    boolean flag = true;
                    if(flag) {
                        HashMap<String, Double> result = javaMethodParser.getJavaMethodUse(containTargetEndpoints, targetEndpoint, filePathName+"/"+pathId+"/"+javaDocument);

                        log.info("--------Success-----------------"+javaDocument+"---------------------------------");
                        if(!result.isEmpty()) {
                            log.info("Target Endpoints :{}", targetEndpoint);
                            log.info("match method name :{}", result);
                            allResult.put(javaDocument, result);
                        }
                    }
                } catch(IOException e) {
                    log.info("IO error :{}", e.toString());
                }
            }else {
                log.info("error read file");
            }

        }

        // 列出排名前三的檔案
        HashMap<String, ArrayList<String>> rank = new HashMap<>();
        // 紀錄分數
        ArrayList<Integer> recordScore = new ArrayList<>();
        int score = 5;
        while (score > 0){
            for(String documentName : allResult.keySet()) {
                // log.info("---------------{}-------------------", documentName);
                ArrayList<String> allDocumentMethodName = new ArrayList<>();
                for(String methodName : allResult.get(documentName).keySet()) {
                    // log.info("method name: {} & score :{}", methodName, allResult.get(documentName).get(methodName));
                    if(allResult.get(documentName).get(methodName) == score) {
                        allDocumentMethodName.add(methodName);
                    }
                }
                if (!allDocumentMethodName.isEmpty()) {
                    rank.put(documentName, allDocumentMethodName);
                    recordScore.add(score);
                }
                if (rank.size() == 3) break;
            }
            score--;
            if (rank.size() == 3) break;
        }

        // 儲存前三名到 neo4j 內
        if(!rank.isEmpty()) {
            buildJavaCodeNeo4j(rank, recordScore);
        }
    }

    // 根據將其 GitHub 內容寫入到 Java Node 裡面，在建立與 Path 的關係
    public void buildJavaCodeNeo4j(HashMap<String, ArrayList<String>> rank, ArrayList<Integer> recordScore) {
        int i = 0;
        for(String documentName : rank.keySet()) {
            log.info("Document :{}, Method :{}", documentName, rank.get(documentName));

            String[] githubId = documentName.split("\\.");

            GitHub gitHub = gitHubRepository.findGitHubById(Long.valueOf(githubId[0]));
            Path path = gitHubRepository.findPathByGitHub(gitHub.getNodeId());

            JavaRepo javaRepo = new JavaRepo();
            javaRepo.setJavaDocumentName(gitHub.getJavaDocumentName());
            javaRepo.setJavaDocumentHtmlUrl(gitHub.getJavaDocumentHtmlUrl());
            javaRepo.setRepoName(gitHub.getRepoName());
            javaRepo.setRepoUrl(gitHub.getRepoUrl());
            javaRepo.setDocumentId(documentName);
            javaRepo.setMethod(rank.get(documentName));
            javaRepo.setScore(recordScore.get(i));

            path.addParseRelationship(path, javaRepo);
            pathRepository.save(path);
            i++;
        }
    }

    // For testing
    public String readLocalFile(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, "UTF-8");
        } catch (IOException e) {
            System.err.println("read swagger error");
            return null;
        }

    }

    public boolean compareCode(String[] target, String[] compare){
        target = replaceStringNullValue(target);
        compare = replaceStringNullValue(compare);
        for(String key : target){
            boolean flag = false;
            if(!key.equals("") && !key.equals("https")){
                for(String key1 : compare){
                    if(key.equals(key1)){
                        //System.out.println(key);
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    //log.info("Miss on:{}", key);
                    return false;
                }
            }
        }
        return true;
    }

    public String[] replaceStringNullValue(String str[]) {
        ArrayList<String> tmp = new ArrayList<String>();

        for(String word:str){
            if(word!=null && word.length()!=0){
                tmp.add(word);
            }
        }
        str= tmp.toArray(new String[0]);
        return str;
    }

    public String getEndpoint(Resource resource, Path path) {
        String api = "";
        List schemes = resource.getSchemes();
        String host = resource.getHost();
        String basePath = resource.getBasePath();
        String url = basePath;

        // 獲得 完整的 BaseUrl
        /*if(!schemes.isEmpty() && !host.isEmpty()){
            boolean flag = false;
            for(Object scheme : schemes){
                if(scheme.toString().toLowerCase().equals("https")){
                    flag = true;
                    url = getBaseUrl("https",host,basePath, url);
                    api = url + path.getPath();
                    break;
                }
            }
            if(!flag){
                url = getBaseUrl("http",host,basePath, url);
                api = url + path.getPath();
            }
        }else{
            log.info("some information lost");
        }*/

        return api;
    }

    public String getBaseUrl(String scheme, String host, String basePath, String url){
        if(basePath == null){
            if(host.subSequence(host.length()-1,host.length()).equals("/")){
                url = scheme.toString().toLowerCase() + "://" + host.substring(0, host.length()-1);
            }else{
                url = scheme.toString().toLowerCase() + "://" + host;
            }
        }else{
            if(basePath.subSequence(basePath.length()-1,basePath.length()).equals("/")){
                url = scheme.toString().toLowerCase() + "://" + host + basePath.substring(0, basePath.length()-1);
            }else{
                url = scheme.toString().toLowerCase() + "://" + host + basePath;
            }
        }
        return url;
    }

}
