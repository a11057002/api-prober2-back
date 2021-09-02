package ntou.soselab.swagger.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import ntou.soselab.swagger.web.ProberPathConfig;
import ntou.soselab.swagger.neo4j.domain.service.Operation;
import ntou.soselab.swagger.neo4j.domain.service.Path;
import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.domain.service.Security;
import ntou.soselab.swagger.neo4j.repositories.service.OASRepository;
import ntou.soselab.swagger.neo4j.repositories.service.OperationRepository;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import ntou.soselab.swagger.neo4j.repositories.service.SecurityRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Service
public class SecurityManager {

    @Autowired
    ResourceRepository resourceRepository;
    @Autowired
    OperationRepository operationRepository;
    @Autowired
    UpdateAnnotation updateAnnotation;
    @Autowired
    SecurityRepository securityRepository;
    @Autowired
    OASRepository oasRepository;
    @Autowired
    ProberPathConfig proberPathConfig;




    public String addOASSecurityScheme(String securityString){

        JsonObject jsonObject = new JsonParser().parse(securityString).getAsJsonObject();
        Long resourceId = jsonObject.get("oasId").getAsLong();
        Security security = new Security();
        Resource resource = resourceRepository.findResourceById(resourceId);
        Long operationId = jsonObject.get("operationId").getAsLong();
        /*Annotate annotate = new Annotate();
        SecurityGraph securitySchemeGraph = new SecurityGraph();
        ResourceGraph resourceGraph = new ResourceGraph(resource);*/

        securityAnnotate(security,jsonObject,resourceId,operationId);
        /*securitySchemeGraph.setSecurity(security);
        securitySchemeGraph.setAnnotate(annotate);
        resourceGraph.setSecuritySchemeGraph(securitySchemeGraph);*/

        // neo4jToDatabase.saveSecurityAnnotateWithResource(resource,security,annotate);

        // need to modify SecuritySchemeInfo Object and to securityInfo set
        return "1";

    }

