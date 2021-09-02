package ntou.soselab.swagger.javaparser;

import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.PathRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ntou.soselab.swagger.web.ProberPathConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MethodParser {

    Logger log = LoggerFactory.getLogger(MethodParser.class);

    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    PathRepository pathRepository;
    @Autowired
    DetectSuperset detectSuperset;
    @Autowired
    ProberPathConfig proberPathConfig;

    @Test
    public void searchDownloadFile() {

        // String filePathName = "/home/andy/Desktop/api-prober/DownloadGitHubJavaDoc";
        String filePathName = proberPathConfig.downloadGithubPath;
        File file = new File(filePathName);

        // 將下載回來的檔案，名稱為 path id，根據其檔案名稱抓取其 path
        for(String javaDocument : file.list()) {
            Long pathId = Long.valueOf(javaDocument);

            Resource resource = resourceRepository.findResourceByPathId(pathId);
            Path path = pathRepository.findPathById(pathId);

            String endpoint = getEndpoint(resource, path);
            log.info("API :{}", endpoint);

            detectSuperset.findSuperset(resource.getNodeId(), path.getNodeId(), resource, endpoint, filePathName);
        }
    }

    public String getEndpoint(Resource resource, Path path) {
        String api = "";
        List schemes = resource.getSchemes();
        //String host = resource.getHost();
        String basePath = resource.getBasePath();

        if(basePath.endsWith("/")) basePath = basePath.substring(0, basePath.length()-1);
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
        api = url + path.getPath();
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
