package ntou.soselab.swagger.web;

import ntou.soselab.swagger.engine.SearchEngine;
import ntou.soselab.swagger.neo4j.domain.service.*;
import ntou.soselab.swagger.neo4j.repositories.service.*;
import ntou.soselab.swagger.transformation.Neo4jToDatabase;
import ntou.soselab.swagger.web.homepage.EndpointFeature;
import ntou.soselab.swagger.web.homepage.EngineResult;
import ntou.soselab.swagger.web.homepage.PopularStandardOAS;
import ntou.soselab.swagger.web.homepage.ServiceFeature;
import ntou.soselab.swagger.web.oaspage.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


@Service
public class ServiceManager {
    @Autowired
    OASParser oasParser;
    @Autowired
    UpdateAnnotation updateAnnotation;
    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    OperationRepository operationRepository;
    @Autowired
    PathRepository pathRepository;
    @Autowired
    ParameterRepository parameterRepository;
    @Autowired
    ResponseRepository responseRepository;
    @Autowired
    StatusCodeRepository statusCodeRepository;
    @Autowired
    JavaRepoRepository javaRepoRepository;
    @Autowired
    SearchEngine searchEngine;
    @Autowired
    SecurityRepository securityRepository;
    @Autowired
    TestCaseRepository testCaseRepository;
    @Autowired
    Neo4jToDatabase neo4jToDatabase;
    @Autowired
    OASRepository oasRepository;


    Logger log = LoggerFactory.getLogger(ServiceManager.class);

