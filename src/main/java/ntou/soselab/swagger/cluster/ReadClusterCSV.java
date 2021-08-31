package ntou.soselab.swagger.cluster;

import ntou.soselab.swagger.neo4j.domain.service.Resource;
import ntou.soselab.swagger.neo4j.repositories.service.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

@Component
public class ReadClusterCSV {

    Logger log = LoggerFactory.getLogger(ReadClusterCSV.class);

    @Autowired
    ResourceRepository resourceRepository;

    public HashMap<Long, String> readClusterResult() {
        HashMap<Long, String> cluster = new HashMap<Long, String>();

        try {
            FileReader fr = new FileReader("./src/main/resources/Cluster-Results.csv");//抓CSV檔進java
            BufferedReader brdFile = new BufferedReader(fr);//bufferedReader
            String strLine = null;

            boolean flag = false;
            while((strLine = brdFile.readLine())!=null){//將CSV檔字串一列一列讀入並存起來直到沒有列為止
                // 避免讀取第一行標頭
                if(!flag) {
                    flag = true;
                }else {
                    String[] array=strLine.split(",");//因為預設是用"，"分開所以用split切開存入字串陣列
                    System.out.println(strLine);

                    // 取得 resource ID
                    String resource[] = array[1].split("-");
                    Long resourceId = Long.valueOf(resource[0]);
                    log.info("resourceId :{}", resourceId);

                    // 取得 cluster 分組
                    String clusterGroup = array[2];
                    log.info("clusterGroup :{}", clusterGroup);

                    cluster.put(resourceId, clusterGroup);
                }
            }
        }catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cluster;
    }

    public void saveClusterToNeo4j(HashMap<Long, String> cluster) {
        for(Long id : cluster.keySet()) {
            Resource resource = resourceRepository.findResourceById(id);

            resource.setClusterGroup(cluster.get(id));
            resourceRepository.save(resource);
        }
    }
}