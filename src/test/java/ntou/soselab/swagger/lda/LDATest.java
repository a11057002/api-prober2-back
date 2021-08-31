package ntou.soselab.swagger.lda;

import ntou.soselab.swagger.transformation.SwaggerToLDA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LDATest {
    Logger log = LoggerFactory.getLogger(LDATest.class);

    @Autowired
    SwaggerToLDA swaggerToLDA;

    //@Test
    public void LDATopicTest() {
        HashMap<String, String> stemmingAndTermsTable = new HashMap<>();
        String[] text = new String[1];
        text[0] = "Welcome to the Daymet Single Pixel Tool API. You can use this API to download daily surface data within the Daymet database in a `csv` or `json` format for a single point. This API allows users to query a single geographic point by latitude and longitude in decimal degrees.";
        try {
            swaggerToLDA.swaggerParseLDA(text, stemmingAndTermsTable);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