    public String runSearchEngine(String query) {
        // 回傳結果相關資料
        ArrayList<EngineResult> engineResults = new ArrayList<>();

        // 進行搜尋
        HashMap<String, Double> result = searchEngine.userQueryMatch(query);
        ArrayList<Double> score = new ArrayList();
        for(String id : result.keySet()) {
            score.add(result.get(id));
        }

        // 分數排名
        double[] doubleArray = new double[score.size()];
        for(int x = 0;x<score.size();x++) {
            doubleArray[x] = score.get(x);
        }
        Arrays.sort(doubleArray);

        log.info("id :{}", result);

        // 紀錄顯示過的結果
        ArrayList<String> record = new ArrayList<>();

        for(int x = doubleArray.length-1;x>=0;x--) {
            for(String key : result.keySet()) {
                if(result.get(key) == doubleArray[x]) {
                    if(!record.contains(key)) {
                        log.info("id :{} ---> score :{}", key, doubleArray[x]);
                        Resource resource = resourceRepository.findResourceById(Long.valueOf(key));
                        EngineResult engineResult = new EngineResult();
                        engineResult.setImage(resource.getLogo());
                        engineResult.setResourceId(String.valueOf(resource.getNodeId()));
                        engineResult.setResourceName(resource.getTitle());
                        engineResult.setDescription(resource.getDescription());
                        double testingRate = 0.0;
                        int count = 0;

                        for(Path path : pathRepository.findPathsByResource(Long.valueOf(key))) {
                            for(Operation operation : operationRepository.findOperationsByPath(path.getNodeId())) {
                                // get testing rate
                                if(operation.getTestingResult()!=null){
                                    count++;
                                    String testingString = operation.getTestingResult().get( operation.getTestingResult().size()-1).split("-")[2];
                                    testingRate += (Double. parseDouble(testingString.split("/")[0])/Double. parseDouble(testingString.split("/")[1]));
                                    System.out.println(testingRate);
                                }
                            }
                        }
                        if(testingRate/count >= 1){
                            resource.setFeature("100% Test Pass");
                        } else if(testingRate/count < 1 && testingRate/count > 0.6){
                            resource.setFeature("60% Test Pass");
                        } else if(testingRate/count < 0.6 && testingRate/count > 0.0){
                            resource.setFeature("Test Fail");
                        } else{
                            resource.setFeature("No Test");
                        }
                        engineResult.setFeature(resource.getFeature());
                        engineResults.add(engineResult);
                        // 紀錄已顯示過之結果，避免重複顯示
                        record.add(key);
                    }
                }
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("search", engineResults);
        log.info("search :{}", jsonObject.toString());
        return jsonObject.toString();
    }

    public String countServiceLevel() {
        int serviceTotal = resourceRepository.totalResource();

        ArrayList<ServiceFeature> result = new ArrayList<>();
        ServiceFeature httpsSupport = new ServiceFeature();
        ServiceFeature userAuthentication = new ServiceFeature();
        ServiceFeature most20perations = new ServiceFeature();
        ServiceFeature exampleAPIConversations = new ServiceFeature();
        httpsSupport.setFeature("HTTPS support");
        userAuthentication.setFeature("User authentication");
        most20perations.setFeature("At most 20 operations");
        exampleAPIConversations.setFeature("Example API conversations");
        int support = 0;
        int Authentication = 0;
        int mostOperations = 0;
        int example = 0;

        for(Resource resource : resourceRepository.findAll()) {
            for(String feature : resource.getFeature()) {
                if(feature.equals("HTTPS support")) {
                    support++;
                }
                if(feature.equals("User authentication")) {
                    Authentication++;
                }
                if(feature.equals("At most 20 operations")) {
                    mostOperations++;
                }
                if(feature.equals("Example API conversations")) {
                    example++;
                }
            }
        }
        log.info("support :{}", support);
        log.info("Authentication :{}", Authentication);
        log.info("mostOperations :{}", mostOperations);
        log.info("example :{}", example);
        httpsSupport.setQuantity(support);
        userAuthentication.setQuantity(Authentication);
        most20perations.setQuantity(mostOperations);
        exampleAPIConversations.setQuantity(example);
//        log.info("httpsSupport :{}", httpsSupport);
//        log.info("userAuthentication :{}", userAuthentication);
//        log.info("most20perations :{}", most20perations);
//        log.info("exampleAPIConversations :{}", exampleAPIConversations);
        result.add(httpsSupport);
        result.add(userAuthentication);
        result.add(most20perations);
        result.add(exampleAPIConversations);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("totalService", serviceTotal);
        jsonObject.put("serviceLevel", result);
        log.info("service feature :{}", jsonObject);
        return jsonObject.toString();
    }

    public String countEndpointLevel() {
        int operationTotal = operationRepository.totalOperation();

        ArrayList<EndpointFeature> result = new ArrayList<>();
        EndpointFeature restStyle = new EndpointFeature();
        EndpointFeature httpStatus = new EndpointFeature();
        EndpointFeature errorMessage = new EndpointFeature();
        EndpointFeature inputJson = new EndpointFeature();
        EndpointFeature outputJson = new EndpointFeature();
        restStyle.setFeature("REST-style URls");
        httpStatus.setFeature("HTTP status code use");
        errorMessage.setFeature("Explain Error messages");
        inputJson.setFeature("Input format JSON");
        outputJson.setFeature("Output format JSON");
        int style = 0;
        int statusCode = 0;
        int error = 0;
        int input = 0;
        int output = 0;

        for(Resource resource : resourceRepository.findAll()) {
            for(Operation operation : operationRepository.findOperationsByResource(resource.getNodeId())) {
                for(String feature : operation.getFeature()) {
                    if(feature.equals("REST-style URls")) {
                        style++;
                    }
                    if(feature.equals("HTTP status code use")) {
                        statusCode++;
                    }
                    if(feature.equals("Explain Error messages")) {
                        error++;
                    }
                    if(feature.equals("Input format JSON")) {
                        input++;
                    }
                    if(feature.equals("Output format JSON")) {
                        output++;
                    }
                }
            }
        }
        log.info("style :{}", style);
        log.info("statusCode :{}", statusCode);
        log.info("error :{}", error);
        log.info("input :{}", input);
        log.info("output :{}", output);
        restStyle.setQuantity(style);
        httpStatus.setQuantity(statusCode);
        errorMessage.setQuantity(error);
        inputJson.setQuantity(input);
        outputJson.setQuantity(output);
        result.add(restStyle);
        result.add(httpStatus);
        result.add(errorMessage);
        result.add(inputJson);
        result.add(outputJson);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("totalOperation", operationTotal);
        jsonObject.put("endpointLevel", result);
        log.info("endpoint feature :{}", jsonObject);
        return jsonObject.toString();
    }

    public String countPopularStandardOAS() {
        ArrayList<PopularStandardOAS> standard = new ArrayList<>();
        ArrayList<PopularStandardOAS> popular = new ArrayList<>();
        for(Resource resource : javaRepoRepository.findResourceByHaveJavaRepo()) {
            ArrayList<String> serviceFeature = resource.getFeature();
            if(serviceFeature.contains("HTTPS support") && serviceFeature.contains("User authentication") && serviceFeature.contains("At most 20 operations") && serviceFeature.contains("Example API conversations")) {

                for(Operation operation : operationRepository.findOperationsByResource(resource.getNodeId())) {
                    ArrayList<String> endpointFeature = operation.getFeature();
                    if(endpointFeature.contains("REST-style URls") && endpointFeature.contains("HTTP status code use") && endpointFeature.contains("Explain Error messages") && endpointFeature.contains("Input format JSON") && endpointFeature.contains("Output format JSON")) {
                        PopularStandardOAS standardOAS = new PopularStandardOAS();
                        standardOAS.setId(resource.getNodeId());
                        standardOAS.setTitle(resource.getTitle());
                        standardOAS.setDescription(resource.getDescription());
                        standard.add(standardOAS);
                        break;
                    }
                }
            }
            if(standard.size() >= 5) {
                break;
            }
        }

        for(Resource resource : javaRepoRepository.findResourceBySortJavaRepo()) {
            if(popular.size() < 5) {
                PopularStandardOAS popularOAS = new PopularStandardOAS();
                popularOAS.setId(resource.getNodeId());
                popularOAS.setTitle(resource.getTitle());
                popularOAS.setDescription(resource.getDescription());
                popular.add(popularOAS);
            }else {
                break;
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("standard", standard);
        jsonObject.put("popular", popular);
        log.info("result :{}", jsonObject);
        return jsonObject.toString();
    }

    public String runOASBasicInformation(Long resourceId) {
        Resource resource = resourceRepository.findResourceById(resourceId);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setTitle(resource.getTitle());
        resourceInfo.setFeatures(resource.getFeature());
        resourceInfo.setDescription(resource.getDescription());
        resourceInfo.setProvider(resource.getProvider());
        resourceInfo.setHost(resource.getHost());
        resourceInfo.setBaseUrl(resource.getBasePath());
        //resourceInfo.setContact(resource.getSwaggerUrl());
        resourceInfo.setSwaggerUrl(resource.getSwaggerUrl());

        // get path info
        ArrayList<PathInfo> pathInfos = new ArrayList<>();
        for(Path path : pathRepository.findPathsByResource(resourceId)) {
            PathInfo pathInfo = new PathInfo();
            pathInfo.setEndpoint(path.getPath());

            // get javaRepo info
            ArrayList<JavaRepoInfo> javaRepos = new ArrayList<>();
            for(JavaRepo javaRepo : javaRepoRepository.findJavaReposByPath(path.getNodeId())) {
                JavaRepoInfo javaRepoInfo = new JavaRepoInfo();
                javaRepoInfo.setRepoName(javaRepo.getRepoName());
                javaRepoInfo.setRepoUrl(javaRepo.getRepoUrl());
                javaRepoInfo.setJavaDocHtml(javaRepo.getJavaDocumentHtmlUrl());
                javaRepoInfo.setMethod(javaRepo.getMethod());
                javaRepoInfo.setScore(javaRepo.getScore());
                javaRepos.add(javaRepoInfo);
            }
            double testingRate = 0.0;
            // get operation info
            ArrayList<OperationInfo> operationInfos = new ArrayList<>();
            for(Operation operation : operationRepository.findOperationsByPath(path.getNodeId())) {
                OperationInfo operationInfo = new OperationInfo();
                operationInfo.setId(operation.getNodeId().toString());
                operationInfo.setOperation(operation.getOperationAction());
                operationInfo.setDescription(operation.getDescription());
                operationInfo.setFeatures(operation.getFeature());
                operationInfo.setTestingResult(operation.getTestingResult());


                // get testing rate
                if(operation.getTestingResult()!=null){
                    String testingString = operation.getTestingResult().get( operation.getTestingResult().size()-1).split("-")[2];
                    testingRate =(Integer. parseInt(testingString.split("/")[0])/Integer. parseInt(testingString.split("/")[1]));
                }

                // get input parameter info
                ArrayList<InputParameterInfo> inputParameterInfos = new ArrayList<>();
                for(Parameter parameter : parameterRepository.findParametersByOperationNoThreshold(operation.getNodeId())) {
                    InputParameterInfo inputParameterInfo = new InputParameterInfo();
                    inputParameterInfo.setId(parameter.getNodeId().toString());
                    inputParameterInfo.setDescription(parameter.getDescription());
                    inputParameterInfo.setIn(parameter.getIn());
                    inputParameterInfo.setParameter(parameter.getName());
                    inputParameterInfo.setRequired(parameter.isRequired());
                    inputParameterInfo.setType(parameter.getMedia_type());
                    inputParameterInfos.add(inputParameterInfo);
                }
                // get status code info
                ArrayList<JSONObject> statusCodeInfos = new ArrayList<>();
                for(StatusCode statusCode : statusCodeRepository.findStatusCodesByOperation(operation.getNodeId())) {
                    JSONObject jsonObject = new JSONObject();
                    String statusNumber = statusCode.getStatusCode();

                    JSONArray responses = new JSONArray();
                    for(Response response : responseRepository.findResponsesByStatusCode(statusCode.getNodeId())) {
                        JSONObject object = new JSONObject();
                        object.put("id", response.getNodeId());
                        object.put("type", response.getMedia_type());
                        object.put("parameter", response.getName());
                        object.put("description", response.getDescription());
                        object.put("required", response.getRequired());
                        responses.put(object);
                    }

                    jsonObject.put(statusNumber, responses);
                    statusCodeInfos.add(jsonObject);
                    log.info("status code :{}", statusCodeInfos);
                }
                operationInfo.setInputParameters(inputParameterInfos);
                operationInfo.setStatusCode(statusCodeInfos);
                operationInfos.add(operationInfo);
            }
            resourceInfo.setServiceTesting(testingRate);
            pathInfo.setOperations(operationInfos);
            pathInfo.setJavaRepos(javaRepos);
            pathInfos.add(pathInfo);
        }
        resourceInfo.setEndpoints(pathInfos);
        JSONObject jsonObjectMary = new JSONObject(resourceInfo);
        log.info("ans :{}",jsonObjectMary);
        return jsonObjectMary.toString();
    }




}
