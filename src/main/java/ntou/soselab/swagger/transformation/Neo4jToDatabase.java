package ntou.soselab.swagger.transformation;

import ntou.soselab.swagger.neo4j.domain.relationship.*;
import ntou.soselab.swagger.neo4j.domain.service.*;
import ntou.soselab.swagger.neo4j.graph.*;
import ntou.soselab.swagger.neo4j.repositories.relationship.*;
import ntou.soselab.swagger.neo4j.repositories.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Neo4jToDatabase {

    Logger log = LoggerFactory.getLogger(Neo4jToDatabase.class);


    @Autowired
    EndpointRepository endpointRepository;
    @Autowired
    InputRepository inputRepository;
    @Autowired
    OutputRepository outputRepository;
    @Autowired
    HaveRepository haveRepository;
    @Autowired
    ActionRepository actionRepository;
    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    ParameterRepository parameterRepository;
    @Autowired
    ResponseRepository responseRepository;
    @Autowired
    OperationRepository operationRepository;
    @Autowired
    AnnotateRepository annotateRepository;
    @Autowired
    SecurityRepository securityRepository;
    @Autowired
    OwnRepository ownRepository;
    @Autowired
    PossessRepository possessRepository;
    @Autowired
    TestRepository testRepository;

    @Transactional(rollbackFor=Exception.class)
    public void buildRelationshipStartWithResource(ResourceGraph resourceGraph) {
        log.info("Start store node to neo4j");
        log.info("start resource parsing: {}", resourceGraph.getResource().getTitle());
        log.info("first step: build Relationships Between ConcreteServices");




        buildRelationshipsBetweenConcreteServices(resourceGraph);

        //buildPossessBetweenResourceAndOAS(resourceGraph.getResource(),resourceGraph.getOasGraph().getOas(),resourceGraph.getOasGraph().getPossess());


        // 將 resource 每個 path 裡面的 operation 先建構起來
        for(PathGraph pathGraph: resourceGraph.getPathGraphs()){
            log.info("build Relationships Between ConcreteServices");
            log.info("Path info :{}", pathGraph.getPath().getPath());
            buildRelationshipsBetweenConcreteServices(pathGraph);

            // 再將 operation 與 path 建構起來
            for(OperationGraph operationGraph : pathGraph.getOperationGraphs()) {
                log.info("build Relationship Between Path and Operation");
                buildActionBetweenPathAndOperation(pathGraph.getPath(), operationGraph.getOperation(), operationGraph.getAction());
            }
        }



        log.info("second step: save Resource And Relationship With Path");
        for(PathGraph pathGraph: resourceGraph.getPathGraphs()){
            log.info("save Resource And Relationship With Path");
            saveResourceAndRelationshipWithPath(resourceGraph.getResource(), pathGraph.getPath(), pathGraph.getEndpoint());
        }

    }
    public void buildRelationshipsBetweenConcreteServices(ResourceGraph resourceGraph){
        if(resourceGraph.getSecuritySchemeGraphs() != null){
            for(SecurityGraph securityGraph:resourceGraph.getSecuritySchemeGraphs()){
                saveSecurityAnnotateWithResource(resourceGraph.getResource(),securityGraph.getSecurity(),securityGraph.getAnnotate());
            }
        }
        //saveSecurityAnnotateWithResource(resourceGraph.getResource(),resourceGraph.getSecuritySchemeGraphs().get(securityGraph).getSecurity(),resourceGraph.getSecuritySchemeGraphs().getAnnotate());


    }
    public void buildRelationshipsBetweenConcreteServices(PathGraph pathGraph){

        // 將每個 path 裡面的操作都建構起來
        for(OperationGraph operationGraph : pathGraph.getOperationGraphs()) {
            for(ParameterGraph parameterGraph: operationGraph.getParameterGraphs()){
                buildInputBetweenConcreteServices(operationGraph.getOperation(), parameterGraph.getParameter(), parameterGraph.getInput());
            }
            for(StatusCodeGraph statusCodeGraph: operationGraph.getStatusCodeGraphs()){

                for(ResponseGraph responseGraph :  statusCodeGraph.getResponseGraphs()){
                    buildHaveBetweenConcreteServices(statusCodeGraph.getStatusCode(), responseGraph.getResponse(), responseGraph.getHave());
                }

                buildOutputBetweenConcreteServices(operationGraph.getOperation(), statusCodeGraph.getStatusCode(), statusCodeGraph.getOutput());
            }
        }
    }

    private void buildInputBetweenConcreteServices(Operation operation, Parameter parameter, Input input){
        // Add input relationship
        input.addInputAndParameter(operation, parameter);
        inputRepository.save(input);
    }

    private void buildOutputBetweenConcreteServices(Operation operation, StatusCode statusCode, Output output){

        // Add Output relationship
        output.addOperationAndStatusCode(operation, statusCode);
        outputRepository.save(output);
    }

    private void buildHaveBetweenConcreteServices(StatusCode statusCode, Response response, Have have){

        // Add Have relationship
        have.addStatusCodeAndResponse(statusCode, response);
        haveRepository.save(have);
    }

    private void buildActionBetweenPathAndOperation(Path path, Operation operation, Action action) {
        action.addRelationshipToResourceAndPath(path, operation);
        actionRepository.save(action);
    }
    public void buildPossessBetweenResourceAndOAS(Resource resource, OAS oas, Possess possess) {

        possess.addPossessToResourceAndOAS(resource, oas);
        possessRepository.save(possess);
    }

    // save Resource and Action relationship with Operation
    private void saveResourceAndRelationshipWithPath(Resource resource, Path path, Endpoint endpoint){
        endpoint.addRelationshipToResourceAndPath(resource, path);
        endpointRepository.save(endpoint);
    }

    public void saveSecurityAnnotateWithResource(Resource resource, Security securityScheme, Annotate annotate){
        annotate.addRelationshipToResourceAndSecurity(resource,securityScheme);
        //System.out.println(annotate.getSecurityScheme());
        annotateRepository.save(annotate);
    }

    public void  saveSecurityDataOwnWithResource(Resource resource, SecurityData securityData, Own own){
        own.addRelationshipToResourceAndSecurity(resource,securityData);
        ownRepository.save(own);
    }
    public void  saveParameterWithOperation(Operation operation, Parameter parameter, Input input){
        input.addInputAndParameter(operation,parameter);
        inputRepository.save(input);
    }
    /*public void updateSecurityAnnotateWithResource(Security securityScheme){

        System.out.println("update");

        //annotate.addRelationshipToResourceAndSecurity(resource,securityScheme);

        securityRepository.save(securityScheme);

    }*/
    public void saveTestCaseWithOperation(Operation operation, TestCase testCase, Test test){
        test.addRelationshipToOperationAndTestCase(operation, testCase);
        testRepository.save(test);
    }

    public void update(Operation operation){
        operationRepository.save(operation);
    }


}
