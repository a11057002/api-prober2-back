package ntou.soselab.swagger.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import ntou.soselab.swagger.github.DownloadJavaDoc;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class DownloadJavaDocTest {
    Logger log = LoggerFactory.getLogger(DownloadJavaDocTest.class);

    @Autowired
    DownloadJavaDoc downloadJavaDoc;
    @Autowired
    ResourceRepository resourceRepository;

    @Test
    public void readGuruFiles(){
        int fileNumber = 245;
        File sDocFolder = new File("./src/main/resources/finishsearch");
        for (String serviceFile : sDocFolder.list()) {
            if(fileNumber == 0) break;
            fileNumber--;
            log.info("parse swagger guru file: {}", serviceFile);
            try {
                // do something
                String document = readLocalSwagger("./src/main/resources/finishsearch/" + serviceFile);
                if(document != null){
                    String title = parseSwaggerTitle(document);
                    downloadJavaDoc.downloadGitHubDoc(resourceRepository.findResourcesByTitle(title));
                    log.error("ret");
                }else{
                    log.error("error read swagger local file: {}", serviceFile);
                }
                Files.move(Paths.get("./src/main/resources/finishsearch/" + serviceFile), Paths.get("./src/main/resources/downloadFinish/" + serviceFile));
                log.info("finish move file {} to finish search folder.", serviceFile);
            } catch (Exception e) {
                log.error("error download on {}", serviceFile);
                log.error(e.toString());
                try {
                    Files.move(Paths.get("./src/main/resources/finishsearch/" + serviceFile), Paths.get("./src/main/resources/downloadFail/" + serviceFile));
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
