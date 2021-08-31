package ntou.soselab.swagger.neo4j.repositories.service;


import ntou.soselab.swagger.neo4j.domain.service.StatusCode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StatusCodeRepository extends GraphRepository<StatusCode> {
    @Query("MATCH (r:Operation)-[w:output]-(x:StatusCode) WHERE id(r)= {id} RETURN x")
    List<StatusCode> findStatusCodesByOperation(@Param("id") Long id);
}
