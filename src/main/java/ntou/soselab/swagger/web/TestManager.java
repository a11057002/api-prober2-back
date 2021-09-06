package ntou.soselab.swagger.web;

import com.google.gson.*;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import ntou.soselab.swagger.neo4j.domain.relationship.Test;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.domain.service.TestCase;
import ntou.soselab.swagger.neo4j.graph.OperationGraph;
import ntou.soselab.swagger.neo4j.graph.TestCaseGraph;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.TestCaseRepository;
import ntou.soselab.swagger.security.jwt.AuthTokenFilter;
import ntou.soselab.swagger.security.jwt.JwtUtils;
import ntou.soselab.swagger.transformation.Neo4jToDatabase;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.*;
@Service
public class TestManager {

    @Autowired
    OperationRepository operationRepository;
    @Autowired
    Neo4jToDatabase neo4jToDatabase;
    @Autowired
    TestCaseRepository testCaseRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthTokenFilter authTokenFilter;


    // endpoint testing in try area (frontend UI)
    public String tryEndpoint(String endpointData) {
        RestTemplate restTemplate = new RestTemplate();
        JsonObject jsonObject = new JsonParser().parse(endpointData).getAsJsonObject();
        Long operationId = jsonObject.get("operationId").getAsLong();
        ResponseEntity<String> postResponse = null;
        HttpHeaders headers = new HttpHeaders();

        Path path = operationRepository.findPathByOperation(operationId);
        Resource resource = operationRepository.findResourceByOperation(operationId);
        String method = operationRepository.findActionsByOperationId(operationId);

        String baseUrl = resource.getBasePath();
        String endpoint = path.getPath().substring(1);

        String queryName;
        String queryValue;
        String pathName;
        String pathValue;
        String headerName;
        String headerValue;
        String body;
        String parameter="?";
//oauth_token=NGW50DGLAX1S0P2P3YCIQAXL33SLTU2GJ4BFOLQWGVOJQLTB&v=20200101
        String response = "error";


        JsonArray parameterArray = jsonObject.get("parameters").getAsJsonArray();
        //https://api.foursquare.com/v2/users/self/venuelikes?oauth_token=NGW50DGLAX1S0P2P3YCIQAXL33SLTU2GJ4BFOLQWGVOJQLTB&v=20190102
        /**/



        if(method.equals("get")){
            for(int i = 0; i < parameterArray.size(); i++){

                JsonObject parameterObject = parameterArray.get(i).getAsJsonObject();
                if(parameterObject.get("in").getAsString().equals("query") ){

                    queryName = parameterObject.get("name").getAsString();
                    queryValue = parameterObject.get("value").getAsString();
                    if(i == parameterArray.size()-1){
                        parameter += queryName+"="+queryValue;
                    }else{
                        parameter += queryName+"="+queryValue+"&";
                    }


                    //System.out.println(parameter);
                }
                if(parameterObject.get("in").getAsString().equals("path")){
                    pathName = parameterObject.get("name").getAsString();
                    pathValue = parameterObject.get("value").getAsString();
                    endpoint = endpoint.replaceAll("\\{" + pathName + '}',pathValue);
                    System.out.println(endpoint);
                }
                if(parameterObject.get("in").getAsString().equals("header")){
                    headers.add("Authorization", "Bearer "+parameterObject.get("value").getAsString());

                }


            }
            System.out.println(baseUrl+endpoint+parameter+headers);
            try{

                response = restTemplate.exchange(baseUrl+endpoint+parameter, HttpMethod.GET, new HttpEntity<Object>(headers),String.class).getBody();

            } catch (HttpClientErrorException e){
                System.out.println(response);
                return "Error: "+ e.getStatusCode()+" "+e.getStatusText();
            }


            //response =  restTemplate.getForObject(baseUrl+endpoint+"oauth_token=NGW50DGLAX1S0P2P3YCIQAXL33SLTU2GJ4BFOLQWGVOJQLTB&v=20200101",String.class);
            //change to getForEntity
            return response;
        }
        if(method.equals("post")){
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();

            for(int i = 0; i < parameterArray.size(); i++){

                JsonObject parameterObject = parameterArray.get(i).getAsJsonObject();
                if(parameterObject.get("in").getAsString().equals("query")){
                    queryName = parameterObject.get("name").getAsString();
                    queryValue = parameterObject.get("value").getAsString();
                    map.add(queryName, queryValue);

                }
                if(parameterObject.get("in").getAsString().equals("path")){
                    pathName = parameterObject.get("name").getAsString();
                    pathValue = parameterObject.get("value").getAsString();
                    endpoint = endpoint.replaceAll("\\{" + pathName + '}',pathValue);
                }
                if(parameterObject.get("in").getAsString().equals("body-form")){
                    map.add(parameterObject.get("name").getAsString(),parameterObject.get("value").getAsString());
                }
                if(parameterObject.get("in").getAsString().equals("header")){
                    headers.add("Authorization", "Bearer "+parameterObject.get("value").getAsString());

                }

            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            System.out.println(baseUrl+endpoint+request);
            try{
                postResponse = restTemplate.postForEntity( baseUrl+endpoint, request , String.class );
                return  postResponse.getBody();
            } catch (HttpClientErrorException e){
                System.out.println(e.getStatusCode()+e.getStatusText());
                return "Error: "+ e.getStatusCode()+" "+e.getStatusText();
            } catch (HttpServerErrorException e){
                System.out.println(e.getStatusCode()+e.getStatusText());
                return "Error: "+ e.getStatusCode()+" "+e.getStatusText();
            }



        }
        return null;
    }


    //endpoint testing in test case area (frontend UI)
    public String runTestCase(String testCaseData) throws Exception{
//{"operationId":"290","parameters":[{"in":"query","name":"v","value":"3"},{"in":"query","name":"oauth_token","value":"1"}],"jsonPath":1,"expectedPartialResult":1}
        /*HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");*/
        ResponseEntity<String> postResponse = null;
        HttpHeaders headers = new HttpHeaders();
        JsonObject jsonObject = new JsonParser().parse(testCaseData).getAsJsonObject();
        Long operationId = 0L;
        Long testCaseId = 0L;
        if(jsonObject.has("nodeId")){
            testCaseId = jsonObject.get("nodeId").getAsLong();
            operationId = operationRepository.findOperationIdByTestCaseId(testCaseId);
            jsonObject = new JsonParser().parse(testCaseRepository.findTestCasesByTestCaseId(testCaseId).toString()).getAsJsonObject();
        }
        if(jsonObject.has("operationId")){
            operationId = jsonObject.get("operationId").getAsLong();
        }

        String baseUrl = operationRepository.findResourceByOperation(operationId).getBasePath();
        String endpoint = operationRepository.findPathByOperation(operationId).getPath().substring(1);
        String method = operationRepository.findActionsByOperationId(operationId);

        String jsonPath = jsonObject.get("jsonPath").getAsString();
        String jsonPathResult = "";
        String expectedPartialResult = jsonObject.get("expectedPartialResult").getAsString();
        boolean expectedPartialResultBool = false;
        if(jsonObject.get("expectedCheck") != null)
            expectedPartialResultBool = jsonObject.get("expectedCheck").getAsBoolean();

        String queryName;
        String queryValue;
        String pathName;
        String pathValue;
        String headerName;
        String headerValue;
        String body;
        String parameter="?";
        String response = "";


        JsonArray parameterArray = jsonObject.get("parameters").getAsJsonArray();
        RestTemplate restTemplate = new RestTemplate();

        if(method.equals("get")){
            for(int i = 0; i < parameterArray.size(); i++){

                JsonObject parameterObject = parameterArray.get(i).getAsJsonObject();
                if(parameterObject.get("in").getAsString().equals("query")){
                    queryName = parameterObject.get("name").getAsString();
                    queryValue = parameterObject.get("value").getAsString();
                    if(i == parameterArray.size()-1){
                        parameter += queryName+"="+queryValue;
                    }else{
                        parameter += queryName+"="+queryValue+"&";
                    }
                }
                if(parameterObject.get("in").getAsString().equals("path")){
                    pathName = parameterObject.get("name").getAsString();
                    pathValue = parameterObject.get("value").getAsString();
                    endpoint = endpoint.replaceAll("\\{" + pathName + '}',pathValue);
                }
                if(parameterObject.get("in").getAsString().equals("header")){
                    headers.add("Authorization", "Bearer "+parameterObject.get("value").getAsString());

                }

            }

            System.out.println(baseUrl+endpoint+parameter+headers);
            try{
                response = restTemplate.exchange(baseUrl+endpoint+parameter, HttpMethod.GET, new HttpEntity<Object>(headers),String.class).getBody();

            } catch (HttpClientErrorException e){
                return "Error: "+ e.getStatusCode()+" "+e.getStatusText();
            }

            //return response;
        }
        if(method.equals("post")){
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();

            for(int i = 0; i < parameterArray.size(); i++){

                JsonObject parameterObject = parameterArray.get(i).getAsJsonObject();
                if(parameterObject.get("in").getAsString().equals("query")){
                    queryName = parameterObject.get("name").getAsString();
                    queryValue = parameterObject.get("value").getAsString();
                    map.add(queryName, queryValue);

                }
                if(parameterObject.get("in").getAsString().equals("path")){
                    pathName = parameterObject.get("name").getAsString();
                    pathValue = parameterObject.get("value").getAsString();
                    endpoint = endpoint.replaceAll("\\{" + pathName + '}',pathValue);
                }
                if(parameterObject.get("in").getAsString().equals("body-form")){
                    map.add(parameterObject.get("name").getAsString(),parameterObject.get("value").getAsString());
                }
                if(parameterObject.get("in").getAsString().equals("header")){
                    headers.add("Authorization", "Bearer "+parameterObject.get("value").getAsString());

                }

            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

            try{
                postResponse = restTemplate.postForEntity( baseUrl+endpoint, request , String.class );
                response =  postResponse.getBody();
            } catch (HttpClientErrorException e){
                System.out.println(e.getStatusCode()+e.getStatusText());
                return "Error: "+ e.getStatusCode()+" "+e.getStatusText();
            } catch (HttpServerErrorException e){
                System.out.println(e.getStatusCode()+e.getStatusText());
                return "Error: "+ e.getStatusCode()+" "+e.getStatusText();
            }
        }

        // testing result compare with JSON path and expected partial values
        try {
            System.out.println(response);
            DocumentContext jsonContext = JsonPath.parse(response);
            jsonPathResult = jsonContext.read(jsonPath).toString();
            System.out.println(jsonContext.jsonString());
            //System.out.println(jsonContext.read(jsonPath).toString());
            if(!expectedPartialResult.isEmpty() && jsonPathResult != null){
                String str[] = expectedPartialResult.split(",");
                for(int i= 0; i < str.length; i++){
                    if (expectedPartialResultBool) {
                        if (jsonPathResult.contains(str[i]) ) {
                            if (i == str.length - 1)
                                return jsonPathResult;
                            else continue;
                        }
                        return "Error: Expected partial result not found.";
                    }
                    else{
                        if (jsonPathResult.contains(str[i]))
                            return jsonPathResult;
                        return "Error: Expected partial result not found.";
                    }
                }

            }


        }catch (PathNotFoundException e){
            return "Error: "+e.getMessage();
        }
        catch (InvalidPathException e) {
            return "Error: "+e.getMessage();
        }
        System.out.println(jsonPathResult);
        return jsonPathResult;
    }
    // store test case to neo4j
    public String saveTestCase(String testCaseData){
        //System.out.println(testCaseData);
        JsonObject jsonObject = new JsonParser().parse(testCaseData).getAsJsonObject();
        Long id = jsonObject.get("operationId").getAsLong();
        Operation operation = operationRepository.findOperationByOperationId(id);

        JsonArray parameterArray = jsonObject.get("parameters").getAsJsonArray();
        TestCase testCase = new TestCase(parameterArray.toString(), jsonObject.get("jsonPath").getAsString(), jsonObject.get("expectedPartialResult").getAsString(), jsonObject.get("provider").getAsString(), jsonObject.get("private").getAsBoolean());

        Test test = new Test();
        TestCaseGraph testCaseGraph = new TestCaseGraph();
        OperationGraph operationGraph = new OperationGraph(operation);

        testCaseGraph.setTestCase(testCase);
        testCaseGraph.setTest(test);
        operationGraph.setTestCaseGraphs(testCaseGraph);
        neo4jToDatabase.saveTestCaseWithOperation(operation, testCase, test);

        return "Save";
    }

    /*public String runTestCase(Long testCaseId) {
        TestCase testcase = testCaseRepository.findTestCasesByTestCaseId(testCaseId);

            return  null;

    }*/
    /*public String importTestCase(Long operationId){
        List<TestCase> testCase = testCaseRepository.findTestCasesByOperationId(operationId);
        JSONArray jsonArray = new JSONArray();


        for(int i = 0; i < testCase.size(); i++){


            jsonArray.put(testCase.get(i));

            System.out.println(testCase.get(i));
        }

        return jsonArray.toList().toString();
    }*/



    // run test case regularly
    // @Scheduled(fixedRate = 86400)
    public String testCaseReport() throws Exception {

        int totalTestCase = testCaseRepository.totalTestCase();
        List<Long> testCaseNodeId = testCaseRepository.getTestCaseNodeId();
        Operation operation = new Operation();
        Map<String,ArrayList<Integer>> testingResultMultiMap = new HashMap<>();

        //date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date testingDate = new Date();
        String date = formatter.format(testingDate);
        System.out.println(date);
        System.out.println(totalTestCase);

        // Testing
        for(int i = 0; i < totalTestCase; i++){

            TestCase testCase = testCaseRepository.findTestCasesByTestCaseId(testCaseNodeId.get(i));
            Long operationId = operationRepository.findOperationIdByTestCaseId(testCaseNodeId.get(i));
            int testCaseNumber = testCaseRepository.numberOfTestCaseByOperationId(operationId);


            //set test case number

            String testCaseData = "{\"operationId\":\""+operationId+"\","+testCase.toString().substring(1);
            System.out.println("===========start===========");
            System.out.println(testCaseData);
            System.out.println("===========end===========");
            String result = runTestCase(testCaseData);




                if(testingResultMultiMap.isEmpty()){
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(1);
                    list.add(testCaseNumber);
                    testingResultMultiMap.put(operationId + "#" + date, list);


                }
                // check the testing result
                for(String key: testingResultMultiMap.keySet()){
                    if(!result.contains("Error")){
                        testCase.setTestingResult(date.replace(" ","-")+"-pass");
                        testCaseRepository.save(testCase);
                        //System.out.println(key.equals(operationId + "#" + date));

                        if(key.equals(operationId + "#" + date)){
                            testingResultMultiMap.get(key).set(0,testingResultMultiMap.get(key).get(0)+1);

                            break;
                        }
                        else{
                            System.out.println("KEY"+key);
                            System.out.println(operationId + "#" + date);
                            ArrayList<Integer> list = new ArrayList<>();
                            list.add(1);
                            list.add(testCaseNumber);
                            testingResultMultiMap.put(operationId + "#" + date, list);

                            break;
                        }

                    }
                    // remove the wrong test case of the client side
                    else{
                        if(result.startsWith("4",7)) {
                            if(key.equals(operationId + "#" + date)) {

                                testingResultMultiMap.get(key).set(1, testingResultMultiMap.get(key).get(1) - 1);


                            }
                        }
                        testCase.setTestingResult(date.replace(" ","-")+"-fail");
                        testCaseRepository.save(testCase);
                        break;
                    }
                }




        }
        for(String key: testingResultMultiMap.keySet()){
            Operation operation1 = operationRepository.findOperationByOperationId(new Long(key.split("#")[0]));
            operation1.setTestingResult(key.split("#")[1].replace(" ","-") +"-"+ testingResultMultiMap.get(key).get(0)+"/"+testingResultMultiMap.get(key).get(1));
            System.out.println(key.split("#")[1]);
            System.out.println(testingResultMultiMap.get(key).get(0)+"/"+testingResultMultiMap.get(key).get(1));
            neo4jToDatabase.update(operation1);
        }

        return "Report";
    }

    // get the test case from resourceId
    public String testCaseList(Long resourceId) {
        List<TestCase> testCase = testCaseRepository.allTestCaseByResourceId(resourceId);
        List<Operation> operations = operationRepository.findOperationsByResource(resourceId);
        /*for(int i = 0; i < operations.size(); i++){
            System.out.println(operations.get(i).getTestingResult());
        }*/

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //JsonElement jsonElement = new JsonParser().parse(StringUtils.join(testCase,","));
        //String json = gson.toJson(jsonElement);
        return StringUtils.join(testCase,",");
    }
    // get the test case and check the login status
    public String getTestCaseList(Long operationId, String token){
        List<TestCase> testCase = testCaseRepository.findTestCasesByOperationId(operationId);
        Resource testCaseResource = testCaseRepository.getResourceByOperaionId(operationId);
        Path testCasePath = testCaseRepository.getPathByOperaionId(operationId);
        JSONArray jsonArray = new JSONArray();
        System.out.println(token);
        if(token.length() > 8)  token = token.substring(7);

        for(int i = 0; i < testCase.size(); i++){
            //System.out.println(testCase.get(i).getParameter());
            JSONArray paramArray = new JSONArray(testCase.get(i).getParameter());


            if(testCase.get(i).isPrivateTestCase()){

                if(token.length() > 8 && jwtUtils.validateJwtToken(token)){
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    if(testCase.get(i).getProvider().equals(username)){
                        jsonArray.put(testCase.get(i).toString().replaceFirst("\\{","{\"nodeId\":"+testCase.get(i).getNodeId()+",\"url\":\"" + testCaseResource.getBasePath() + "\",\"path\":\""  + testCasePath.getPath() +"\","));
                        System.out.println("enter");
                    }
                }

            }
            else{

                if(token.length() > 8 && jwtUtils.validateJwtToken(token)){
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    if(testCase.get(i).getProvider().equals(username)){
                        jsonArray.put(testCase.get(i).toString().replaceFirst("\\{","{\"nodeId\":"+testCase.get(i).getNodeId()+",\"basePath\":\"" + testCaseResource.getBasePath()+ "\",\"path\":\"" + testCasePath.getPath()+"\","));

                    }
                    else{
                        for (int j = 0; j < paramArray.length(); j++ ){

                            if(paramArray.getJSONObject(j).get("name").toString().contains("token") || paramArray.getJSONObject(j).get("name").toString().contains("Authorization") || paramArray.getJSONObject(j).get("name").toString().contains("password")){
                                paramArray.getJSONObject(j).put("value","***********");
                            }
                        }
                        testCase.get(i).setParameters(paramArray.toString());
                        jsonArray.put(testCase.get(i).toString().replaceFirst("\\{","{\"nodeId\":"+testCase.get(i).getNodeId()+",\"basePath\":\"" + testCaseResource.getBasePath()+ "\",\"path\":\"" + testCasePath.getPath()+"\","));

                    }

                }
                else{
                    for (int j = 0; j < paramArray.length(); j++ ){

                        if(paramArray.getJSONObject(j).get("name").toString().contains("token") || paramArray.getJSONObject(j).get("name").toString().contains("Authorization") || paramArray.getJSONObject(j).get("name").toString().contains("password")){
                            paramArray.getJSONObject(j).put("value","***********");
                        }
                    }
                    testCase.get(i).setParameters(paramArray.toString());
                    jsonArray.put(testCase.get(i).toString().replaceFirst("\\{","{\"nodeId\":"+testCase.get(i).getNodeId()+",\"basePath\":\"" + testCaseResource.getBasePath()+ "\",\"path\":\"" + testCasePath.getPath()+"\","));

                }
            }

            //System.out.println(testCase.get(i).toString().replaceFirst("\\{","{\"nodeId\":"+testCase.get(i).getNodeId()+","));

        }

        //Operation operation = operationRepository.findOperationByOperationId(operationId);
        return jsonArray.toList().toString();

    }
}
