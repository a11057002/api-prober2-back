package ntou.soselab.swagger.neo4j.domain.service;


import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class SecurityData extends ConcreteService{
    String clientId = "";
    String clientSecret = "";
    String apiKey = "";
    String token = "";
    String bearer = "";
    String basic;
    String note = "";

    public SecurityData(){ }


    public SecurityData(String clientId, String clientSecret, String apiKey, String token, String bearer, String basic) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.apiKey = apiKey;
        this.token = token;
        this.bearer = bearer;
        this.basic = basic;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBearer() {
        return bearer;
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    public String getBasic() {
        return basic;
    }

    public void setBasic(String basic) {
        this.basic = basic;
    }


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
