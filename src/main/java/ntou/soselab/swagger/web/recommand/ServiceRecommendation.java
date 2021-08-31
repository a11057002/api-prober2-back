package ntou.soselab.swagger.web.recommand;

import java.util.ArrayList;

public class ServiceRecommendation {
    ArrayList<Similarity> similaritys;
    ArrayList<Mashup> mashups;

    public ArrayList<Similarity> getSimilaritys() {
        return similaritys;
    }

    public void setSimilaritys(ArrayList<Similarity> similaritys) {
        this.similaritys = similaritys;
    }

    public ArrayList<Mashup> getMashups() {
        return mashups;
    }

    public void setMashups(ArrayList<Mashup> mashups) {
        this.mashups = mashups;
    }
}
