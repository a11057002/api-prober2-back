package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.Parameter;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParameterRepository extends GraphRepository<Parameter> {

    @Query("MATCH (n:Resource)-[a:endpoint]-(m:Path)-[s:action]-(r:Operation)-[w:input]-(x:Parameter) WHERE id(n)= {id} RETURN x")
    List<Parameter> findParametersByResource(@Param("id") Long id);

    @Query("MATCH (r:Operation)-[w:input]-(x:Parameter {required: true}) WHERE id(r)= {id} RETURN x")
    List<Parameter> findParametersByOperation(@Param("id") Long id);

    @Query("MATCH (r:Operation)-[w:input]-(x:Parameter) WHERE id(r)= {id} RETURN x")
    List<Parameter> findParametersByOperationNoThreshold(@Param("id") Long id);


    /*@Query("MATCH (n:Resource)-[a:endpoint]-(m:Path)-[s:action]-(r:Operation)-[w:input]-(x:Parameter) WHERE id(r)=5250  RETURN x")
    List<Parameter> findParametersByResource*/
}

