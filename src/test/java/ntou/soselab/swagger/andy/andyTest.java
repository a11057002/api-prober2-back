package ntou.soselab.swagger.andy;

import org.junit.Test;
import org.junit.runner.RunWith;
import ntou.soselab.swagger.web.ProberPathConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class andyTest{

    @Autowired
    private ProberPathConfig proberPathConfig;

    public andyTest(){

    }

    @Test
    public void test(){
        System.out.println("=================================================================");
        System.out.println(proberPathConfig.backEndURI);
        System.out.println(proberPathConfig.frontEndURI);
        System.out.println(proberPathConfig.wordNetPath);
        System.out.println(proberPathConfig.downloadGithubPath);
        System.out.println("=================================================================");
    }

}
