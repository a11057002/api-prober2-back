package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Response;
import ntou.soselab.swagger.neo4j.domain.service.StatusCode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="output")
public class Output {
    @GraphId
    Long graphId;

    @StartNode
    Operation operation ;

    @EndNode
    StatusCode statusCode;

    public Output() {
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    private void addOutputToOperationAndStatusCode(){
        if(this.operation != null){
            this.operation.setOutput(this);
        }

        if(this.statusCode != null){
            this.statusCode.setOutput(this);
        }
    }

    public void addOperationAndStatusCode(Operation operation, StatusCode statusCode){
        this.operation = operation;
        this.statusCode = statusCode;
        addOutputToOperationAndStatusCode();
    }
}
