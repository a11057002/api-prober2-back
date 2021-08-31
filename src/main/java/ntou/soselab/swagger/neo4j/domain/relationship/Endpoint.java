package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="endpoint")
public class Endpoint {
    @GraphId
    Long graphId;

    @StartNode
    Resource resource;

    @EndNode
    Path path;

    public Endpoint() {}

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void addRelationshipToResourceAndPath(Resource resource, Path path){
        this.resource=resource;
        this.path=path;

        if(this.resource != null){
            this.resource.setEndpoints(this);
        }
        if(this.path != null){
            this.path.setEndpoints(this);
        }
    }

}
