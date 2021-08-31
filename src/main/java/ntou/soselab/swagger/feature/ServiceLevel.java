package ntou.soselab.swagger.feature;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

@Component
public class ServiceLevel {
    Logger log = LoggerFactory.getLogger(ServiceLevel.class);

    public ArrayList<String> parseSwaggerService(OpenAPI swagger){
        boolean httpsFlag = false;

        // Service Level check
        int operationQuantity = 0;
        //List<Scheme> schemes = null;
        Map<String, SecurityScheme> securitySchemeDefinitions = null;

        // save restful feture
        ArrayList<String> feture = new ArrayList<>();

        //schemes = swagger.getSchemes();
        if(swagger.getComponents() != null && swagger.getComponents().getSecuritySchemes() != null) {
            securitySchemeDefinitions = swagger.getComponents().getSecuritySchemes();
        }
        /*if(schemes != null){
            for(Scheme scheme : schemes){
                if(scheme.toValue().toLowerCase().equals("https")){
                    feture.add("HTTPS support");
                    httpsFlag = true;
                    break;
                }
            }
        }*/
        System.out.println("----------");
        if(securitySchemeDefinitions != null){
            for(String key : securitySchemeDefinitions.keySet()){
                SecurityScheme securityScheme = securitySchemeDefinitions.get(key);

                if(securityScheme.getType().toString().equals("http")){
                    feture.add("HTTP authentication schemes");
                    break;
                }else if(securityScheme.getType().toString().equals("apiKey")){
                    feture.add("API Key");
                    break;
                }else if(securityScheme.getType().toString().equals("oauth2")){
                    System.out.println(securityScheme.getType());
                    feture.add("OAuth2");
                    break;
                }
            }
        }

        for(String p : swagger.getPaths().keySet()){
            if (swagger.getPaths().get(p).getDelete() != null) {
                operationQuantity++;
            }
            if (swagger.getPaths().get(p).getGet() != null) {
                operationQuantity++;
            }
            if (swagger.getPaths().get(p).getPost() != null) {
                operationQuantity++;
            }
            if (swagger.getPaths().get(p).getPut() != null) {
                operationQuantity++;
            }
            if (swagger.getPaths().get(p).getPatch() != null) {
                operationQuantity++;
            }
        }
        if(operationQuantity >= 20){
            feture.add("At most 20 operations");
        }

        return feture;
    }
}