    public String securityAnnotate(Security security, JsonObject jsonObject, Long resourceId, Long operationId){
        //ArrayList<String> scope = new ArrayList<>();

        //String oas = oasRepository.findOASByResourceId(resourceId).getProberVersionOAS();
        String swaggerDocument = jsonObject.get("oas").getAsString();
        String type = jsonObject.get("type").getAsString();

        // get original swagger or new swagger
        /*if(swaggerDocument.isEmpty())
            swaggerDocument = oasRepository.findOASByResourceId(resourceId).getProberVersionOAS();*/
        swaggerDocument = oasRepository.findOASByResourceId(resourceId).getProberVersionOAS();
        JsonObject oasJsonObject = new JsonParser().parse(swaggerDocument).getAsJsonObject();

        Path path = operationRepository.findPathByOperation(operationId);
        Operation operation = operationRepository.findOperationByOperationId(operationId);
        String p = path.getPath();
        String action = operation.getOperationAction();
        //check the parameter
        if(!jsonObject.get("paramIn").getAsString().isEmpty() && !jsonObject.get("paramName").getAsString().isEmpty()){
            updateAnnotation.getParameter(jsonObject);


            JsonObject paramJsonObject = oasJsonObject.getAsJsonObject("paths").getAsJsonObject(p).getAsJsonObject(action).getAsJsonObject("parameters");

            if(paramJsonObject != null){
                JsonArray paramJsonArray = paramJsonObject.getAsJsonArray();
                JsonObject paramElement = new JsonObject();
                paramElement.addProperty("in",jsonObject.get("paramIn").getAsString());
                paramElement.addProperty("name",jsonObject.get("paramName").getAsString());
                paramJsonArray.add(paramElement);

            }
            /*else{
                JsonArray jsonElementParam = new JsonArray();
                JsonObject paramElement = new JsonObject();
                paramElement.addProperty("in",jsonObject.get("paramIn").getAsString());
                paramElement.addProperty("name",jsonObject.get("paramName").getAsString());
                jsonElementParam.add(jsonElementParam);
                //System.out.println("ALL:"+oasJsonObject.get("paths").getAsJsonObject().get(p).getAsJsonObject().get(action).getAsJsonObject());
                oasJsonObject.get("paths").getAsJsonObject().get(p).getAsJsonObject().getAsJsonObject(action).add("parameters",jsonElementParam);
            }*/

        }


        // need to add condition to check securitySchemes exist or not
        JsonObject schemeJsonObject = oasJsonObject.getAsJsonObject("components").getAsJsonObject("securitySchemes");

        if(schemeJsonObject == null){
            if(oasJsonObject.getAsJsonObject("components") == null){
                JsonObject jsonElementComponents = new JsonObject();
                JsonObject jsonElementSecurity = new JsonObject();
                oasJsonObject.getAsJsonArray().getAsJsonObject().add("components",jsonElementComponents);
                oasJsonObject.get("components").getAsJsonObject().add("securitySchemes",jsonElementSecurity);
            }
            else{
                JsonObject jsonElementSecurity = new JsonObject();
                oasJsonObject.get("components").getAsJsonObject().add("securitySchemes",jsonElementSecurity);
            }
            schemeJsonObject = oasJsonObject.getAsJsonObject("components").getAsJsonObject("securitySchemes");
        }
        // check the type
        if(type.equals("apiKey")){
            JsonObject jsonElement = new JsonObject();
            jsonElement.addProperty("in",jsonObject.get("in").getAsString());
            jsonElement.addProperty("name",jsonObject.get("name").getAsString());
            jsonElement.addProperty("type",type);
            System.out.println(jsonElement);
            schemeJsonObject.add("apiKey",jsonElement);
            oasJsonObject.get("paths").getAsJsonObject().get(p).getAsJsonObject().get(action).getAsJsonObject().add("security",new JsonPrimitive("apiKey"));
            if(operation.getFeature() != null && !operation.getFeature().contains("Authentication"))
                operation.setFeature("Authentication");
            System.out.println(oasJsonObject);

            /*securityScheme.setType(SecurityScheme.Type.APIKEY);
            securityScheme.setIn(SecurityScheme.In.valueOf(jsonObject.get("in").getAsString().toUpperCase()));
            securityScheme.setName(jsonObject.get("name").getAsString());
            swagger.getComponents().getSecuritySchemes().put(type,securityScheme);*/
        } else if(type.equals("http")){
            JsonObject jsonElement = new JsonObject();
            jsonElement.addProperty("type",type);
            jsonElement.addProperty("scheme",jsonObject.get("scheme").getAsString());
            if(jsonObject.get("scheme").getAsString().equals("bearer"))
                jsonElement.addProperty("bearerFormat",jsonObject.get("bearerFormat").getAsString());

            schemeJsonObject.add("httpAuth",jsonElement);
            oasJsonObject.get("paths").getAsJsonObject().get(p).getAsJsonObject().get(action).getAsJsonObject().add("security",new JsonPrimitive("http"));
            if(operation.getFeature() != null && !operation.getFeature().contains("Authentication"))
                operation.setFeature("Authentication");

        } else if(type.equals("oauth2")){
            String flow = jsonObject.get("flow").getAsString();
            JsonObject jsonElement = new JsonObject();
            jsonElement.addProperty("type",type);
            JsonObject flowName = new JsonObject();
            JsonObject flowElements = new JsonObject();
            JsonObject scopeElement = new JsonObject();


            if(jsonObject.get("scopes").getAsString().isEmpty()){

                scopeElement.addProperty("","");

            }
            else{

                String scopes[] = jsonObject.get("scopes").getAsString().split(",");
                for(int i = 0; i < scopes.length; i++){
                    scopeElement.addProperty(scopes[i].split(":")[0],scopes[i].split(":")[1]);
                }

            }

            if(flow.equals("implicit")){

                flowElements.addProperty("authorizationUrl", jsonObject.get("authorizationUrl").getAsString());
                //flowElements.addProperty("scopes","{}");
                flowName.add("implicit", flowElements);
                if(operation.getFeature() != null && !operation.getFeature().contains("Authentication"))
                    operation.setFeature("Authentication");

            }
            if(flow.equals("authorizationCode")){

                flowElements.addProperty("authorizationUrl", jsonObject.get("authorizationUrl").getAsString());
                flowElements.addProperty("tokenUrl", jsonObject.get("tokenUrl").getAsString());
                //flowElements.addProperty("scopes",scopeElement.addProperty("",""));
                flowName.add("authorizationCode", flowElements);
                if(operation.getFeature() != null && !operation.getFeature().contains("Authentication"))
                    operation.setFeature("Authentication");

            }
            if(flow.equals("resourceOwnerPassword")){

                flowElements.addProperty("tokenUrl", jsonObject.get("tokenUrl").getAsString());
                //flowElements.addProperty("scopes",new JsonObject().getAsString());
                flowName.add("password", flowElements);
                if(operation.getFeature() != null && !operation.getFeature().contains("Authentication"))
                    operation.setFeature("Authentication");
            }
            if(flow.equals("clientCredentials")){

                flowElements.addProperty("tokenUrl", jsonObject.get("authorizationUrl").getAsString());
                //flowElements.addProperty("scopes","{}");
                flowName.add("clientCredentials", flowElements);
                if(operation.getFeature() != null && !operation.getFeature().contains("Authentication"))
                    operation.setFeature("Authentication");
            }

            flowElements.add("scopes",scopeElement);
            jsonElement.add("flows",flowName);
            schemeJsonObject.add(flow+"Auth",jsonElement);
            jsonElement.addProperty("x-provider", jsonObject.get("provider").getAsString());

            JsonObject securityJsonObject = new JsonObject();
            securityJsonObject.add(flow+"Auth", new JsonArray());
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(securityJsonObject);

            oasJsonObject.get("paths").getAsJsonObject().get(p).getAsJsonObject().get(action).getAsJsonObject().add("security",jsonArray);

        }
        System.out.println(oasJsonObject);
        operationRepository.save(operation);
        updateAnnotation.updateResourceAuthentication(oasJsonObject.toString(),resourceId, operationId, jsonObject.get("provider").getAsString());
        return "Save";
    }
    /*public String addSecurityValue(String data){
        JsonObject jsonObject = new JsonParser().parse(data).getAsJsonObject();
        JsonObject securityObject = new JsonObject();
        Long resourceId = jsonObject.get("oasId").getAsLong();
        Resource resource = resourceRepository.findResourceById(resourceId);


        System.out.println(jsonObject.get("apiKey").getAsString());
        //String clientId, String clientSecret, String apiKey, String token, String bearer, String basic
        SecurityData securityData = new SecurityData(jsonObject.get("clientId").getAsString(),jsonObject.get("clientSecret").getAsString(),jsonObject.get("apiKey").getAsString(),jsonObject.get("token").getAsString(),jsonObject.get("basic").getAsString(),jsonObject.get("bearer").getAsString());

        securityData.setNote(jsonObject.get("note").getAsString());

        Own own = new Own();
        SecurityDataGraph securityDataGraph = new SecurityDataGraph();
        ResourceGraph resourceGraph = new ResourceGraph(resource);

        securityDataGraph.setSecurityData(securityData);
        securityDataGraph.setOwn(own);
        resourceGraph.setSecurityDataGraph(securityDataGraph);
        neo4jToDatabase.saveSecurityDataOwnWithResource(resource,securityData,own);

        return "1";
    }*/

