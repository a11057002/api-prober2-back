package ntou.soselab.swagger.github;

import ntou.soselab.swagger.PersonalInformation;
import ntou.soselab.swagger.neo4j.domain.service.GitHub;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.PathRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import ntou.soselab.swagger.web.ProberPathConfig;

@Service
public class DownloadJavaDoc {

    Logger log = LoggerFactory.getLogger(DownloadJavaDoc.class);

    @Autowired
    PathRepository pathRepository;
    @Autowired
    ProberPathConfig proberPathConfig;


    public String downloadGitHubDoc(List<Resource> resources) {

        for(Resource resource : resources) {
            Long resourceId = resource.getNodeId();

            for(Path path : pathRepository.findPathsByResource(resourceId)) {

                Long pathId = path.getNodeId();
                //String folderName = swaggerName.replaceAll("/", "");

                if(pathRepository.findGitHubsByPathId(pathId).size() > 0) {
                    // File dir_file = new File("/home/andy/Desktop/api-prober/DownloadGitHubJavaDoc/"+pathId.toString());
                    File dir_file = new File(proberPathConfig.downloadGithubPath +pathId.toString());

                    if(!dir_file.exists()) {
                        dir_file.mkdir();
                    }
                }

                for(GitHub gitHub : pathRepository.findGitHubsByPathId(pathId)) {
                    Long id = gitHub.getNodeId();
                    String javaName = gitHub.getJavaDocumentName();
                    String repoFullName = gitHub.getRepoFullName();
                    String javaDocumentPath = gitHub.getJavaDocumentPath();

                    // avoid my repo affect result
                    if(!repoFullName.equals("Pudding124/DataClassification")) {
                        String url = new String("https://api.github.com/repos/"+repoFullName+"/contents/"+javaDocumentPath);
                        while (true){
                            if(sendRequestAndDownloadJavaDoc(id, url, javaName, pathId.toString())) {
                                break;
                            }else{
                                log.info("請求失敗，重新請求");
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean sendRequestAndDownloadJavaDoc(Long id, String downloadUrl, String javaName, String apiId) {
        // Auth
        String auth = PersonalInformation.GITHUB_ACCOUNT + ":" + PersonalInformation.GITHUB_PASSWORD;
       // byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")) );
        byte[] encodedAuth = Base64.encodeBase64(PersonalInformation.ACCESS_TOKEN.getBytes());
        String authHeader = "Basic " + new String( encodedAuth );

        try {
            // Request
            RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());


            HttpHeaders headers = new HttpHeaders();
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            headers.add("Retry-After","3");
            headers.set("Accept","application/vnd.github.VERSION.raw");
            headers.set("Authorization", authHeader);
            HttpEntity<String> requestEntity = new HttpEntity<String>("parameters", headers);
            log.info("DownloadUrl :{}", downloadUrl);
            log.info("等待 2 秒...");
            Thread.sleep(2000);
            ResponseEntity<String> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, requestEntity, String.class);

//            JSONObject jsonObject = new JSONObject(response.getBody());
//            String download = jsonObject.getString("download_url");
//            response = restTemplate.exchange(download, HttpMethod.GET, requestEntity, String.class);

            File file = new File("/home/andy/Desktop/api-prober/DownloadGitHubJavaDoc/"+apiId+"/"+id+".java");

            // avoid file overwrite, some swagger title is same
            if(!file.exists()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(response.getBody());
                fileWriter.flush();
                fileWriter.close();
            }

        }catch (HttpClientErrorException e) {
            System.out.println(e.getStatusCode().toString());
            System.out.println(e.getResponseBodyAsString());
            if (e.getStatusCode().toString().equals("404")) {
                log.info("file maybe delete by user");
                return true;
            }
            log.info("停止 20 秒");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e1) {
                log.info("InterruptedException :{}", e1.toString());
            }
            return false;
        } catch (InterruptedException e) {
            log.info("InterruptedException :{}", e.toString());
            return true;
        } catch (ResourceAccessException e) {
            log.info("time out :{}", e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("CCCC");
            log.info("IOException :{}", e.toString());
            return true;
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
}
