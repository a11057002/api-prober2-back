package ntou.soselab.swagger.neo4j.repositories.service;

import ntou.soselab.swagger.neo4j.domain.service.TestCase;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestCaseRepository extends GraphRepository<TestCase> {


    @Query("MATCH (n:TestCase) RETURN count(n)")
    int totalTestCase();

    @Query("MATCH (n:TestCase) RETURN ID(n)")
    List<Long> getTestCaseNodeId();

    @Query("MATCH (m:TestCase)-[s:test]-(r:Operation) WHERE id(r)= {id}  RETURN m")
    List<TestCase> findTestCasesByOperationId(@Param("id") Long id);

    @Query("MATCH (m:TestCase)-[s:test]-(r:Operation) WHERE id(r)= {id}  RETURN count(m)")
    int numberOfTestCaseByOperationId(@Param("id") Long id);

    @Query("MATCH (m:TestCase) WHERE id(m)= {id}  RETURN m")
    TestCase findTestCasesByTestCaseId(@Param("id") Long id);


    @Query("MATCH (m:TestCase)-[t:test]-(o:Operation)-[a:action]-(p:Path)-[e:endpoint]-(r:Resource) WHERE id(r)= {id}  RETURN m")
    List<TestCase> allTestCaseByResourceId(@Param("id") Long id);

}
