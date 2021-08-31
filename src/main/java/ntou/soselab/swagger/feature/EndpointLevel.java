package ntou.soselab.swagger.feature;


import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class EndpointLevel {
    Logger log = LoggerFactory.getLogger(EndpointLevel.class);

    public ArrayList<String> parseSwaggerEndpoint(OpenAPI swagger, io.swagger.v3.oas.models.Operation swaggerOperation, String path){
        ArrayList<String> feture = new ArrayList<>();

        boolean checkRestful = path.contains("{");

        if(checkRestful){
            feture.add("REST-style URls");
        }
        feture = checkStatusCodeAndErrorMessages(swaggerOperation, feture);
        //feture = checkInputFormatAndOutputFormat(swagger, swaggerOperation, feture);


        return feture;
    }

    public ArrayList<String> checkStatusCodeAndErrorMessages(io.swagger.v3.oas.models.Operation swaggerOperation, ArrayList<String> feture){
        if(swaggerOperation.getResponses() != null){
            feture.add("HTTP status code use");
            for(String key : swaggerOperation.getResponses().keySet()){
                if(key.toLowerCase().equals("default")){
                    feture.add("Explain Error messages");
                    break;
                }
                int statusCode = Integer.valueOf(key); //小心接收到 default
                if(statusCode >= 300){
                    feture.add("Explain Error messages");
                    log.info("status code :{}", key);
                    break;
                }
            }
        }
        if(swaggerOperation.getSecurity() != null){
            feture.add("Authentication");
        }
        return feture;
    }

    /*public ArrayList<String> checkInputFormatAndOutputFormat(OpenAPI swagger, Operation swaggerOperation, ArrayList<String> feture){
        if(swaggerOperation.getConsumes() != null){
            if(swaggerOperation.getConsumes().contains("application/json")){
                if(!feture.contains("Input format JSON")){
                    feture.add("Input format JSON");
                }
            }
        }else {
            List<String> consumes = null;
            consumes = swagger.getConsumes();
            if(consumes != null){
                for(String consume : consumes){
                    if(consume.equals("application/json")){
                        feture.add("Input format JSON");
                        break;
                    }
                }
            }
        }

        if(swaggerOperation.getProduces() != null){
            if(swaggerOperation.getProduces().contains("application/json")){
                if(!feture.contains("Output format JSON")){
                    feture.add("Output format JSON");
                }
            }
        }else {
            List<String> produces = null;
            produces = swagger.getProduces();
            if(produces != null){
                for(String produce : produces){
                    if(produce.equals("application/json")){
                        feture.add("Output format JSON");
                        break;
                    }
                }
            }
        }

        return  feture;
    }*/
}
