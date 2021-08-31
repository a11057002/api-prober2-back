package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Annotate;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Jwt extends Security {
    String httpName = "HttpSecurity";
    String type;
    String description;
    String scheme = "bearer";
    String bearerFormat = "JWT";
    String provider = "original";

    public Jwt() {
    }

    public Jwt(String type, String description, String scheme, String bearerFormat, String provider){
        this.type = type;
        this.description = description;
        this.scheme = scheme;
        this.bearerFormat = bearerFormat;
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

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getBearerFormat() {
        return bearerFormat;
    }

    public void setBearerFormat(String bearerFormat) {
        this.bearerFormat = bearerFormat;
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
