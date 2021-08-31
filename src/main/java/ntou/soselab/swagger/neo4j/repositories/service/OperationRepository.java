package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OperationRepository extends GraphRepository<Operation> {

    @Query("MATCH (n:Operation) RETURN count(n)")
    int totalOperation();

    @Query("MATCH (n:Resource)-[a:endpoint]-(m:Path)-[s:action]-(r:Operation) WHERE id(n)= {id} RETURN r")
    List<Operation> findOperationsByResource(@Param("id") Long id);

    @Query("MATCH (m:Path)-[s:action]-(r:Operation) WHERE id(r)= {id} RETURN m")
    Path findPathByOperation(@Param("id") Long id);

    @Query("MATCH (m:Path)-[s:action]-(r:Operation) WHERE id(m)= {id} RETURN r")
    List<Operation> findOperationsByPath(@Param("id") Long id);

    @Query("MATCH (n:Resource)-[a:endpoint]-(m:Path)-[s:action]-(r:Operation) WHERE id(r)= {id} RETURN n")
    Resource findResourceByOperation(@Param("id") Long id);

    @Query("MATCH (n:Operation) WHERE id(n)={id} RETURN n.operationAction")
    String findActionsByOperationId(@Param("id") Long id);

    @Query("MATCH (n:Operation) WHERE id(n)={id} RETURN n")
    Operation findOperationByOperationId(@Param("id") Long id);


    @Query("MATCH (n:Operation)-[s:test]-(m:TestCase) WHERE id(m)={id} RETURN ID(n)")
    Long findOperationIdByTestCaseId(@Param("id") Long id);

}

