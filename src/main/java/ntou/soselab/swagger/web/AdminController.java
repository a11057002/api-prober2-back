package ntou.soselab.swagger.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
public class AdminController {

    @Autowired
    AdminManager adminManager;


    @CrossOrigin
    @RequestMapping(value = "/displayAllTestCase/{id}", method = RequestMethod.GET)
    public String getAllTestCase(@PathVariable("id")Long resourceId) {
        return adminManager.displayAllTestCase(resourceId);
    }

    @CrossOrigin
    @RequestMapping(value = "/getAllTestCase/{id}", method = RequestMethod.GET)
    public String getAllTestCaseByOperationId(@PathVariable("id")Long operationId) {

        return adminManager.getAllTestCaseByOperationId(operationId);
    }

    @CrossOrigin
    @RequestMapping(value = "/getTestCaseList/{id}", method = RequestMethod.GET)
    public String getTestCaseList(@PathVariable("id")Long operationId) {
        return adminManager.getTestCaseList(operationId);
    }

    @CrossOrigin
    @RequestMapping(value = "/editTestCase", method = RequestMethod.POST)
    public void editTestCase(@RequestBody String testCaseData) {
        adminManager.editTestCase(testCaseData);
    }
    @CrossOrigin
    @RequestMapping(value = "/getOperationList/{id}", method = RequestMethod.GET)
    public String getOperationList(@PathVariable("id")Long resourceId) {
        return adminManager.getOperationList(resourceId);
    }

}
