package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Test;
import ntou.soselab.swagger.neo4j.domain.service.TestCase;

public class TestCaseGraph {

    TestCase testCase;
    Test test;

    public TestCaseGraph() {
    }

    public TestCaseGraph(TestCase testCase) {
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }
}
