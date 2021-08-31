package ntou.soselab.swagger.github;

import ntou.soselab.swagger.PersonalInformation;
import ntou.soselab.swagger.neo4j.domain.service.GitHub;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.repositories.service.PathRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@RestController
public class JavaSearch {

    Logger log = LoggerFactory.getLogger(JavaSearch.class);

    @Autowired
    PathRepository pathRepository;

    Path path;
    GitHub gitHub;

    @RequestMapping(value = "/search/code", method = RequestMethod.GET)
    public String searchCode (@RequestParam("code") String code, Path path) {
        //if(rateLimit == 0){
        //    log.info("Github API 次數用完，等待中...");
        //    Thread.sleep(1000*70);
        //    rateLimit = 30;
        //}
        while(true){
            log.info("code :{}",code);
            if(requestGithubAPI(code, path)){
                break;
            }else{
                log.info("請求失敗，重新請求");
            }
        }
        // github url

        return "Done";
    }

    public boolean requestGithubAPI(String code, Path path) {
        int page = 1;
        String url = new String("https://api.github.com/search/code?q="+code+"+language:java"+"&per_page=100&page="+page);

        // Auth
        String auth = PersonalInformation.GITHUB_ACCOUNT + ":" + PersonalInformation.GITHUB_PASSWORD;
        //byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")) );
        byte[] encodedAuth = Base64.encodeBase64(PersonalInformation.ACCESS_TOKEN.getBytes());
        String authHeader = "Basic " + new String( encodedAuth );

        try {
            // Request
            RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());


            HttpHeaders headers = new HttpHeaders();
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            headers.add("Retry-After","3");
            headers.set("Accept","application/vnd.github.v3.text-match + json");
            headers.set("Authorization", authHeader);
            HttpEntity<String> requestEntity = new HttpEntity<String>("parameters", headers);
            log.info("等待 5 秒...");
            Thread.sleep(5000);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            //rateLimit--;
            //System.out.println(response.getBody());
            JSONArray items;
            JSONObject jsonObject;

            jsonObject = new JSONObject(response.getBody());
            //int total = jsonObject.getInt("total_count");

            items = jsonObject.getJSONArray("items");
            //parseJson(items, apiEndpoint); // SearchGitHub class 使用
            parseFragmentCode(items, code, path); // SearchGitHubRealUse class 使用
        }catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getResponseBodyAsString());

            // The search is longer than 128 characters.
            if(e.getStatusCode().toString().equals("422")) {
                return true;
            }
            log.info("停止 20 秒");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e1) {
                log.info("e1 error :{}", e1.toString());
                return false;
            }
            return false;
        }catch (ResourceAccessException e) {
            log.info("time out :{}", e.getMessage());
            return false;
        }catch (Exception e) {
            log.info("Error :{}", e.toString());
            return false;
        }
        return true;
    }

    // RestTemplate conection time out
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        int timeout = 10000;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(config)
                .build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }

    public void parseFragmentCode(JSONArray jsonArray, String code, Path path){

        // delete brackets content, avoid token can not be match
        String api = code.replaceAll("(?<=\\{)(?!\\s*\\{)[^{}]+","");

        String str2[] = api.replaceAll("[\\pP\\p{Punct}]"," ").toLowerCase().split(" ");

        for(int i = 0; i < str2.length; i++) log.info("Finished API token :{}", str2[i]);

        for(int i = 0;i < jsonArray.length();i++){
            boolean saveCheck = false;
            JSONObject item = jsonArray.getJSONObject(i);
            JSONArray textMatches = item.getJSONArray("text_matches");
            String javaDocumentName = item.getString("name");
            String javaDocumentUrl = item.getString("url");
            String javaDocumentPath = item.getString("path");
            String javaDocumentHtmlUrl = item.getString("html_url");

            JSONObject repo = item.getJSONObject("repository");
            String repoName = repo.getString("name");
            String repoFullName = repo.getString("full_name");
            String repoHtmlUrl = repo.getString("html_url");

            ArrayList<String> textMatchWord = new ArrayList<>();

            // collection all text match fragment
            for(int j = 0;j < textMatches.length();j++){
                JSONObject textMatch = textMatches.getJSONObject(j);
                String fragment = textMatch.getString("fragment");
                fragment = fragment.trim().toLowerCase();
                fragment = fragment.replaceAll("\n","");
                fragment = fragment.replaceAll("[\\pP\\p{Punct}]"," ");
                String str[] = fragment.split(" ");

                for(String word : str) {
                    textMatchWord.add(word);
                }
            }

            String[] str1 = textMatchWord.toArray(new String[0]);

            if(compareCode(str1,str2)){
                saveCheck = true;
            }
            if(saveCheck) {
                log.info("success !");
                gitHub = new GitHub(repoFullName, repoName, repoHtmlUrl, javaDocumentName, javaDocumentUrl, javaDocumentPath, javaDocumentHtmlUrl);
                path.addFindRelationship(path, gitHub);
                pathRepository.save(path);
            }
        }
    }

    public boolean compareCode(String[] str1, String[] str2){
        boolean httpControl = false;
        for(String key : str1) {
            if(key.equals("http") || key.equals("https")) {
                httpControl = true;
                break;
            }
        }
        if(!httpControl) {
            log.info("Miss on http or https");
            return false;
        }
        for(String key : str2){
            boolean flag = false;
            if(!key.equals("") && !key.equals("https")){
                for(String key1 : str1){
                    if(key.equals(key1)){
                        //System.out.println(key);
                        flag = true;
                        break;
                    }
                }
                if(!flag){
                    log.info("Miss on:{}", key);
                    return false;
                }
            }
        }
        return true;
    }
}
