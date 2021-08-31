package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Annotate;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;


@NodeEntity
public class OAuth2 extends Security {
    String oauth2Name;
    String type;
    String description;
    String flow;
    String authorizationUrl=null;
    String tokenUrl=null;
    String provider = "original";
    String scope;


    public OAuth2(){}

    public OAuth2(String type, String description, String flow, String authorizationUrl, String tokenUrl, String oauth2Name, String scope, String provider) {
        this.type = type;
        this.description = description;
        this.flow = flow;
        this.authorizationUrl = authorizationUrl;
        this.tokenUrl = tokenUrl;
        this.oauth2Name = oauth2Name;
        this.scope = scope;
        this.provider = provider;
    }

    public String getOauth2Name() {
        return oauth2Name;
    }

    public void setOauth2Name(String oauth2Name) {
        this.oauth2Name = oauth2Name;
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

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Relationship(type = "annotate", direction = Relationship.INCOMING)
    Set<Annotate> securityAnnotate = new HashSet<>();


    public Set<Annotate> getAnnotate() {
        return securityAnnotate;
    }

    public void setAnnotate(Annotate securityAnnotate) { this.securityAnnotate.add(securityAnnotate); }
}
