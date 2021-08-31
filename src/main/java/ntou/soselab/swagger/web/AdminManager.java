package ntou.soselab.swagger.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.TestCase;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.PathRepository;
import ntou.soselab.swagger.neo4j.repositories.service.TestCaseRepository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdminManager {

    @Autowired
    PathRepository pathRepository;
    @Autowired
    OperationRepository operationRepository;
    @Autowired
    TestCaseRepository testCaseRepository;

    public String displayAllTestCase(Long resourceId){

        return null;
    }
    public String getTestCaseList(Long operationId){
        List<TestCase> testCase = testCaseRepository.findTestCasesByOperationId(operationId);
        JSONArray jsonArray = new JSONArray();
        JsonObject jsonObject = new JsonObject();

        for(int i = 0; i < testCase.size(); i++){


            jsonArray.put(testCase.get(i).toString().replaceFirst("\\{","{\"nodeId\":"+testCase.get(i).getNodeId()+","));

           // System.out.println(testCase.get(i).toString().replaceFirst("\\{","{\"nodeId\":"+testCase.get(i).getNodeId()+","));

        }

        Operation operation = operationRepository.findOperationByOperationId(operationId);
        return jsonArray.toList().toString();

    }

    public void editTestCase(String testCaseData){
        JsonObject jsonObject = new JsonParser().parse(testCaseData).getAsJsonObject();
        System.out.println(jsonObject);
        Long testCaseId = jsonObject.get("testCaseId").getAsLong();
        TestCase testCase = testCaseRepository.findTestCasesByTestCaseId(testCaseId);
        testCase.setJsonPath(jsonObject.get("jsonPath").getAsString());
        testCase.setExpectedPartialResult(jsonObject.get("expectedPartialResult").getAsString());
        System.out.println(jsonObject.get("parameters").getAsJsonArray().toString());
        testCase.setParameters(jsonObject.get("parameters").getAsJsonArray().toString());
        testCaseRepository.save(testCase);

    }
    public String getAllTestCaseByOperationId(Long operationId){

        List<TestCase> testCase = testCaseRepository.findTestCasesByOperationId(operationId);

        JSONArray jsonArray = new JSONArray();

        for(int i = 0; i < testCase.size(); i++){
            JSONArray paramArray = new JSONArray(testCase.get(i).getParameter());

            for (int j = 0; j < paramArray.length(); j++ ){

                if(paramArray.getJSONObject(j).get("name").toString().contains("token") || paramArray.getJSONObject(j).get("name").toString().contains("oauth_token")  || paramArray.getJSONObject(j).get("name").toString().contains("Authorization") || paramArray.getJSONObject(j).get("name").toString().contains("password")){
                    paramArray.getJSONObject(j).put("value","***********");
                }
            }
            testCase.get(i).setParameters(paramArray.toString());
            jsonArray.put(testCase.get(i).toString().replaceFirst("\\{","{\"nodeId\":"+testCase.get(i).getNodeId()+","));

        }

        Operation operation = operationRepository.findOperationByOperationId(operationId);
        return operation.getTestingResult().toString() + "#" + jsonArray.toList().toString();

    }

    public String getOperationList(Long resourceId){
        List<Path> path = pathRepository.findPathsByResource(resourceId);
        JSONArray jsonArray = new JSONArray();

        for(int i = 0; i < path.size(); i++){
            List<Operation> operation = operationRepository.findOperationsByPath(path.get(i).getNodeId());
            for(int j = 0; j < operation.size(); j++){
                jsonArray.put(operation.get(j).getOperationAction()+"#"+path.get(i).getPath()+"#"+operation.get(j).getNodeId());
            }


        }
        return jsonArray.toList().toString();
    }
}
