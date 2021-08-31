package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Test;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class TestCase extends ConcreteService {

    String parameters;

    String jsonPath;
    String expectedPartialResult;
    boolean expectedPartialResultBool = false;
    String provider = "User";
    ArrayList<String> testingResults;
    boolean privateTestCase = false;

    public TestCase(){}
    public TestCase(String parameters, String jsonPath, String expectedPartialResult, String provider, boolean privateTestCase) {

        this.parameters = parameters;
        this.jsonPath = jsonPath;
        this.expectedPartialResult = expectedPartialResult;
        this.provider = provider;
        this.privateTestCase = privateTestCase;
    }

    public String getParameter() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getExpectedPartialResult() {
        return expectedPartialResult;
    }

    public void setExpectedPartialResult(String expectedPartialResult) {
        this.expectedPartialResult = expectedPartialResult;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public ArrayList<String> getTestingResults() {
        return testingResults;
    }

    public void setTestingResults(ArrayList<String> testingResults) {
        this.testingResults = testingResults;
    }
    public void setTestingResult(String testingResult) {
        if (testingResults == null) {
            testingResults = new ArrayList<>();
        }
        testingResults.add(testingResult);
    }

    public boolean isPrivateTestCase() {
        return privateTestCase;
    }

    public void setPrivateTestCase(boolean privateTestCase) {
        this.privateTestCase = privateTestCase;
    }

    @Override
    public String toString() {
        //System.out.println(getTestingResults());
        return "{\"parameters\":"+getParameter()+",\"jsonPath\":\""+getJsonPath()+"\",\"expectedPartialResult\":\""+getExpectedPartialResult()+"\",\"provider\":\""+getProvider()+"\",\"testingResults\":\""+ getTestingResults()+"\",\"private\":\""+privateTestCase+"\"}";
    }


    @Relationship(type = "test", direction = Relationship.INCOMING)
    Set<Test> tests = new HashSet<>();

    public Set<Test> getTests() {
        return tests;
    }

    public void setTest(Test test) {
        this.tests.add(test);
    }
}
