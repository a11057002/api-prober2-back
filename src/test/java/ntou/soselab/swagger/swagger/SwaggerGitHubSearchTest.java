package ntou.soselab.swagger.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import ntou.soselab.swagger.github.JavaSearch;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.PathRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SwaggerGitHubSearchTest {
    Logger log = LoggerFactory.getLogger(SwaggerGitHubSearchTest.class);

    @Autowired
    ResourceRepository resourceRepository;

    @Autowired
    PathRepository pathRepository;

    @Autowired
    JavaSearch javaSearch;

    @Test
    public void readFinishFile(){
        int fileNumber = 80;
        File sDocFolder = new File("./src/main/resources/finish");
        for (String serviceFile : sDocFolder.list()) {
            if(fileNumber == 0) break;
            fileNumber--;
            log.info("parse swagger guru file: {}", serviceFile);
            try {
                // do something
                String document = readLocalSwagger("./src/main/resources/finish/" + serviceFile);
                if(document != null){
                    String title = parseSwaggerTitle(document);
                    addGitHubNode(title);
                }else{
                    log.error("error read swagger local file: {}", serviceFile);
                }
                Files.move(Paths.get("./src/main/resources/finish/" + serviceFile), Paths.get("./src/main/resources/finishsearch/" + serviceFile));
                log.info("finish move file {} to finish search folder.", serviceFile);
            } catch (Exception e) {
                log.error("error parsing on {}", serviceFile);
                try {
                    Files.move(Paths.get("./src/main/resources/finish/" + serviceFile), Paths.get("./src/main/resources/finishfailsearch/" + serviceFile));
                } catch (IOException e1) {
                    log.info("error on move file to error folder", e);
                }
            }
        }
    }

    public String parseSwaggerTitle(String swaggerDoc) throws IOException, InterruptedException {
        SwaggerParseResult swaggerResult = new OpenAPIV3Parser().readContents(swaggerDoc, null, null);
        OpenAPI swagger = swaggerResult.getOpenAPI();

        String title = null;
        title = swagger.getInfo().getTitle();
        return title;
    }

    // 進行搜尋
    public void addGitHubNode(String title){
        for(Resource resource : resourceRepository.findResourcesByTitle(title)) {
            //針對 特定 Resource ID
            Long resourceId = resource.getNodeId();

            List schemes = resource.getSchemes();
            //String host = resource.getBasePath();
            String basePath = resource.getBasePath();
            //String url = null;

            // 獲得 完整的 BaseUrl
            /*if(!schemes.isEmpty() && !basePath.isEmpty()){
                boolean flag = false;
                for(Object scheme : schemes){
                    if(scheme.toString().toLowerCase().equals("https")){
                        flag = true;
                        url = getBaseUrl("https",host,basePath, url);
                        break;
                    }
                }
                if(!flag){
                    url = getBaseUrl("http",host,basePath, url);
                }
            }else{
                log.info("some information lost");
            }*/

            // 將 url 加入 path 後 進行搜尋
            searchByUrl(basePath, resourceId);
        }
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

    public void searchByUrl(String baseUrl, Long resourceId) {
        for(Path path : pathRepository.findPathsByResource(resourceId)){
            if(baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length()-1);
            String endpoint = baseUrl + path.getPath();

            log.info("API Enpoint Before :{}", endpoint);
            endpoint = endpoint.replaceAll("[{}]", "");

            if(endpoint != null){
                log.info("API Enpoint After :{}", endpoint);
                javaSearch.searchCode(endpoint, path);
            }else{
                log.info("有 API Endpoint 是空值");
            }
        }
    }

    // For testing
    public String readLocalSwagger(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, "UTF-8");
        } catch (IOException e) {
            System.err.println("read swagger error");
            return null;
        }

    }
}
