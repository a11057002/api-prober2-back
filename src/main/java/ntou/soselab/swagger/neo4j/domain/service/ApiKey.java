package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Annotate;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class ApiKey extends Security{
    String apiKeyName = "ApiKeySecurity";
    String type;
    String description;
    String in;
    String name;
    String provider = "original";

    public ApiKey(){

    }
    public ApiKey(String type, String description, String in, String name, String apiKeyName, String provider){
        this.type = type;
        this.description = description;
        this.in = in;
        this.name = name;
        this.apiKeyName = apiKeyName;
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Relationship(type = "annotate", direction = Relationship.INCOMING)
    Set<Annotate> securityAnnotate = new HashSet<>();


    public Set<Annotate> getAnnotate() {
        return securityAnnotate;
    }

    public void setAnnotate(Annotate securityAnnotate) { this.securityAnnotate.add(securityAnnotate); }


}
