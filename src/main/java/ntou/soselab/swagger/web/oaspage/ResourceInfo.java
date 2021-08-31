package ntou.soselab.swagger.web.oaspage;

import java.util.ArrayList;

public class ResourceInfo {

    String title;
    ArrayList<String> features;
    String description;
    String provider;
    String host;
    String baseUrl;
    String contact;
    String swaggerUrl;
    ArrayList<PathInfo> endpoints;
    String proberVersionOAS;
    double serviceTesting;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<String> features) {
        this.features = features;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getSwaggerUrl() {
        return swaggerUrl;
    }

    public void setSwaggerUrl(String swaggerUrl) {
        this.swaggerUrl = swaggerUrl;
    }

    public ArrayList<PathInfo> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(ArrayList<PathInfo> endpoints) {
        this.endpoints = endpoints;
    }

    public String getProberVersionOAS() {
        return proberVersionOAS;
    }

    public void setProberVersionOAS(String proberVersionOAS) {
        this.proberVersionOAS = proberVersionOAS;
    }

    public double getServiceTesting() {
        return serviceTesting;
    }

    public void setServiceTesting(double serviceTesting) {
        this.serviceTesting = serviceTesting;
    }
}
