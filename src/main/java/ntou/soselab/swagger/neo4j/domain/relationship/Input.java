package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Parameter;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="input")
public class Input {
    @GraphId
    Long graphId;

    @StartNode
    Operation operation ;

    @EndNode
    Parameter parameter;

    public Input() {}

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public void addInputAndParameter(Operation operation, Parameter parameter){
        this.operation = operation;
        this.parameter = parameter;
        addInputToOperationAndParameter();
    }
    private void addInputToOperationAndParameter(){
        if(this.operation != null){
            this.operation.setInput(this);
        }
        if(this.parameter != null){
            this.parameter.setInput(this);
        }
    }

}
