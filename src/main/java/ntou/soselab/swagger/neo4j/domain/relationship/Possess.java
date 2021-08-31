package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.OAS;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="possess")
public class Possess {
    @GraphId
    Long graphId;

    @StartNode
    Resource resource;

    @EndNode
    OAS oas;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public OAS getOas() {
        return oas;
    }

    public void setOas(OAS oas) {
        this.oas = oas;
    }

    public Possess() {
    }

    public Possess(Resource resource, OAS oas) {
        this.resource = resource;
        this.oas = oas;
    }
    public void addPossessToResourceAndOAS(Resource resource, OAS oas){
        this.resource = resource;
        this.oas = oas;
    }
}
