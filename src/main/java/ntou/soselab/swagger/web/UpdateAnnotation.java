package ntou.soselab.swagger.web;

import com.google.gson.JsonObject;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import ntou.soselab.swagger.neo4j.domain.relationship.Annotate;
import ntou.soselab.swagger.neo4j.domain.relationship.Input;
import ntou.soselab.swagger.neo4j.domain.service.*;
import ntou.soselab.swagger.neo4j.graph.ResourceGraph;
import ntou.soselab.swagger.neo4j.graph.SecurityGraph;
import ntou.soselab.swagger.neo4j.repositories.service.OASRepository;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import ntou.soselab.swagger.neo4j.repositories.service.SecurityRepository;
import ntou.soselab.swagger.transformation.Neo4jToDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

@Service
public class UpdateAnnotation {

    @Autowired
    OASRepository oasRepository;
    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    SecurityRepository securityRepository;
    @Autowired
    Neo4jToDatabase neo4jToDatabase;
    @Autowired
    OperationRepository operationRepository;


    public void updateResourceAuthentication(String swaggerDocument, Long resourceId, Long operationId, String provider) {

        SwaggerParseResult swaggerResult = new OpenAPIV3Parser().readContents(swaggerDocument);
        OpenAPI swagger = swaggerResult.getOpenAPI();




        Resource resource = resourceRepository.findResourceById(resourceId);
        ResourceGraph resourceGraph = new ResourceGraph(resource);

        ArrayList<String> authentications = new ArrayList<>();
        ArrayList<String> feature = new ArrayList<>();
        String authentication = "";
        Operation operation = operationRepository.findOperationByOperationId(operationId);
        //operation.setFeature();
        //securityRepository.deleteAnnotaionAndSecurityByResourceId(resourceId);

        //resourceGraph.setSecuritySchemeGraph()getSecurityInformation(OpenAPI swagger, Resource resource)
        if(swagger.getComponents().getSecuritySchemes() != null) {

            for (Map.Entry<String,SecurityScheme> entry : swagger.getComponents().getSecuritySchemes().entrySet()) {

                SecurityScheme ss = entry.getValue();


                if(ss.getType().toString().equals("oauth2")){
                    if(resource.getAuthentications() == null ){
                        resource.setAuthentication("OAuth2");
                        resource.setFeature("OAuth2");
                    }
                    else if(!resource.getAuthentications().contains("oauth2") && !resource.getFeature().contains("OAuth2")){

                        resource.setAuthentication("OAuth2");
                        resource.setFeature("OAuth2");

                    }

                }
                if(ss.getType().toString().equals("apiKey") && resource.getAuthentications()!=null){
                    if(resource.getAuthentications() == null){
                        resource.setAuthentication("apiKey");
                        resource.setFeature("apiKey");
                    }
                    else if(!resource.getAuthentications().contains("apiKey") && !resource.getFeature().contains("apiKey")){
                        resource.setAuthentication("apiKey");
                        resource.setFeature("apiKey");
                    }
                }

                if(ss.getType().toString().equals("http") && resource.getAuthentications()!=null){
                    if(resource.getAuthentications() == null){
                        resource.setAuthentication(ss.getScheme());
                        resource.setFeature("http");
                    }
                    else if(!resource.getAuthentications().contains("http") && !resource.getFeature().contains("http"))
                    {
                        resource.setAuthentication(ss.getScheme());
                        resource.setFeature("http");
                    }
                }
                //log.info("entry key:{} \n entry value : {} ", entry.getKey(),entry.getValue());



                neo4jToDatabase.saveSecurityAnnotateWithResource(resource,getSecurityInformation(entry.getKey(),ss,resourceId, provider),new Annotate());

                resourceRepository.save(resource);

            }
        }

        OAS oas = oasRepository.findOASByResourceId(resourceId);
        oas.setProberVersionOAS(swaggerDocument);
        oasRepository.save(oas);

        System.out.println(swaggerDocument);
        //resourceGraph.setResource(resource);

    }

