package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.TestCase;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="test")
public class Test  {

    @GraphId
    Long graphId;

    @StartNode
    Operation operation;

    @EndNode
    TestCase testCase;

    public Test() {
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }


    public void addRelationshipToOperationAndTestCase(Operation operation, TestCase testCase){
        this.operation = operation;
        this.testCase = testCase;

        if(this.operation != null){
            this.operation.setTest(this);
        }
        if(this.testCase != null){
            this.testCase.setTest(this);
        }
    }
}