    JsonObject securityObject = new JsonObject();

    String authUrl;
    String tokenUrl;
    // heck the oauth flow

    public String checkOAuth2Flow(String securityData){
        JsonObject jsonObject = new JsonParser().parse(securityData).getAsJsonObject();
        String flow = jsonObject.get("flow").getAsString();
        if(flow.equals("authorizationCode"))
            return runAuthorizationCodeFlow(securityData);
        if(flow.equals("implicit"))
            return runImplicitFlow(securityData);
        if(flow.equals("clientCredentials"))
            return runClientCredentialsFlow(securityData);
        if(flow.equals("resourceOwnerPassword"))
            return runResourceOwnerPasswordFlow(securityData);

        return null;


    }
    // run the Implicit Flow to get token
    public String runImplicitFlow(String securityData){
        JsonObject jsonObject = new JsonParser().parse(securityData).getAsJsonObject();
        // String redirect_uri = "http://140.121.197.130:55213/implicit";
        String redirect_uri = proberPathConfig.backEndURI + "/implicit";

        String resourceId = jsonObject.get("oasId").getAsString();
        String flow = null;
        String authenticate = null;

        authUrl = securityRepository.findAnnotationsAuthorizationUrlByResourceId(new Long(resourceId));
        //tokenUrl = securityRepository.findAnnotationsTokenUrlByResourceId(resourceId);


        securityObject.add("clientId", jsonObject.get("clientId"));
        securityObject.add("clientSecret", jsonObject.get("clientSecret"));
        securityObject.add("authorizationUrl", new JsonPrimitive(authUrl));
        //securityObject.add("tokenUrl", new JsonPrimitive(tokenUrl));
        securityObject.add("other", jsonObject.get("other"));

        authenticate = authUrl +
                "?client_id="+jsonObject.get("clientId").getAsString()+
                "&response_type=token" +
                "&redirect_uri="+redirect_uri+(jsonObject.get("other").getAsString());


        System.out.println(authenticate);
        return authenticate;
    }

