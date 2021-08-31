package ntou.soselab.swagger.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import ntou.soselab.swagger.algo.TFIDF;
import ntou.soselab.swagger.algo.TokenizationAndStemming;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AllSwaggerTFIDF {
    Logger log = LoggerFactory.getLogger(AllSwaggerTFIDF.class);

    TokenizationAndStemming tokenizationAndStemming = new TokenizationAndStemming();

    @Test
    public void calculateTFIDF() {
        try{
            File file = new File("./src/main/resources/SwaggerTFIDFData"); //file path
            File[] source = file.listFiles();
            ArrayList<ArrayList<String>> docList = new  ArrayList<ArrayList<String>>();
            for(File txt : source){
                FileReader fr = new FileReader(txt);
                BufferedReader br = new BufferedReader(fr);
                ArrayList<String> doc = new ArrayList<String>();
                Double wordSum = 0.0;
                while(br.ready()){
                    String tmp[] = br.readLine().split(" ");
                    for(String word : tmp) {
                        doc.add(word);
                    }
                }
                docList.add(doc);
                fr.close();
            }
            TFIDF.algo(docList);
        }catch(IOException e){
            System.out.print("Read file miss:"+ e);
        }
    }

    //@Test
    public void readSwaggerFile() {
        File sDocFolder = new File("./src/main/resources/swagger document");

        int count = 0;
        for (String serviceFile : sDocFolder.list()) {
            log.info("parse swagger guru file: {}", serviceFile);
            try {
                // do something
                String document = readLocalSwagger("./src/main/resources/swagger document/" + serviceFile);
                if(document != null){
                    ArrayList<String> allDocWord = new ArrayList<>();

                    SwaggerParseResult swaggerResult = new OpenAPIV3Parser().readContents(document, null, null);
                    OpenAPI swagger = swaggerResult.getOpenAPI();

                    getResourceInformation(swagger, allDocWord);
                    getOperation(swagger, allDocWord);
                    File file = new File("./src/main/resources/SwaggerTFIDFData/"+count+".txt");

                    // avoid file overwrite, some swagger title is same
                    if(!file.exists()) {
                        FileWriter fileWriter = new FileWriter(file);
                        for(String word : allDocWord) {
                            fileWriter.write(word + " ");
                        }
                        fileWriter.flush();
                        fileWriter.close();
                    }

                }else{
                    log.error("error read swagger local file: {}", serviceFile);
                }
            } catch (Exception e) {
                log.error("error parsing on {}", serviceFile);
            }
            count++;
        }
    }

    public void getResourceInformation(OpenAPI swagger, ArrayList<String> allDocWord) {

        // store swagger parse information
        ArrayList<String> swaggerInfo = new ArrayList<>();

        // For saving key: stemming term --> value: original term
        HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();

        String description = null;

        Map<String, Object> info = swagger.getInfo().getExtensions();

        // get description
        description = swagger.getInfo().getDescription();
        if(description != null) swaggerInfo.add(description);

        String swaggerInfoArray[] = swaggerInfo.toArray(new String[0]);

        try {
            if(swaggerInfo.size() != 0) {
                for (int i = 0; i < swaggerInfoArray.length; i++) {
                    String terms = change_ToSeperateTerms(
                            changeDotsToSeperateTerms(changeCamelWordsToSeperateTerms(replaceTagsToNone(swaggerInfoArray[i]))));
                    swaggerInfoArray[i] = tokenizationAndStemming.stemTermsAndSaveOriginalTerm(terms, stemmingAndTermsTable);
                    //writeTxt.inputTxt("['"+ResourceConcept[i]+"'],");
                    log.info(" -- {}", swaggerInfoArray[i]);
                    allDocWord.add(swaggerInfoArray[i]);
                }
            }
        } catch (IOException e) {
            log.info("Parse word topic error :{}", e.toString());
        }
    }

    public void getOperation(OpenAPI swagger, ArrayList<String> allDocWord) {
        for (String p : swagger.getPaths().keySet()) {

            if (swagger.getPaths().get(p).getDelete() != null) {
                log.info("--- operation:DELETE on {}", p);
                getOperationInformation(swagger.getPaths().get(p).getDelete(), allDocWord);
            }
            if (swagger.getPaths().get(p).getGet() != null) {
                log.info("--- operation:GET on {}", p);
                getOperationInformation(swagger.getPaths().get(p).getGet(), allDocWord);
            }
            if (swagger.getPaths().get(p).getPatch() != null) {
                log.info("--- operation:PATCH on {}", p);
                getOperationInformation(swagger.getPaths().get(p).getPatch(), allDocWord);
            }
            if (swagger.getPaths().get(p).getPost() != null) {
                log.info("--- operation:POST on {}", p);
                getOperationInformation(swagger.getPaths().get(p).getPost(), allDocWord);
            }
            if (swagger.getPaths().get(p).getPut() != null) {
                log.info("--- operation:PUT on {}", p);
                getOperationInformation(swagger.getPaths().get(p).getPut(), allDocWord);
            }
        }
    }

    private void getOperationInformation(io.swagger.v3.oas.models.Operation swaggerOperation, ArrayList<String> allDocWord) {

        // store swagger parse information
        ArrayList<String> swaggerInfo = new ArrayList<>();
        // For saving key: stemming term --> value: original term
        HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();

        String description = null;

        description = swaggerOperation.getDescription();
        if(description != null) swaggerInfo.add(description);

        String swaggerInfoArray[] = swaggerInfo.toArray(new String[0]);

        try {
            if(swaggerInfo.size() != 0) {
                for (int i = 0; i < swaggerInfoArray.length; i++) {
                    String terms = change_ToSeperateTerms(
                            changeDotsToSeperateTerms(changeCamelWordsToSeperateTerms(replaceTagsToNone(swaggerInfoArray[i]))));
                    swaggerInfoArray[i] = tokenizationAndStemming.stemTermsAndSaveOriginalTerm(terms, stemmingAndTermsTable);
                    //writeTxt.inputTxt("['"+ResourceConcept[i]+"'],");
                    log.info(" -- {}", swaggerInfoArray[i]);
                    allDocWord.add(swaggerInfoArray[i]);
                }
            }
        } catch (IOException e) {
            log.info("Parse word topic error :{}", e.toString());
        }
    }

    private String replaceTagsToNone(String input) {
        return input.replaceAll("<.*?>", " ").trim();
    }

    private String changeCamelWordsToSeperateTerms(String input) {
        String[] data = input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        StringBuilder builder = new StringBuilder();
        for (String w : data) {
            builder.append(w.toLowerCase());
            builder.append(" ");
        }
        return builder.toString().trim();
    }

    private String changeDotsToSeperateTerms(String input) {
        return input.replaceAll("\\.", " ").trim();
    }

    private String change_ToSeperateTerms(String input) {
        return input.replaceAll("_", " ").trim();
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
