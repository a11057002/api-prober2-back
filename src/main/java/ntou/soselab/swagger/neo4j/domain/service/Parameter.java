package ntou.soselab.swagger.neo4j.domain.service;

import ntou.soselab.swagger.neo4j.domain.relationship.Input;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Parameter extends ConcreteService {

    String name;
    String in;
    String description;
    String media_type;
    String format;
    boolean required;
    String example;
    ArrayList<String> originalWord;
    ArrayList<String> wordnetWord;
    String value;

    public Parameter() {
        super();
    }

    public Parameter(String name, String in, String description, String media_type, String format, boolean required, ArrayList<String> originalWord, ArrayList<String> wordnetWord, String example) {
        this.name = name;
        this.in = in;
        this.description = description;
        this.media_type = media_type;
        this.format = format;
        this.required = required;
        this.originalWord = originalWord;
        this.wordnetWord = wordnetWord;
        this.example = example;
    }
    public Parameter(String name, String in, String value){
        this.name = name;
        this.in = in;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMedia_type() {
        return media_type;
    }

    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public ArrayList<String> getOriginalWord() {
        return originalWord;
    }

    public void setOriginalWord(ArrayList<String> originalWord) {
        this.originalWord = originalWord;
    }

    public ArrayList<String> getWordnetWord() {
        return wordnetWord;
    }

    public void setWordnetWord(ArrayList<String> wordnetWord) {
        this.wordnetWord = wordnetWord;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Relationship(type = "input", direction = Relationship.INCOMING)
    Set<Input> inputs = new HashSet<>();

    public Set<Input> getInputs() {
        return inputs;
    }
    public void setInput(Input input) {
        this.inputs.add(input);
    }
}
