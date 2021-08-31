package ntou.soselab.swagger.neo4j.domain.relationship;


import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.domain.service.SecurityData;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="own")
public class Own {

    @GraphId
    Long graphId;

    @StartNode
    Resource resource;

    @EndNode
    SecurityData securityData;




    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public SecurityData getSecurityData() {
        return securityData;
    }

    public void setSecurityData(SecurityData securityData) {
        this.securityData = securityData;
    }


    public void addRelationshipToResourceAndSecurity(Resource resource, SecurityData securityData){
        this.resource = resource;
        this.securityData = securityData;

        if(resource!=null){
            this.resource.setOwn(this);
        }

    }
}
