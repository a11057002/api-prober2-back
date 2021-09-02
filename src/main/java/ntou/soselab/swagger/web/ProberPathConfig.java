package ntou.soselab.swagger.web;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ProberPathConfig{
    @Value("${prober.backend.uri}")
    public String backEndURI;

    @Value("${prober.frontend.uri}")
    public String frontEndURI;

    @Value("${prober.wordnet.folder}")
    public String wordNetPath;

    @Value("${prober.downloadGithub.folder}")
    public String downloadGithubPath;

}