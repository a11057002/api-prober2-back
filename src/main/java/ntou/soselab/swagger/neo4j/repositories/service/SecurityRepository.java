package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.Security;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

public interface SecurityRepository extends GraphRepository<Security> {
    //MATCH (n:Resource)-[a:endpoint]-(m:Path) WHERE id(n)= {id} RETURN m
    //MATCH (n:Resource)-[a:securityAnnotate]-(m:ProberSeurityScheme) RETURN n,m
    @Query("MATCH (n:Resource)-[a:annotate]-(m:Security) WHERE id(n)={id} RETURN m")
    Security findAnnotationsByResourceId(@Param("id") Long id);

    @Query("MATCH (n:Resource)-[a:annotate]-(m:Security) WHERE id(n)={id} AND m.authorizationUrl IS NOT NULL RETURN m.authorizationUrl limit 1")
    String findAnnotationsAuthorizationUrlByResourceId(@Param("id") Long id);

    @Query("MATCH (n:Resource)-[a:annotate]-(m:Security) WHERE id(n)={id} AND m.tokenUrl IS NOT NULL  RETURN m.tokenUrl limit 1")
    String findAnnotationsTokenUrlByResourceId(@Param("id") Long id);
    //MATCH (n:Security) where id(n)= DETACH delete n
    //MATCH (n) DETACH DELETE n

    @Query("MATCH (r:Resource)-[a:annotate]-(s:Security) where id(r)={id} delete s,a")
    void deleteAnnotaionAndSecurityByResourceId(@Param("id") Long id);


}
