package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="action")
public class Action {

    @GraphId
    Long graphId;

    @StartNode
    Path path;

    @EndNode
    Operation operation;

    public Action() {}

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public void addRelationshipToResourceAndPath(Path path, Operation operation){
        this.path=path;
        this.operation=operation;

        if(this.path != null){
            this.path.setActions(this);
        }

        if(this.operation != null){
            this.operation.setActions(this);
        }
    }
}
