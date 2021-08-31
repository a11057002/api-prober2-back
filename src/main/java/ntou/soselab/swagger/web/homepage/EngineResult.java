package ntou.soselab.swagger.web.homepage;

import java.util.ArrayList;

public class EngineResult {

    String image;
    String resourceId;
    String resourceName;
    ArrayList<String> feature;
    String description;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public ArrayList<String> getFeature() {
        return feature;
    }

    public void setFeature(ArrayList<String> feature) {
        this.feature = feature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
