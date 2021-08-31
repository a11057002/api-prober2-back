package ntou.soselab.swagger.neo4j.domain.relationship;

import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Response;
import ntou.soselab.swagger.neo4j.domain.service.StatusCode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="have")
public class Have {
    @GraphId
    Long graphId;

    @StartNode
    StatusCode statusCode ;

    @EndNode
    Response response;

    public Have() {}

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    private void addHaveToStatusCodeAndResponse(){
        if(this.statusCode != null){
            this.statusCode.setHave(this);
        }

        if(this.response != null){
            this.response.setHave(this);
        }
    }

    public void addStatusCodeAndResponse(StatusCode statusCode, Response response){
        this.statusCode = statusCode;
        this.response = response;
        addHaveToStatusCodeAndResponse();
    }
}
