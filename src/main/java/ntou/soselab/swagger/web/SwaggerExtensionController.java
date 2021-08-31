package ntou.soselab.swagger.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import ntou.soselab.swagger.neo4j.repositories.service.OASRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SwaggerExtensionController {

    @Autowired
    SecurityManager securityManager;
    @Autowired
    TestManager testManager;
    @Autowired
    OASRepository oasRepository;


    @CrossOrigin
    @RequestMapping(value = "/addSecurityScheme", method = RequestMethod.POST)
    // dd security schemes
    public String addSecurityScheme(@RequestBody String securityString) {
        return securityManager.addOASSecurityScheme(securityString);
    }
    @CrossOrigin
    @RequestMapping(value = "/tryEndpoint", method = RequestMethod.POST)
    // endpoint testing in try area (frontend UI)
    public String tryEndpoint(@RequestBody String endpointData) {
        return testManager.tryEndpoint(endpointData);
    }

    /*@CrossOrigin
    @RequestMapping(value = "/addSecurityValue", method = RequestMethod.POST)
    public String addSecurityValue(@RequestBody String securityData) {
        return serviceManager.addSecurityValue(securityData);
    }*/
    @CrossOrigin
    @RequestMapping(value = "/authorize", method = RequestMethod.POST)
    // check oauth flow
    public String checkOAuth2Flow(@RequestBody String securityData) {

        return securityManager.checkOAuth2Flow(securityData);
    }

    @CrossOrigin
    @RequestMapping(value = "/implicit", method = RequestMethod.GET)
    // get token in Implicit Flow
    public void getImplicitToken(){

    }

    /*@CrossOrigin
    @RequestMapping(value = "/cc", method = RequestMethod.POST)
    public String getPasswordToken(@RequestBody String str){
        return "dhgf4gf53j438ryt4ut3g";
    }*/
    @CrossOrigin
    @RequestMapping(value = "/auth", method = RequestMethod.GET)
    // get token in Authorization Code Flow
    public String getAccessToken(@RequestParam String code){
        return securityManager.getToken(code);

    }


    @CrossOrigin
    @RequestMapping(value = "/getOASDoc/{id}", method = RequestMethod.GET, produces = "application/json")
    // get OAS document from resourceId
    public String getOASDoc(@PathVariable("id")String resourceId){
        Long id = new Long(resourceId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = new JsonParser().parse(oasRepository.findOASByResourceId(id).getProberVersionOAS());
        String json = gson.toJson(jsonElement);
        return json;

    }



    @CrossOrigin
    @RequestMapping(value = "/runTestCase", method = RequestMethod.POST)
    // run test case
    public String runTestCase(@RequestBody String testCaseData) throws Exception {
        return testManager.runTestCase(testCaseData);
        //System.out.println(testCaseData);

    }

    @CrossOrigin("*")
    @RequestMapping(value = "/saveTestCase", method = RequestMethod.POST)
    // store the test case to neo4j
    public String saveTestCase(@RequestBody String testCaseData){
        return testManager.saveTestCase(testCaseData);
    }


    /*@CrossOrigin
    @RequestMapping(value = "/importTestCase/{id}", method = RequestMethod.GET)
    public String importTestCase(@PathVariable("id")Long operationId){
        return testManager.importTestCase(operationId);
    }*/

    @CrossOrigin
    @RequestMapping(value = "/testCaseList/{id}", method = RequestMethod.GET)
    // get the test cases from  operationId
    public String getTestCaseList(@PathVariable("id")Long operationId, @RequestHeader (name="Authorization") String token) {
        return testManager.getTestCaseList(operationId, token);
    }

    /*@CrossOrigin
    @RequestMapping(value = "/runTestCaseById/{id}", method = RequestMethod.GET)
    public String runTestCaseById(@PathVariable("id")Long testCaseId) {
        return testManager.runTestCaseById(testCaseId);
    }*/


    @CrossOrigin
    @RequestMapping(value = "/testCaseReport", method = RequestMethod.GET)
    // run the test cases regularly
    public String testCaseReport() throws Exception {
        return testManager.testCaseReport();
    }

    @CrossOrigin
    @RequestMapping(value = "/testCaseList/{id}", method = RequestMethod.GET, produces = "application/json")
    // get the test cases from  resourceId
    public String testCaseList(@PathVariable("id")Long resourceId){
        return testManager.testCaseList(resourceId);
    }


}
