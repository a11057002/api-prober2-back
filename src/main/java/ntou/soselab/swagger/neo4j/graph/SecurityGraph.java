package ntou.soselab.swagger.neo4j.graph;

import ntou.soselab.swagger.neo4j.domain.relationship.Annotate;

import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.domain.service.Security;


public class SecurityGraph {


    Security security;
    //Resource resource;
    Annotate securityAnnotate;

    public SecurityGraph(){ super();}
    public SecurityGraph(Security securityScheme){
        this.security = securityScheme;
    }
    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security securityScheme) {
        this.security = securityScheme;
    }

    public Annotate getAnnotate() {
        return securityAnnotate;
    }

    public void setAnnotate(Annotate securityAnnotate) {
        this.securityAnnotate = securityAnnotate;
    }


}
