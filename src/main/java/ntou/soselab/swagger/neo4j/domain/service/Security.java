package ntou.soselab.swagger.neo4j.domain.service;

import io.swagger.v3.oas.models.security.SecurityScheme;

//@NodeEntity
public class Security extends ConcreteService{


    SecurityScheme securityScheme;
    String provider;



    public Security(){super();}

    public Security(SecurityScheme securityScheme, String provider){

        this.securityScheme = securityScheme;
        this.provider = provider;
    }





    /*@Relationship(type = "annotate", direction = Relationship.INCOMING)
    Set<Annotate> securityAnnotate = new HashSet<>();


    public Set<Annotate> getAnnotate() {
        return securityAnnotate;
    }

    public void setAnnotate(Annotate securityAnnotate) { this.securityAnnotate.add(securityAnnotate); }*/
}
