package ntou.soselab.swagger.transformation;

import ntou.soselab.swagger.algo.WordNetExpansion;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Parameter;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.domain.service.Response;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ParameterRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class UpdateLDAWordNetWord {
    Logger log = LoggerFactory.getLogger(UpdateLDAWordNetWord.class);

    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    OperationRepository operationRepository;
    @Autowired
    ParameterRepository parameterRepository;
    @Autowired
    ResponseRepository responseRepository;
    @Autowired
    SwaggerToLDA swaggerToLDA;
    @Autowired
    WordNetExpansion wordNetExpansion;

    public void updateResource() {

        HashMap<String, ArrayList<String>> havaTagOAS = new HashMap<>();

        ArrayList<String> OAS1 = new ArrayList<>();
        OAS1.add("API");OAS1.add("API descriptions");OAS1.add("API definition");OAS1.add("swagger");OAS1.add("RAML");OAS1.add("WADL");OAS1.add("API blueprint");OAS1.add("OpenAPI");
        havaTagOAS.put("APIMATIC API Transformer", OAS1);

        ArrayList<String> OAS2 = new ArrayList<>();
        OAS2.add("API");OAS2.add("Catalog");OAS2.add("Directory");OAS2.add("REST");OAS2.add("Swagger");OAS2.add("OpenAPI");
        havaTagOAS.put("APIs.guru", OAS2);

        ArrayList<String> OAS3 = new ArrayList<>();
        OAS3.add("payments");OAS3.add("bank-neutral");OAS3.add("omni-channel reporting");
        havaTagOAS.put("Beanstream Payments", OAS3);

        ArrayList<String> OAS4 = new ArrayList<>();
        OAS4.add("sandbox");OAS4.add("API");OAS4.add("REST");OAS4.add("SOAP");OAS4.add("Swagger");OAS4.add("OpenAPI");
        havaTagOAS.put("Sandbox", OAS4);

        ArrayList<String> OAS5 = new ArrayList<>();
        OAS5.add("blog");OAS5.add("social journalism");OAS5.add("publishing platform");
        havaTagOAS.put("Medium.com", OAS5);

        ArrayList<String> OAS6 = new ArrayList<>();
        OAS6.add("humor");OAS6.add("comics");
        havaTagOAS.put("XKCD", OAS6);

        ArrayList<String> OAS7 = new ArrayList<>();
        OAS7.add("bitcoin");OAS7.add("digital currency");
        havaTagOAS.put("Yunbi", OAS7);

        ArrayList<String> OAS8 = new ArrayList<>();
        OAS8.add("SMS");OAS8.add("bulk SMS");
        havaTagOAS.put("Zoom Connect", OAS8);

        try {
            for(Resource resource : resourceRepository.findAll()) {
                // store swagger parse information
                ArrayList<String> resourceSwaggerInfo = new ArrayList<>();
                // For saving key: stemming term --> value: original term
                HashMap<String, String> resourceStemmingAndTermsTable = new HashMap<String, String>();
                String description = resource.getDescription();
                if(description != null) resourceSwaggerInfo.add(description);

                if(havaTagOAS.containsKey(resource.getTitle())) {
                    for(String str : havaTagOAS.get(resource.getTitle())) {
                        resourceSwaggerInfo.add(str);
                    }
                }

                if(resourceSwaggerInfo.size() != 0) {
                    // parse LDA
                    ArrayList<String> resourceLDAWord = swaggerToLDA.swaggerParseLDA(resourceSwaggerInfo.toArray(new String[0]), resourceStemmingAndTermsTable);
                    // parse WordNet
                    ArrayList<String> resourceWordNetWord = wordNetExpansion.getWordNetExpansion(resourceLDAWord, resourceStemmingAndTermsTable);
                    resource.setOriginalWord(resourceLDAWord);
                    resource.setWordnetWord(resourceWordNetWord);
                    resourceRepository.save(resource);
                }
            }
        } catch (IOException e) {
            log.error("IOException :{}", e.toString());
        } catch (Exception e) {
            log.error("Exception :{}", e.toString());
        }
    }

    public void updateOperation() {
        try {
            for(Resource resource : resourceRepository.findAll()) {
                for(Operation operation : operationRepository.findOperationsByResource(resource.getNodeId())) {
                    // store swagger parse information
                    ArrayList<String> operationSwaggerInfo = new ArrayList<>();
                    // For saving key: stemming term --> value: original term
                    HashMap<String, String> operationStemmingAndTermsTable = new HashMap<String, String>();
                    String description = operation.getDescription();
                    if(description != null) operationSwaggerInfo.add(description);

                    if(operationSwaggerInfo.size() != 0) {
                        // parse LDA
                        ArrayList<String> operationLDAWord = swaggerToLDA.swaggerParseLDA(operationSwaggerInfo.toArray(new String[0]), operationStemmingAndTermsTable);
                        // parse WordNet
                        ArrayList<String> operationWordNetWord = wordNetExpansion.getWordNetExpansion(operationLDAWord, operationStemmingAndTermsTable);
                        operation.setOriginalWord(operationLDAWord);
                        operation.setWordnetWord(operationWordNetWord);
                        operationRepository.save(operation);
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOException :{}", e.toString());
        } catch (Exception e) {
            log.error("Exception :{}", e.toString());
        }
    }

    public void updateParameter() {
        try {
            for(Resource resource : resourceRepository.findAll()) {
                for(Operation operation : operationRepository.findOperationsByResource(resource.getNodeId())) {
                    for(Parameter parameter : parameterRepository.findParametersByOperationNoThreshold(operation.getNodeId())) {
                        // store swagger parse information
                        ArrayList<String> parameterSwaggerInfo = new ArrayList<>();
                        // For saving key: stemming term --> value: original term
                        HashMap<String, String> parameterStemmingAndTermsTable = new HashMap<String, String>();
                        String name = parameter.getName();
                        String description = parameter.getDescription();
                        if(name != null) parameterSwaggerInfo.add(name);
                        if(description != null) parameterSwaggerInfo.add(description);

                        if(parameterSwaggerInfo.size() != 0) {
                            // parse LDA
                            ArrayList<String> parameterLDAWord = swaggerToLDA.swaggerParseLDA(parameterSwaggerInfo.toArray(new String[0]), parameterStemmingAndTermsTable);
                            // parse WordNet
                            ArrayList<String> parameterWordNetWord = wordNetExpansion.getWordNetExpansion(parameterLDAWord, parameterStemmingAndTermsTable);
                            parameter.setOriginalWord(parameterLDAWord);
                            parameter.setWordnetWord(parameterWordNetWord);
                            parameterRepository.save(parameter);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOException :{}", e.toString());
        } catch (Exception e) {
            log.error("Exception :{}", e.toString());
        }
    }

    public void updateResponse() {
        try {
            for(Resource resource : resourceRepository.findAll()) {
                for(Operation operation : operationRepository.findOperationsByResource(resource.getNodeId())) {
                    for(Response response : responseRepository.findSuccessResponsesByOperation(operation.getNodeId())) {
                        // store swagger parse information
                        ArrayList<String> responseSwaggerInfo = new ArrayList<>();
                        // For saving key: stemming term --> value: original term
                        HashMap<String, String> responseStemmingAndTermsTable = new HashMap<String, String>();
                        String name = response.getName();
                        String description = response.getDescription();
                        if(name != null) responseSwaggerInfo.add(name);
                        if(description != null) responseSwaggerInfo.add(description);

                        if(responseSwaggerInfo.size() != 0) {
                            // parse LDA
                            ArrayList<String> responseLDAWord = swaggerToLDA.swaggerParseLDA(responseSwaggerInfo.toArray(new String[0]), responseStemmingAndTermsTable);
                            // parse WordNet
                            ArrayList<String> responseWordNetWord = wordNetExpansion.getWordNetExpansion(responseLDAWord, responseStemmingAndTermsTable);
                            response.setOriginalWord(responseLDAWord);
                            response.setWordnetWord(responseWordNetWord);
                            responseRepository.save(response);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOException :{}", e.toString());
        } catch (Exception e) {
            log.error("Exception :{}", e.toString());
        }
    }

}
