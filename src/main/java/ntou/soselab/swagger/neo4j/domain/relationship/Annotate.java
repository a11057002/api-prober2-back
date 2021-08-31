package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.domain.service.Security;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="annotate")
public class Annotate {

    @GraphId
    Long graphId;

    @StartNode
    Resource resource;

    @EndNode
    Security securityScheme;


    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Security getSecurityScheme() {
        return securityScheme;
    }

    public void setSecurityScheme(Security securityScheme) {
        this.securityScheme = securityScheme;
    }

    public void addRelationshipToResourceAndSecurity(Resource resource, Security securityScheme){
        this.resource = resource;
        this.securityScheme = securityScheme;

        if(resource!=null){
            this.resource.setAnnotate(this);
        }

    }


}
