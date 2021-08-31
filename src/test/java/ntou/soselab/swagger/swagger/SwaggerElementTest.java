package ntou.soselab.swagger.swagger;


import ntou.soselab.swagger.transformation.SwaggerToNeo4jTransformation;
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
public class SwaggerElementTest {
    Logger log = LoggerFactory.getLogger(SwaggerElementTest.class);

    @Autowired
    SwaggerToNeo4jTransformation swaggerToNeo4jTransformation;

    //@Test
    public void readOneFile() {
        try {
            // do something
            String document = readLocalSwagger("./src/main/resources/swagger document/" + "ebay.com_buy-feed_v1_beta.9.0.json");
            if(document != null){
                swaggerToNeo4jTransformation.parseSwaggerDocument(document);
            }else{
                log.error("error read swagger local file");
            }
        } catch (Exception e) {
            log.error("error parsing");
            log.error(e.toString());
        }
    }

    @Test
    public void readSwaggerFile() {
        File sDocFolder = new File("./src/main/resources/swagger document");
        int fileNumber = 200;

        for (String serviceFile : sDocFolder.list()) {
            if(fileNumber == 0) break;
            fileNumber--;
            log.info("parse swagger guru file: {}", serviceFile);
            try {
                // do something
                String document = readLocalSwagger("./src/main/resources/swagger document/" + serviceFile);
                if(document != null){
                    log.info(serviceFile);
                    swaggerToNeo4jTransformation.parseSwaggerDocument(document);
                }else{
                    log.error("error read swagger local file: {}", serviceFile);
                }
                Files.move(Paths.get("./src/main/resources/swagger document/" + serviceFile), Paths.get("./src/main/resources/finish/" + serviceFile));
                log.info("finish move file {} to finish folder.", serviceFile);
            } catch (Exception e) {
                log.error("error parsing on {}", serviceFile);
                log.info(e.toString());
                try {
                    Files.move(Paths.get("./src/main/resources/swagger document/" + serviceFile), Paths.get("./src/main/resources/fail/" + serviceFile));
                } catch (IOException e1) {
                    log.info("error on move file to error folder", e);
                }
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