    public void getParameter(JsonObject jsonObject){
        //JsonObject jsonObject = new JsonParser().parse(doc).getAsJsonObject();

            Parameter parameter = new Parameter();
            parameter.setName(jsonObject.get("paramName").getAsString());
            parameter.setIn(jsonObject.get("paramIn").getAsString());
            parameter.setRequired(true);

            Long operationId = jsonObject.get("operationId").getAsLong();

            Operation operation = operationRepository.findOperationByOperationId(operationId);
            Input input = new Input();

            neo4jToDatabase.saveParameterWithOperation(operation,parameter,input);






    }
    public Security getSecurityInformation(String name, SecurityScheme securityScheme, Long resourceId, String provider){
        // 獲得全域的認證機制名稱，內容分析 swagger parser 無支援
        // List<SecurityRequirement> requirements = swagger.getSecurity();



        Security security = new Security();
        Annotate annotate = new Annotate();
        SecurityGraph securitySchemeGraph = new SecurityGraph();

        String scope = "";



        //need to create the security scheme graph
        //ResourceGraph resourceGraph = getResourceInformation(swagger, resource);
        if(securityScheme.getType().toString()=="apiKey"){


            security = new ApiKey("apiKey", securityScheme.getDescription(), securityScheme.getIn().toString(), securityScheme.getName(), "", provider);

        }

        if(securityScheme.getType().toString()=="http"){
            if(securityScheme.getScheme().equals("basic")) {
                security = new Http("http", securityScheme.getDescription(), securityScheme.getScheme(), provider);

            }
            if(securityScheme.getScheme().equals("bearer")) {
                security = new Jwt("http", securityScheme.getDescription(), securityScheme.getScheme(), securityScheme.getBearerFormat(), provider);
            }
            System.out.println(security.toString());
        }
        // securityScheme.getFlows().getAuthorizationCode().getScopes();
        if(securityScheme.getType().toString()=="oauth2"){

            if(securityScheme.getFlows().getImplicit()!=null) {
                if(securityScheme.getFlows().getImplicit().getScopes()!=null){
                    for(Map.Entry<String, String> entryScope : securityScheme.getFlows().getImplicit().getScopes().entrySet()){
                        scope += entryScope.getKey() + ":" + entryScope.getValue() + ",";
                    }

                }
                security = new OAuth2("oauth2", securityScheme.getDescription(), "implicit flow", securityScheme.getFlows().getImplicit().getAuthorizationUrl(), null, name,scope, provider);

            }
            if(securityScheme.getFlows().getAuthorizationCode()!=null) {
                if(securityScheme.getFlows().getAuthorizationCode().getScopes()!=null){
                    for(Map.Entry<String, String> entryScope : securityScheme.getFlows().getAuthorizationCode().getScopes().entrySet()){
                        scope += entryScope.getKey() + ":" + entryScope.getValue() + ",";
                    }

                }
                security = new OAuth2("oauth2", securityScheme.getDescription(), "authorization code flow", securityScheme.getFlows().getAuthorizationCode().getAuthorizationUrl(), securityScheme.getFlows().getAuthorizationCode().getTokenUrl(), name,scope, provider);

            }
            if(securityScheme.getFlows().getClientCredentials()!=null) {
                if(securityScheme.getFlows().getClientCredentials().getScopes()!=null){
                    for(Map.Entry<String, String> entryScope : securityScheme.getFlows().getClientCredentials().getScopes().entrySet()){
                        scope += entryScope.getKey() + ":" + entryScope.getValue() + ",";
                    }

                }
                security = new OAuth2("oauth2", securityScheme.getDescription(), "client credentials flow", null, securityScheme.getFlows().getClientCredentials().getTokenUrl(), name,scope, provider);

            }
            if(securityScheme.getFlows().getPassword()!=null) {
                if(securityScheme.getFlows().getPassword().getScopes()!=null){
                    for(Map.Entry<String, String> entryScope : securityScheme.getFlows().getPassword().getScopes().entrySet()){
                        scope += entryScope.getKey() + ":" + entryScope.getValue() + ",";
                    }

                }
                security = new OAuth2("oauth2", securityScheme.getDescription(), "resource owner password", null, securityScheme.getFlows().getPassword().getTokenUrl(), name, scope, provider);

            }
        }




        return security;

    }

}
