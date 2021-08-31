package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.OAS;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

public interface OASRepository extends GraphRepository<OAS> {

    @Query("MATCH (r:Resource)-[w:possess]-(x:OAS) WHERE id(r)={id} RETURN x")
    OAS findOASByResourceId(@Param("id") Long id);
}