    // run the Authorization Code Flow to get grant code
    public String runAuthorizationCodeFlow(String securityData){
        JsonObject jsonObject = new JsonParser().parse(securityData).getAsJsonObject();
        // String redirect_uri = "http://140.121.197.130:55213/auth";
        String redirect_uri = proberPathConfig.backEndURI + "/auth";

        String resourceId = jsonObject.get("oasId").getAsString();
        String flow = null;
        String authenticate = null;

        authUrl = securityRepository.findAnnotationsAuthorizationUrlByResourceId(new Long(resourceId));
        tokenUrl = securityRepository.findAnnotationsTokenUrlByResourceId(new Long(resourceId));
        System.out.println(tokenUrl);


        securityObject.add("clientId", jsonObject.get("clientId"));
        securityObject.add("clientSecret", jsonObject.get("clientSecret"));
        securityObject.add("authorizationUrl", new JsonPrimitive(authUrl));
        securityObject.add("tokenUrl", new JsonPrimitive(tokenUrl));
        securityObject.add("other", jsonObject.get("other"));

        authenticate = authUrl +
                "?client_id="+jsonObject.get("clientId").getAsString()+
                "&response_type=code" +
                "&redirect_uri="+redirect_uri + jsonObject.get("other").getAsString();



        //System.out.println(authenticate);
        return authenticate;
    }

    // get the token in Authorization Code Flow
    public String getToken(String code){
        // String redirect_uri = "http://140.121.197.130:55213/auth";
        String redirect_uri = proberPathConfig.backEndURI +  "/auth";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type","authorization_code");
        map.add("client_id",securityObject.get("clientId").getAsString());
        map.add("client_secret",securityObject.get("clientSecret").getAsString());
        map.add("code",code);
        map.add("redirect_uri",redirect_uri);
        //map.add("scope","read_station");
        System.out.println(map);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        // System.out.println(tokenUrl);
        // System.out.println(request);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

        JSONObject responseBody = new JSONObject(response.getBody());
        System.out.println(responseBody.get("access_token"));

        return  responseBody.toString();
    }
    // run Resource Owner Password Flow to get token
    public String runResourceOwnerPasswordFlow(String securityData){
        JsonObject jsonObject = new JsonParser().parse(securityData).getAsJsonObject();

        String resourceId = jsonObject.get("oasId").getAsString();



        tokenUrl = securityRepository.findAnnotationsTokenUrlByResourceId(new Long(resourceId));
        securityObject.add("clientId", jsonObject.get("clientId"));
        securityObject.add("clientSecret", jsonObject.get("clientSecret"));
        securityObject.add("tokenUrl", new JsonPrimitive(tokenUrl));
        securityObject.add("username", jsonObject.get("username"));
        securityObject.add("password", jsonObject.get("password"));

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type","password");
        map.add("client_id",securityObject.get("clientId").getAsString());
        map.add("client_secret",securityObject.get("clientSecret").getAsString());
        map.add("username", securityObject.get("username").getAsString());
        map.add("password", securityObject.get("password").getAsString());
        //map.add("scope","read_station");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        System.out.println(tokenUrl);

        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

        JSONObject responseBody = new JSONObject(response.getBody());
        System.out.println(responseBody.get("access_token"));
        return responseBody.get("access_token").toString();


    }
    // run the Client Credentials Flow to get token
    public String runClientCredentialsFlow(String securityData){
        JsonObject jsonObject = new JsonParser().parse(securityData).getAsJsonObject();

        String resourceId = jsonObject.get("oasId").getAsString();
        String flow = null;
        //String authenticate = null;


        tokenUrl = securityRepository.findAnnotationsTokenUrlByResourceId(new Long(resourceId));
        securityObject.add("clientId", jsonObject.get("clientId"));
        securityObject.add("clientSecret", jsonObject.get("clientSecret"));
        securityObject.add("tokenUrl", new JsonPrimitive(tokenUrl));
        securityObject.add("other", jsonObject.get("other"));


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type","client_credentials");
        map.add("client_id",securityObject.get("clientId").getAsString());
        map.add("client_secret",securityObject.get("clientSecret").getAsString());
        map.add("other", securityObject.get("other").getAsString());


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        System.out.println(tokenUrl);

        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

        JSONObject responseBody = new JSONObject(response.getBody());
        System.out.println(response.toString());

        System.out.println(responseBody.get("access_token"));
        return responseBody.get("access_token").toString();


    }
}
