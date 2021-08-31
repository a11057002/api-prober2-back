package ntou.soselab.swagger.transformation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import ntou.soselab.swagger.algo.WordNetExpansion;
import ntou.soselab.swagger.feature.EndpointLevel;
import ntou.soselab.swagger.feature.ServiceLevel;
import ntou.soselab.swagger.neo4j.domain.relationship.*;
import ntou.soselab.swagger.neo4j.domain.service.*;
import ntou.soselab.swagger.neo4j.graph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SwaggerToNeo4jTransformation {

    Logger log = LoggerFactory.getLogger(SwaggerToNeo4jTransformation.class);

    @Autowired
    Neo4jToDatabase neo4jToDatabase;

    @Autowired
    WordNetExpansion wordNetExpansion;

    @Autowired
    SwaggerToLDA swaggerToLDA;

    @Autowired
    ServiceLevel serviceLevel;

    @Autowired
    EndpointLevel endpointLevel;

    public void parseSwaggerDocument(String swaggerDocument) {


        //Swagger swagger = new SwaggerParser().parse(swaggerDocument);
        SwaggerParseResult swaggerResult = new OpenAPIV3Parser().readContents(swaggerDocument);

        Resource resource = new Resource();
        OpenAPI swagger = swaggerResult.getOpenAPI();

        ResourceGraph resourceGraph = getResourceInformation(swagger, resource);
        //Json.pretty(swagger) ---> convert swagger to original json format
        // set OAS document to database
        OAS oas = new OAS();
        Possess possess = new Possess();
        oas.setProberVersionOAS(swaggerDocument);
        OASGraph oasGraph = new OASGraph();
        oasGraph.setOas(oas);
        oasGraph.setPossess(possess);
        resourceGraph.setOasGraph(oasGraph);
        //Resource resource, OAS oas, Possess possess
        neo4jToDatabase.buildPossessBetweenResourceAndOAS(resource,oas,possess);

        log.info("-----path out");
        for (String p : swagger.getPaths().keySet()) {
            log.info("-----path in");
            // 設定路徑
            Path path = new Path();
            path.setPath(p);

            // record relationship type
            Endpoint endpoint = new Endpoint();
            PathGraph pathGraph = new PathGraph();

            pathGraph.setPath(path);
            pathGraph.setEndpoint(endpoint);




            if (swagger.getPaths().get(p).getDelete() != null) {
                log.info("--- operation:DELETE on {}", p);
                OperationGraph operationGraph = getOperationInformation(swagger, swagger.getPaths().get(p).getDelete(), "delete", p);

                findAllTheParametersFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getDelete()); // set

                findAllTheStatusCodeFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getDelete()); // set Response

                //swagger.getSecurity();
                pathGraph.setOperationGraph(operationGraph);
            }
            if (swagger.getPaths().get(p).getGet() != null) {
                log.info("--- operation:GET on {}", p);
                OperationGraph operationGraph = getOperationInformation(swagger, swagger.getPaths().get(p).getGet(), "get", p);

                findAllTheParametersFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getGet()); // set

                findAllTheStatusCodeFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getGet()); // set Response
                pathGraph.setOperationGraph(operationGraph);
            }
            if (swagger.getPaths().get(p).getPatch() != null) {
                log.info("--- operation:PATCH on {}", p);
                OperationGraph operationGraph = getOperationInformation(swagger, swagger.getPaths().get(p).getPatch(), "patch", p);

                findAllTheParametersFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getPatch()); // set
                findAllTheStatusCodeFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getPatch()); // set Response
                pathGraph.setOperationGraph(operationGraph);
            }
            if (swagger.getPaths().get(p).getPost() != null) {
                log.info("--- operation:POST on {}", p);
                OperationGraph operationGraph = getOperationInformation(swagger, swagger.getPaths().get(p).getPost(), "post", p);
                findAllTheParametersFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getPost()); // set
                findAllTheStatusCodeFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getPost()); // set Response
                pathGraph.setOperationGraph(operationGraph);
            }
            if (swagger.getPaths().get(p).getPut() != null) {
                log.info("--- operation:PUT on {}", p);
                OperationGraph operationGraph = getOperationInformation(swagger, swagger.getPaths().get(p).getPut(), "put", p);

                findAllTheParametersFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getPut()); // set
                findAllTheStatusCodeFromOperation(operationGraph, swagger.getComponents().getSchemas(),
                        swagger.getPaths().get(p).getPut()); // set Response
                pathGraph.setOperationGraph(operationGraph);
            }
            resourceGraph.setPathGraph(pathGraph);
        }
        //System.out.println("Swagger:"+swagger);
        neo4jToDatabase.buildRelationshipStartWithResource(resourceGraph);


    }




    public SecurityGraph getSecurityInformation(String name,SecurityScheme securityScheme){
        // 獲得全域的認證機制名稱，內容分析 swagger parser 無支援
        // List<SecurityRequirement> requirements = swagger.getSecurity();



        Security security = new Security();

        Annotate annotate = new Annotate();
        SecurityGraph securitySchemeGraph = new SecurityGraph();
        String scope = "";



        //need to create the security scheme graph
        //ResourceGraph resourceGraph = getResourceInformation(swagger, resource);
        if(securityScheme.getType().toString()=="apiKey"){


            security = new ApiKey("apiKey", securityScheme.getDescription(), securityScheme.getIn().toString(), securityScheme.getName(),name,"Original");

        }

        if(securityScheme.getType().toString()=="http"){
            if(securityScheme.getScheme()=="http")
                security = new Http("http", securityScheme.getDescription(), securityScheme.getScheme(),"Original");

            if(securityScheme.getScheme()=="bearer")
                security = new Jwt("http",securityScheme.getDescription(),securityScheme.getScheme(),securityScheme.getBearerFormat(),"Original");


        }
       // securityScheme.getFlows().getAuthorizationCode().getScopes();
        if(securityScheme.getType().toString()=="oauth2"){

            if(securityScheme.getFlows().getImplicit()!=null) {
                if(securityScheme.getFlows().getImplicit().getScopes()!=null){
                    for(Map.Entry<String, String> entryScope : securityScheme.getFlows().getImplicit().getScopes().entrySet()){
                        scope += entryScope.getKey() + ":" + entryScope.getValue() + ",";
                    }

                }
                security = new OAuth2("oauth2", securityScheme.getDescription(), "implicit flow", securityScheme.getFlows().getImplicit().getAuthorizationUrl(), null, name,scope,"Original");

            }
            if(securityScheme.getFlows().getAuthorizationCode()!=null) {
                if(securityScheme.getFlows().getAuthorizationCode().getScopes()!=null){
                    for(Map.Entry<String, String> entryScope : securityScheme.getFlows().getAuthorizationCode().getScopes().entrySet()){
                        scope += entryScope.getKey() + ":" + entryScope.getValue() + ",";
                    }

                }
                security = new OAuth2("oauth2", securityScheme.getDescription(), "authorization code flow", securityScheme.getFlows().getAuthorizationCode().getAuthorizationUrl(), securityScheme.getFlows().getAuthorizationCode().getTokenUrl(), name,scope,"Original");

            }
            if(securityScheme.getFlows().getClientCredentials()!=null) {
                if(securityScheme.getFlows().getClientCredentials().getScopes()!=null){
                    for(Map.Entry<String, String> entryScope : securityScheme.getFlows().getClientCredentials().getScopes().entrySet()){
                        scope += entryScope.getKey() + ":" + entryScope.getValue() + ",";
                    }

                }
                security = new OAuth2("oauth2", securityScheme.getDescription(), "client credentials flow", null, securityScheme.getFlows().getClientCredentials().getTokenUrl(), name,scope,"Original");

            }
            if(securityScheme.getFlows().getPassword()!=null) {
                if(securityScheme.getFlows().getPassword().getScopes()!=null){
                    for(Map.Entry<String, String> entryScope : securityScheme.getFlows().getPassword().getScopes().entrySet()){
                        scope += entryScope.getKey() + ":" + entryScope.getValue() + ",";
                    }
                }
                security = new OAuth2("oauth2", securityScheme.getDescription(), "resource owner password", null, securityScheme.getFlows().getPassword().getTokenUrl(), name, scope,"Original");

            }
        }

        securitySchemeGraph.setSecurity(security);
        securitySchemeGraph.setAnnotate(annotate);

                //log.info(":::::{}",security.getSecurityScheme().getScheme());
                //Resource resource, Security securityScheme, Annotate annotate
                //neo4jToDatabase.saveSecurityAnnotateWithResource(resource,securityScheme,securitySchemeGraph.getSecurityAnnotate());



        return securitySchemeGraph;

    }

    public ResourceGraph getResourceInformation(OpenAPI swagger, Resource resource) {

        // store swagger parse information
        ArrayList<String> swaggerInfo = new ArrayList<>();
        ResourceGraph resourceGraph = new ResourceGraph();
        // For saving key: stemming term --> value: original term
        HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();

        // For resource concept
        String title = null;
        String description = null;
        /*String host = null;*/
        String basePath = null;
        String version = null;
        String authentication = null;
        String proberVersionOAS = swagger.toString();


        // set new swagger string to resource
        /*resource.setProberVersionOAS(proberVersionOAS);
        log.info("versionnnn:{}",resource.getProberVersionOAS());*/

        // get basePath
        log.info("server : {}",swagger.getServers());
        basePath = swagger.getServers().get(0).getUrl();
        // if basePath have variable
        if(swagger.getServers().get(0).getVariables()!=null){
            for (Map.Entry<String, ServerVariable> entry : swagger.getServers().get(0).getVariables().entrySet()) {
                // According to the oas spec, default value must be specified
                String replaceKey = "\\{" + entry.getKey() + '}';
                basePath = basePath.replaceAll(replaceKey, entry.getValue().getDefault());
            }
        }


        resource.setBasePath(basePath);

        // get title
        title = swagger.getInfo().getTitle();
        log.info("Title :{}", title);
        resource.setTitle(title);

        // avoid provider name in title
        if(title != null) swaggerInfo.add(title);

        // get Logo
        //log.info("-----info:{}",swagger.getInfo().getExtensions());
        Map<String, Object> info = swagger.getInfo().getExtensions();




        if (info.get("x-logo") != null) {
            Object logo = info.get("x-logo");
            if (logo instanceof Map) {
                Map<String, Object> logoNode = (Map) logo;
                resource.setLogo(logoNode.get("url").toString());
            }
        }

        // get description
        description = swagger.getInfo().getDescription();
        resource.setDescription(description);
        if(description != null) swaggerInfo.add(description);

        // get provider
        if (info.get("x-providerName") != null) {
            Object provider = info.get("x-providerName");
            if (provider instanceof String) {
                resource.setProvider((String) provider);
            }
        }

        // get swagger url
        if (info.get("x-origin") != null) {
            Object swaggerUrl = info.get("x-origin");
            if(swaggerUrl instanceof ArrayList){
                ArrayList list = (ArrayList)swaggerUrl;
                if (list.size() > 0 && list.get(0) instanceof Map) {
                    Map<String, Object> swaggerUrlNode = (Map)list.get(0);
                    log.info(swaggerUrlNode.get("url").toString());
                    resource.setSwaggerUrl(swaggerUrlNode.get("url").toString());
                }
            }
        }

        // set id, the id is key=nodeId+title
        resource.setId(resource.toString());

        // set version
        resource.setVersion(swagger.getInfo().getVersion());


        //resourceGraph.setSecuritySchemeGraph()getSecurityInformation(OpenAPI swagger, Resource resource)
        
        if(swagger.getComponents() != null && swagger.getComponents().getSecuritySchemes() != null) {
            System.out.println("SECURITY");
            for (Map.Entry<String,SecurityScheme> entry : swagger.getComponents().getSecuritySchemes().entrySet()) {

                SecurityScheme ss = entry.getValue();

                if(ss.getType().toString().equals("oauth2")){
                    if(ss.getFlows().getImplicit()!=null) authentication = "Implicit";
                    if(ss.getFlows().getAuthorizationCode()!=null) authentication = "AuthorizationCode";
                    if(ss.getFlows().getClientCredentials()!=null) authentication = "ClientCredentials";
                    if(ss.getFlows().getPassword()!=null) authentication = "Password";
                }
                if(ss.getType().toString().equals("apiKey"))
                    authentication = "apiKey";
                if(ss.getType().toString().equals("http")){
                    authentication = ss.getScheme().equals("basic")?"http":"jwt";
                    System.out.println("scheme:"+authentication);
                }
                //log.info("entry key:{} \n entry value : {} ", entry.getKey(),entry.getValue());
                resourceGraph.setSecuritySchemeGraph(getSecurityInformation(entry.getKey(),ss));
                resource.setAuthentication(authentication);

            }
        }

        

        // 增加更多 swagger 額外的標註詞彙
        // get x-tags
        if (info.get("x-tags") != null) {
            Object infoTags = info.get("x-tags");
            if (infoTags instanceof ArrayList) {
                ArrayList<String> infoTagsNode = (ArrayList) infoTags;
                // assertEquals(infoTagsNode.get(0), "Azure");
                for (String tag : infoTagsNode) {
                    if(tag != null) swaggerInfo.add(tag);
                }
            }
        }

        // 檢查 Service Level Feature
        resource.setFeatures(serviceLevel.parseSwaggerService(swagger));
        //resource.setConsume(info.get("x-loo").toString());
        try {
            if(swaggerInfo.size() != 0) {
                // parse LDA
                ArrayList<String> LDAWord = swaggerToLDA.swaggerParseLDA(swaggerInfo.toArray(new String[0]), stemmingAndTermsTable);
                // parse WordNet
                ArrayList<String> WordNetWord = wordNetExpansion.getWordNetExpansion(LDAWord, stemmingAndTermsTable);

                resource.setOriginalWord(LDAWord);
                resource.setWordnetWord(WordNetWord);
            }
        } catch (IOException e) {
            log.info("Parse word topic error :{}", e.toString());
        }
        resourceGraph.setResource(resource);
        return resourceGraph;
    }

    private OperationGraph getOperationInformation(OpenAPI swagger, io.swagger.v3.oas.models.Operation swaggerOperation, String Swaggeraction, String path) {

        // store swagger parse information
        ArrayList<String> swaggerInfo = new ArrayList<>();
        // For saving key: stemming term --> value: original term
        HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();

        String description = null;

        Operation operation = new Operation();
        description = swaggerOperation.getDescription();
        operation.setDescription(description);
        if(description != null) swaggerInfo.add(description);
        log.info("operation description :{}", swaggerOperation.getDescription());

        operation.setOperationAction(Swaggeraction);
        log.info("operation action :{}", Swaggeraction);

        // operation feature
        operation.setFeatures(endpointLevel.parseSwaggerEndpoint(swagger, swaggerOperation, path));

        try {
            if(swaggerInfo.size() != 0) {
                // parse LDA
                ArrayList<String> LDAWord = swaggerToLDA.swaggerParseLDA(swaggerInfo.toArray(new String[0]), stemmingAndTermsTable);
                // parse WordNet
                ArrayList<String> WordNetWord = wordNetExpansion.getWordNetExpansion(LDAWord, stemmingAndTermsTable);

                operation.setOriginalWord(LDAWord);
                operation.setWordnetWord(WordNetWord);
            }
        } catch (IOException e) {
            log.info("Parse word topic error :{}", e.toString());
        }

        // record relationship type
        Action action = new Action();
        OperationGraph operationGraph = new OperationGraph();

        operationGraph.setAction(action);
        operationGraph.setOperation(operation);

        return operationGraph;
    }

    // 將該 path action input 參數全部分析
    private void findAllTheParametersFromOperation(OperationGraph operationGraph, Map<String, Schema> schemas, io.swagger.v3.oas.models.Operation operation) {

        List<io.swagger.v3.oas.models.parameters.Parameter> lp = operation.getParameters();
        //RequestBody rp = operation.getRequestBody();
//swagger.getPaths().get(p).getPost()
        // check 不同 parameters in --> https://swagger.io/docs/specification/2-0/describing-parameters/
        if(lp!=null) {

            log.info("---- detect the non-body parameter: ");
            for (io.swagger.v3.oas.models.parameters.Parameter p : lp) {


                    log.info("---- detect the non-body parameter: {}", p.getName());

                    ParameterGraph pb = getParameterBeanEntity(p);
                    if (pb != null) {
                        operationGraph.setParameterGraph(pb); // 填入參數
                    }




                //}
            }
//            if(p instanceof PathParameter) {
//                log.info("---- detect the path parameter in :{}", p.getName());
//                log.info("---- detect the path parameter in :{}", p.getIn());
//                log.info("---- detect the path parameter in :{}", p.getDescription());
//                log.info("---- detect the path parameter in :{}", p.getRequired());
//                log.info("---- detect the path parameter in :{}", ((PathParameter) p).getType());
//                log.info("---- detect the path parameter in :{}", ((PathParameter) p).getFormat());
//            }else if(p instanceof QueryParameter) {
//                log.info("---- detect the query parameter in :{}", p.getName());
//            }else if(p instanceof HeaderParameter) {
//                log.info("---- detect the header parameter in :{}", p.getName());
//            }else if(p instanceof BodyParameter) {
//                log.info("---- detect the body parameter in :{}", p.getName());
//                log.info("---- detect the path parameter in :{}", p.getIn());
//                log.info("---- detect the path parameter in :{}", p.getDescription());
//                log.info("---- detect the path parameter in :{}", p.getRequired());
//            }else if(p instanceof FormParameter) {
//                log.info("---- detect the formData parameter in :{}", p.getName());
//            }else if(p instanceof RefParameter) {
//                log.info("---- detect the reference parameter in :{}", p.getName());
//            }
        }
        if(operation.getRequestBody()!=null){
            RequestBody rp = operation.getRequestBody();
            log.info("ssssbody: {}",rp.get$ref());

            parseBodyParameter(rp, schemas, operationGraph);
        }
    }

    private void findAllTheStatusCodeFromOperation(OperationGraph operationGraph, Map<String, Schema> componentsSchema,
                                                  io.swagger.v3.oas.models.Operation operation) {

        ApiResponses responses = operation.getResponses();

        for(String key : responses.keySet()) {
            if (responses.get(key) != null && responses.get(key).getDescription()!=null ) {
                log.info("status code :{}", key);
                log.info("Response Description :{}", responses.get(key).getDescription());

                // 根據不同狀態碼，建立節點
                StatusCode statusCode = new StatusCode();
                statusCode.setStatusCode(key);
                statusCode.setDescription(responses.get(key).getDescription());
                Output output = new Output();
                StatusCodeGraph statusCodeGraph = new StatusCodeGraph(statusCode);
                statusCodeGraph.setOutput(output);

                //System.out.println("setStatusCode:"+key+"  setDescription:"+responses.get(key).getDescription());

                operationGraph.setStatusCodeGraphs(statusCodeGraph);

                /*if(!responses.get(key).getContent().values().isEmpty()) {
                    System.out.println(responses.get(key).getContent().values());*/

                if(responses.get(key).getContent()!= null ){
                   //MediaType content = responses.get(key).getContent();
                    //log.info("content : {}",responses.get(key).getContent().);
                    //if(content.get(content).equals("application/json") ){
                    if (responses.get(key).getContent().containsKey("application/json")) {

                        Schema responseSchema = responses.get(key).getContent().get("application/json").getSchema();


                        //System.out.println(properties);
                        /*
                         * if(property instanceof RefProperty){ //不取只顯示成功的字樣
                         * log.info("--- detect response: 200");
                         * parseRefProperty((RefProperty)property, definitions, "",
                         * operationBean, "response", new ArrayList<String>()); }
                         */
//                    if (property instanceof RefProperty) { // 不取只顯示成功的字樣
//                        parseStatusCodeRefProperty((RefProperty) property, definitions, "", statusCodeGraph);
//
//                    } else {
//                        parseResponseProperty(null, property, definitions, statusCodeGraph);
//                    }
                        parseResponseProperty(null, responseSchema, componentsSchema, statusCodeGraph, key);
                    }
                }
                //}
            }
        }
    }

    private ParameterGraph getParameterBeanEntity(io.swagger.v3.oas.models.parameters.Parameter swaggerParameter) {
        // store swagger parse information
        ArrayList<String> swaggerInfo = new ArrayList<>();
        // For saving key: stemming term --> value: original term
        HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();

        String name = null;
        String in = null;
        String description = null;
        String media_type = null;
        String format = null;
        boolean required = false;
        String examlple = null;
        log.info("------ create ParameterGraph by non body: {}", swaggerParameter.getName());


        ntou.soselab.swagger.neo4j.domain.service.Parameter parameter = new ntou.soselab.swagger.neo4j.domain.service.Parameter();
        // set parameter Name
        name = swaggerParameter.getName();
        parameter.setName(name);
        if(name != null) swaggerInfo.add(name);
        log.info("Parameter Name :{}", name);

        // set parameter in
        in = swaggerParameter.getIn();
        parameter.setIn(in);
        log.info("Parameter In :{}", in);

        // set parameter description
        description = swaggerParameter.getDescription();
        parameter.setDescription(description);
        if(description != null) swaggerInfo.add(description);
        log.info("Parameter Description :{}", description);

        // set parameter required
        if(swaggerParameter.getRequired()!= null){
            required = swaggerParameter.getRequired();
            parameter.setRequired(required);

        }

        log.info("Parameter Required :{}", required);
        // set parameter example

        if(swaggerParameter.getExample() != null) {
            examlple = swaggerParameter.getExample().toString();
            parameter.setExample(examlple);
            swaggerInfo.add(examlple);
        }



        // media_type and format
        if (swaggerParameter instanceof QueryParameter) {
            media_type = ((QueryParameter) swaggerParameter).getSchema().getType();
            format = ((QueryParameter) swaggerParameter).getSchema().getFormat();
            parameter.setMedia_type(media_type);
            parameter.setFormat(format);
            log.info("Parameter Media_Type :{}", media_type);
            log.info("Parameter Format :{}", format);
        } else if (swaggerParameter instanceof PathParameter) {
            media_type = ((PathParameter) swaggerParameter).getSchema().getType();
            format = ((PathParameter) swaggerParameter).getSchema().getFormat();
            parameter.setMedia_type(media_type);
            parameter.setFormat(format);
            log.info("Parameter Media_Type :{}", media_type);
            log.info("Parameter Format :{}", format);
        } /*else if (swaggerParameter instanceof FormParameter) {
            media_type = ((FormParameter) swaggerParameter).getType();
            format = ((FormParameter) swaggerParameter).getFormat();
            parameter.setMedia_type(media_type);
            parameter.setFormat(format);
            log.info("Parameter Media_Type :{}", media_type);
            log.info("Parameter Format :{}", format);
        }*/ else if (swaggerParameter instanceof HeaderParameter) {
            media_type = ((HeaderParameter) swaggerParameter).getSchema().getType();
            format = ((HeaderParameter) swaggerParameter).getSchema().getFormat();
            parameter.setMedia_type(media_type);
            parameter.setFormat(format);
            log.info("Parameter Media_Type :{}", media_type);
            log.info("Parameter Format :{}", format);
        }

        try {
            if(swaggerInfo.size() != 0) {
                // parse LDA
                ArrayList<String> LDAWord = swaggerToLDA.swaggerParseLDA(swaggerInfo.toArray(new String[0]), stemmingAndTermsTable);
                // parse WordNet
                ArrayList<String> WordNetWord = wordNetExpansion.getWordNetExpansion(LDAWord, stemmingAndTermsTable);

                parameter.setOriginalWord(LDAWord);
                parameter.setWordnetWord(WordNetWord);
            }
        } catch (IOException e) {
            log.info("Parse word topic error :{}", e.toString());
        }

        // Build relationship
        Input input = new Input();
        ParameterGraph parameterGraph = new ParameterGraph(parameter);

        parameterGraph.setInput(input);

        return parameterGraph;
    }

    private ParameterGraph getParameterBeanEntity(String key, Schema property, String in) {

        // store swagger parse information
        ArrayList<String> swaggerInfo = new ArrayList<>();
        // For saving key: stemming term --> value: original term
        HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();

        log.info("------ create ParameterBean by Property entity: {}", key);
        String name = null;
        String description = null;
        String media_type = null;


        name = key;
        description = property.getDescription();
        media_type = property.getType();





        log.info("Parameter Name :{}", name);
        log.info("Parameter In :{}", in);
        log.info("Parameter Description :{}", property.getDescription());
        log.info("Parameter Media_Type :{}", property.getType());

        ntou.soselab.swagger.neo4j.domain.service.Parameter parameter = new ntou.soselab.swagger.neo4j.domain.service.Parameter();
        parameter.setName(name);
        parameter.setIn(in);
        parameter.setDescription(description);
        parameter.setMedia_type(media_type);


        if(name != null) swaggerInfo.add(name);
        if(description != null) swaggerInfo.add(description);

        try {
            if(swaggerInfo.size() != 0) {
                // parse LDA
                ArrayList<String> LDAWord = swaggerToLDA.swaggerParseLDA(swaggerInfo.toArray(new String[0]), stemmingAndTermsTable);
                // parse WordNet
                ArrayList<String> WordNetWord = wordNetExpansion.getWordNetExpansion(LDAWord, stemmingAndTermsTable);

                parameter.setOriginalWord(LDAWord);
                parameter.setWordnetWord(WordNetWord);
            }
        } catch (IOException e) {
            log.info("Parse word topic error :{}", e.toString());
        }

        // Build relationship
        Input input = new Input();
        ParameterGraph parameterGraph = new ParameterGraph(parameter);

        parameterGraph.setInput(input);

        return parameterGraph;
    }

    private void parseBodyParameter(RequestBody p, Map<String, Schema> definitions, OperationGraph operationGraph) {


        log.info("-----p: {}",p);
        if(p.getContent() != null){
            if (p.getContent().containsKey("application/json")) {
                String ref  =  p.getContent().get("application/json").getSchema().get$ref();
                if(ref != null){
                    parseRefModel(ref, definitions, "body-object", operationGraph, "parameter");
                }
                else{
                    Map<String, Schema> sp = p.getContent().get("application/json").getSchema().getProperties();
                    if (sp != null) {
                        for (String s : sp.keySet()) {
                            if (sp.get(s).get$ref() != null) {
                                parseRefProperty(sp.get(s), definitions, "body-object", operationGraph, "parameter");
                            } else {
                                ParameterGraph pb = getParameterBeanEntity(s, sp.get(s), "body-object");
                                if (pb != null) {
                                    operationGraph.setParameterGraph(pb);
                                }
                            }
                        }
                    }
                }
            }
            else if(p.getContent().containsKey("application/x-www-form-urlencoded")){
                String ref  =  p.getContent().get("application/x-www-form-urlencoded").getSchema().get$ref();
                if(ref!=null) parseRefModel(ref, definitions, "body-form", operationGraph, "parameter");
                else{
                    Map<String, Schema> sp = p.getContent().get("application/x-www-form-urlencoded").getSchema().getProperties();
                    if (sp != null) {
                        for (String s : sp.keySet()){
                            if (sp.get(s).get$ref() != null) {
                                parseRefProperty(sp.get(s), definitions, "body-form", operationGraph, "parameter");
                            } else {

                                ParameterGraph pb = getParameterBeanEntity(s, sp.get(s), "body-form");
                                if (pb != null) {
                                    operationGraph.setParameterGraph(pb);
                                }
                            }
                        }
                    }

                }
            }
        }
        else {
            log.info("----- Error finding body parameter is not RefModel");
        }

    }

    private void parseRefModel(String refModel, Map<String, Schema> definitions, String in, OperationGraph operationGraph, String paramOrResponse) {


        log.info("----- go to RefModel {}", refModel);
        Schema petModel = definitions.get(refModel);
        if (petModel instanceof ComposedSchema) {
            log.info("---- detect allOf on definition:{} at {}", refModel, paramOrResponse);
            parseComposedModel((ComposedSchema) petModel, definitions, in, operationGraph, paramOrResponse);
        }
        if (petModel != null) {
            Map<String, Schema> sp = petModel.getProperties();
            if (sp != null) {
                for (String s : sp.keySet()) {
                    log.info("---- create ParameterBean on definition:{} at {} -- key: {}", refModel,
                            paramOrResponse, s);
                    if (sp.get(s) instanceof Schema) {

                        parseRefProperty(sp.get(s), definitions, in, operationGraph, paramOrResponse);
                    } else {
                        if (paramOrResponse.equals("parameter")) {
                            ParameterGraph pb = getParameterBeanEntity(s, sp.get(s), in);
                            if (pb != null) {
                                operationGraph.setParameterGraph(pb);
                            }
                        }
                    }
                }
            }
        }
    }

    private void parseComposedModel(ComposedSchema mod, Map<String, Schema> definitions, String in, OperationGraph operationGraph, String paramOrResponse) {

        for (Schema model : mod.getAllOf()) {
            if (model instanceof Schema) {
                parseRefModel( model.get$ref(), definitions, in, operationGraph, paramOrResponse);
            } else {
                if (model.getProperties() != null) {
                    Map<String, Schema> sp = model.getProperties();
                    for (String s : sp.keySet()) {
                        log.info("---- create ParameterBean on allOf at {} -- key: {}", paramOrResponse, s);
                        if (paramOrResponse.equals("parameter")) {
                            ParameterGraph pb = getParameterBeanEntity(s, sp.get(s), in);
                            if (pb != null) {
                                operationGraph.setParameterGraph(pb);
                            }
                        }
                        if (sp.get(s) instanceof Schema) {
                            log.info("parseRefProperty2");
                            parseRefProperty((Schema) sp.get(s), definitions, in, operationGraph, paramOrResponse);
                        }
                    }

                }
            }
        }
    }

    private void parseRefProperty(Schema ref, Map<String, Schema> definitions, String in, OperationGraph operationGraph, String flag) {

        log.info("---- go to RefProperty {}", ref.get$ref());
        Schema petModel = definitions.get(ref.get$ref().split("#/components/schemas/")[1]);

        if (petModel != null) {
            Map<String, Schema> sp = petModel.getProperties();

            if (petModel instanceof ComposedSchema) {
                log.info("---- detect allOf on definition:{} at {}", ref.get$ref(), flag);
                parseComposedModel((ComposedSchema) petModel, definitions, in, operationGraph, flag);
            }

            if (sp != null) {
                for (String s : sp.keySet()) {
                    log.info("---- create ParameterBean on definition:{} at {} -- key: {}", ref.get$ref(), flag,s);
                    System.out.println(flag);
                    if (flag.equals("parameter")) {
                        ParameterGraph pb = getParameterBeanEntity(s, sp.get(s), in);
                        if (pb != null) {
                            operationGraph.setParameterGraph(pb);
                        }
                    }
                }
            }
        }
    }

    private void parseStatusCodeRefModel(Schema ref, Map<String, Schema> componentsSchema, String in, StatusCodeGraph statusCodeGraph, String statusCode) {
        log.info("----- go to RefModel {}", ref.get$ref());

        Schema petModel = componentsSchema.get(ref.get$ref().split("#/components/schemas/")[1]);


        if (petModel instanceof ComposedSchema) {
            log.info("---- detect allOf on definition:{} at {}", ref.get$ref(), "status code");
            parseStatusCodeComposedModel((ComposedSchema) petModel, componentsSchema, in, statusCodeGraph, statusCode);
        }
        if (petModel != null) {
            Map<String, Schema> sp = petModel.getProperties();
            if (sp != null) {
                for (String s : sp.keySet()) {
                    log.info("---- create ParameterBean on definition:{} at {} -- key: {}", ref.get$ref(),
                            "status code", s);

                    if (sp.get(s) instanceof Schema) {
                        parseStatusCodeRefProperty(sp.get(s), componentsSchema, in, statusCodeGraph, statusCode);
                    } else {
                        statusCodeGraph.setResponseGraph(getResponseBeanEntity(s, sp.get(s), statusCode));
                    }
                }
            }
        }
    }

    private void parseStatusCodeComposedModel(ComposedSchema sch, Map<String, Schema> componentsSchema, String in, StatusCodeGraph statusCodeGraph, String statusCode) {

        for (Schema model : sch.getAllOf()) {
            if (model.get$ref() != null) {
                parseStatusCodeRefModel(model, componentsSchema, in, statusCodeGraph, statusCode);
            } else {
                if (model.getProperties() != null) {
                    Map<String, Schema> sp = model.getProperties();
                    for (String s : sp.keySet()) {
                        log.info("---- create ParameterBean on allOf at {} -- key: {}", "status code", s);
                        statusCodeGraph.setResponseGraph(getResponseBeanEntity(s, sp.get(s), statusCode));

                        if (sp.get(s) instanceof Schema) {
                            parseStatusCodeRefProperty( sp.get(s), componentsSchema, in, statusCodeGraph, statusCode);
                        }
                    }

                }
            }
        }
    }

    private void parseStatusCodeRefProperty(Schema ref, Map<String, Schema> componentsSchema, String in, StatusCodeGraph statusCodeGraph, String statusCode) {

        log.info("---- go to RefProperty {}", ref.get$ref());
        Schema petModel = null;
        if(ref.get$ref()!=null) {
            String r = ref.get$ref().split("#/components/schemas/")[1];
            petModel = componentsSchema.get(r);

        }

        //System.out.println(ref.get$ref());
        //log.info("")
        if (petModel instanceof ComposedSchema) {
            log.info("---- detect allOf on definition:{} at {}", ref.get$ref(), "status code");
            parseStatusCodeComposedModel((ComposedSchema) petModel, componentsSchema, in, statusCodeGraph, statusCode);
        }
        if (petModel != null) {
            Map<String, Schema> sp = petModel.getProperties();

            if (petModel instanceof ComposedSchema) {
                log.info("---- detect allOf on definition:{} at {}", ref.get$ref(), "status code");
                parseStatusCodeComposedModel((ComposedSchema) petModel, componentsSchema, in, statusCodeGraph, statusCode);
            }

            if (sp != null) {
                for (String s : sp.keySet()) {
                    log.info("---- create ParameterBean on definition:{} at {} -- key: {}", ref.get$ref(), "status code", s);
                    log.info("-------s:{}",s);
                    statusCodeGraph.setResponseGraph(getResponseBeanEntity(s, sp.get(s), statusCode));

                }
            }
        }
    }

    private void parseResponseProperty(String key, Schema responseSchema, Map<String, Schema> componentsSchema, StatusCodeGraph statusCodeGraph, String statusCode) {

        //System.out.println(responseSchema.get$ref());
        /*if(responseSchema.get$ref()!=null){
            //System.out.println(responseSchema.get$ref());
            String r = responseSchema.get$ref().split("#/components/schemas/")[1];/*/

            //"#components/schemas/"

            if (responseSchema instanceof ObjectSchema) {
                log.info("---- parse response ObjectProperty: {}", key);
                ObjectSchema os = (ObjectSchema) responseSchema;
                Map<String, Schema> paramTable = os.getProperties();
                if (paramTable != null) {
                    for (Map.Entry<String, Schema> entry : paramTable.entrySet()) {
                        parseResponseProperty(entry.getKey(), entry.getValue(), componentsSchema, statusCodeGraph, statusCode);
                    }
                }

            } else if (responseSchema instanceof ArraySchema) {
                log.info("---- parse response ArrayProperty: {}", key);
                ArraySchema array = (ArraySchema) responseSchema;
                Schema pItems = array.getItems();
                if (pItems instanceof Schema) {
                    parseStatusCodeRefProperty(pItems, componentsSchema, "", statusCodeGraph, statusCode);
                }
            } else if (responseSchema.get$ref() != null) {
                log.info("---- parse response RefProperty: {}", key);
                parseStatusCodeRefProperty(responseSchema, componentsSchema, "", statusCodeGraph, statusCode);
            } else {
                log.info("----- parse response final Property: {}, and save to operationBean!", key);
                statusCodeGraph.setResponseGraph(getResponseBeanEntity(key, responseSchema, statusCode));
            }




    }

    private ResponseGraph getResponseBeanEntity(String key, Schema swaggerResponse, String statusCode) {

        // store swagger parse information
        ArrayList<String> swaggerInfo = new ArrayList<>();
        // For saving key: stemming term --> value: original term
        HashMap<String, String> stemmingAndTermsTable = new HashMap<String, String>();

        String name = null;
        String mediaType = null;
        String description = null;
        String format = null;
        boolean required = false;

        Response response = new Response();
        log.info("Response Name :{}", key);
        log.info("Response Media_Type :{}", swaggerResponse.getType());
        log.info("Response Description :{}", swaggerResponse.getDescription());
        log.info("Response Format :{}", swaggerResponse.getFormat());
        log.info("Response Required :{}", swaggerResponse.getRequired());
        name = key;
        mediaType = swaggerResponse.getType();
        description = swaggerResponse.getDescription();
        format = swaggerResponse.getFormat();
        //required = swaggerResponse.getRequired();
        response.setName(name);
        response.setMedia_type(mediaType);
        response.setDescription(description);
        response.setFormat(format);
        response.setRequired(required);

        if(name != null) swaggerInfo.add(name);
        if(description != null) swaggerInfo.add(description);

        try {
            if(swaggerInfo.size() != 0 && statusCode.equals("200")) {
                // parse LDA
                ArrayList<String> LDAWord = swaggerToLDA.swaggerParseLDA(swaggerInfo.toArray(new String[0]), stemmingAndTermsTable);
                // parse WordNet
                ArrayList<String> WordNetWord = wordNetExpansion.getWordNetExpansion(LDAWord, stemmingAndTermsTable);

                response.setOriginalWord(LDAWord);
                response.setWordnetWord(WordNetWord);
            }
        } catch (IOException e) {
            log.info("Parse word topic error :{}", e.toString());
        }

        ResponseGraph responseGraph = new ResponseGraph(response);
        // Build relationship
        Have have = new Have();
        responseGraph.setHave(have);

        return responseGraph;
    }
}
