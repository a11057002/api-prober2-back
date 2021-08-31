package ntou.soselab.swagger.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@Service
public class JavaMethodParser {

    Logger log = LoggerFactory.getLogger(JavaMethodParser.class);

    static String URI = "";

    // record all method result
    HashMap<String, Double> methodResult;

    // record java doc import lib;
    ArrayList<String> javaDocumentLibrary;

    // record fake endpoint
    ArrayList<String> fakeEndpoints;

    ArrayList<String> librarys = new ArrayList<String>();

    public HashMap<String, Double> getJavaMethodUse(ArrayList<String> containTargetEndpoints, String uri, String javaDocPath) throws IOException {

        // 已知的 java library
        librarys.add("import okhttp3.Request;");
        librarys.add("import org.springframework.web.client.RestTemplate;");
        librarys.add("import org.apache.http.client.HttpClient;");
        librarys.add("import com.loopj.android.http.AsyncHttpClient;");
        librarys.add("import com.android.volley.toolbox.StringRequest;");
        librarys.add("import com.android.volley.toolbox.JsonObjectRequest;");
        librarys.add("import org.apache.commons.httpclient.HttpClient;");
        librarys.add("import org.apache.httpcore.HttpRequest;");
        librarys.add("import com.mashape.unirest.http.Unirest;");

        methodResult = new HashMap<String, Double>();

        javaDocumentLibrary = new ArrayList<>();

        fakeEndpoints = new ArrayList<>(containTargetEndpoints);

        URI = uri;
        // creates an input stream for the file to be parsed
        FileInputStream in = new FileInputStream(javaDocPath);

        // parse it
        try{
            // 設定不分析 comment
            JavaParser.getStaticConfiguration().setAttributeComments(false);
            CompilationUnit cu = JavaParser.parse(in);

            // visit and print the class variable
            // cu.accept(new ClassVisitor(), null);

            // get java class import
            for(ImportDeclaration str : cu.getImports()) {
                javaDocumentLibrary.add(str.toString());
            }


            ClassVisitor classVisitor = new ClassVisitor();
            classVisitor.visit(cu, null);
            // log.info("Match Methods Name :{}", codeFragment.getFragment());
            // log.info("Match Methods Name :{}", methodResult);
        }catch (Exception e) {
            log.info("error document format:{}", javaDocPath);
        }


//        for(TypeDeclaration type : cu.getTypes()) {
//            // first give all this java doc member
//            List<BodyDeclaration> members = type.getMembers();
//            // check all member content
//            for(BodyDeclaration member : members) {
//                // if member state equal ClassOrInterfaceDeclaration, and you can identify it which is inner class
//                if(member.isClassOrInterfaceDeclaration()) {
//                    log.info("class name :{}", member.asClassOrInterfaceDeclaration().getName());
//                    // get inner class method
//                    for(MethodDeclaration method : member.asClassOrInterfaceDeclaration().getMethods()) {
//                        log.info("Method Name :{}", method.getName());
//                    }
//                    VerifyInnerClassAndParse(member.asClassOrInterfaceDeclaration());
//                }
//            }
//        }
        return methodResult;
    }

    class ClassVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        /* here you can access the attributes of the method.
         this method will be called for all methods in this
         CompilationUnit, including inner class methods */

            // find url fragment
            findAPIUseFragment(n);
            // verify inner class
            verifyInnerClassAndParse(n);
        }
    }

    public void findAPIUseFragment(ClassOrInterfaceDeclaration n) {
        // get class variable name and content
        for(FieldDeclaration field : n.getFields()) {
            //System.out.println(field.getVariables());
            for(VariableDeclarator variable : field.getVariables()) {
                // System.out.println(variable.getName() + " : " + variable.getInitializer().get());
                // log.info("Variable Name :{}", variable.getName());

                // 如果有 {} 將裡面內容變為 xx 之後去除所有符號
                String api[] = URI.replaceAll("(?<=\\{)(?!\\s*\\{)[^{}]+","xx").replaceAll("[\\pP\\p{Punct}]"," ").split(" ");

                // 避免抓到空值
                if(variable.getInitializer().orElse(null) != null){
                    String fragmentUri[] = variable.getInitializer().get().toString().replaceAll("[?]", " st ").replaceAll("[\\pP\\p{Punct}]"," ").replaceAll("\r\n|\r|\n", " ").split(" ");

                    boolean flag = true;
                    // 確認 類別變數不包含 該 api 的超集
                    for(String fakeEndpoint : fakeEndpoints) {
                        String fakeAPI[] = fakeEndpoint.replaceAll("(?<=\\{)(?!\\s*\\{)[^{}]+","xx").replaceAll("[\\pP\\p{Punct}]"," ").split(" ");
                        if(precisionCompareCodeWithoutBuffer(fakeAPI, fragmentUri)) {
                            flag = false;
                            break;
                        }
                    }

                    if(flag) {
                        // 避免有人在測試回傳格式 導致誤判
                        if(fragmentUri.length > (api.length*2)) {
                            log.info("To Long");
                        }else {
                            if(precisionCompareCodeWithoutBuffer(api, fragmentUri)) {
                                String variableName = variable.getName().toString();
                                log.info("Representative Uri :{}", variableName);
                                for(MethodDeclaration method : n.getMethods()) {
                                    //System.out.println("Name :" + method.getName());
                                    //System.out.println("Body :" + method.getBody().get());
                                    log.info("Search Representative Method Name :{}", method.getName());
                                    String vName[] = variableName.replaceAll("[\\pP\\p{Punct}]"," ").replaceAll("\r\n|\r|\n", " ").split(" ");
                                    if(method.getBody().orElse(null) != null) {
                                        String methodBody[] = method.getBody().get().toString().replaceAll("[\\pP\\p{Punct}]"," ").replaceAll("\r\n|\r|\n", " ").split(" ");
                                        // 比對 variable api
                                        if(compareCode(vName, methodBody)) {
                                            // codeFragment.setFragment(method.getName().toString());
                                            methodResult.put(variable.toString()+"\n"+method.toString(), 3.0);
                                            detectJavaLibraryUse(variable.toString()+"\n"+method.toString());
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        // get class methods
        for(MethodDeclaration method : n.getMethods()) {
            //System.out.println("Name :" + method.getName());
            //System.out.println("Body :" + method.getBody().get());
            //log.info("Method Name :{}", method.getName());
            String api[] = URI.replaceAll("(?<=\\{)(?!\\s*\\{)[^{}]+","xx").replaceAll("[\\pP\\p{Punct}]"," ").split(" ");
            if(method.getBody().orElse(null) != null) {

                Optional<BlockStmt> block = method.getBody();
                NodeList<Statement> statements = block.get().getStatements();


                // 分析 method body 的 每一段 程式碼
                for (Statement tmp : statements) {
                    // 判斷是否在 Return 片段時才出現
                    if(tmp.isReturnStmt()) {
                        //tmp.removeComment();
                        String statementArray[] = tmp.toString().replaceAll("[?]", " st ").replaceAll(";", " st ").replaceAll(",", " st ").replaceAll("[\\pP\\p{Punct}]"," ").replaceAll("\r\n|\r|\n", " ").split(" ");
                        // 先確認在此段 有找到疑似的 api 使用
                        if(precisionCompareCodeWithoutBuffer(api, statementArray)) {
                            boolean flag = true;

                            // 再去確認 該片段不包含 該 api 的超集
                            for(String fakeEndpoint : fakeEndpoints) {
                                String fakeAPI[] = fakeEndpoint.replaceAll("(?<=\\{)(?!\\s*\\{)[^{}]+","xx").replaceAll("[\\pP\\p{Punct}]"," ").split(" ");
                                if(precisionCompareCodeWithoutBuffer(fakeAPI, statementArray)) {
                                    flag = false;
                                    break;
                                }
                            }

                            if(flag) {
                                methodResult.put(method.toString(), 2.0);
                                detectJavaLibraryUse(method.toString());
                            }
                        }
                    }else {
                        //tmp.removeComment();
                        String statementArray[] = tmp.toString().replaceAll("[?]", " st ").replaceAll(";", " st ").replaceAll(",", " st ").replaceAll("[\\pP\\p{Punct}]"," ").replaceAll("\r\n|\r|\n", " ").split(" ");
                        if(precisionCompareCodeWithoutBuffer(api, statementArray)) {
                            boolean flag = true;

                            // 再去確認 該片段不包含 該 api 的超集
                            for(String fakeEndpoint : fakeEndpoints) {
                                String fakeAPI[] = fakeEndpoint.replaceAll("(?<=\\{)(?!\\s*\\{)[^{}]+","xx").replaceAll("[\\pP\\p{Punct}]"," ").split(" ");
                                if(precisionCompareCodeWithoutBuffer(fakeAPI, statementArray)) {
                                    flag = false;
                                    break;
                                }
                            }

                            if(flag) {
                                methodResult.put(method.toString(), 3.0);
                                detectJavaLibraryUse(method.toString());
                            }
                        }
                    }
                }
            }
        }
    }

    // 檢查 inner class 遞迴
    public void verifyInnerClassAndParse(ClassOrInterfaceDeclaration innerClass) {
        for(BodyDeclaration member : innerClass.getMembers()) {
            if(member.isClassOrInterfaceDeclaration()) {
                // log.info("inner class name :{}", member.asClassOrInterfaceDeclaration().getName());
                // 再次尋找 api fragment
                findAPIUseFragment(member.asClassOrInterfaceDeclaration());
                // 遞迴檢查 inner class
                verifyInnerClassAndParse(member.asClassOrInterfaceDeclaration());
            }
        }
    }

    // 當 variables 變數是名稱時 比對
    public boolean compareCode(String[] api, String[] fragmentUri){
        for(String key : api){
            boolean flag = false;
            for(String key1 : fragmentUri){
                if(key.equals(key1)){
                    //System.out.println(key);
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return false;
            }
        }
        return true;
    }

    // 針對 endpoint 更嚴格定義，沒有 buffer 設計，可以接受有資源操作的 endpoint
    public boolean precisionCompareCodeWithoutBuffer(String[] api, String[] fragmentUri) {
        api = replaceStringNullValue(api);
        fragmentUri = replaceStringNullValue(fragmentUri);
//        log.info("api-2 :{}", Arrays.toString(api));
//        log.info("fragmentUri-2 :{}", Arrays.toString(fragmentUri));
        for(int i = 0;i < fragmentUri.length;i++) {
            // 考慮 API 不一定用 http or https
            if(fragmentUri[i].equals("http") || fragmentUri[i].equals("https")) {
                int offset = i;
                for(int j = 1;j < api.length;j++) {
                    boolean flag = true;
                    while(++offset < fragmentUri.length) {
                        if(fragmentUri[offset].equals("st")) {
                            //log.info("已經碰到 ? 所以停止");
                            flag = false;
                            break;
                        }
                        if(!fragmentUri[offset].equals(api[j]) && !api[j].equals("xx")) {
                            flag = false;
                        }else if(fragmentUri[offset].equals(api[j]) || api[j].equals("xx")) {
                            // 比到最後一個 api token 皆相等 回傳 true
                            if(j == api.length-1) {
                                // log.info("成功");
                                return true;
                            }else {
                                break;
                            }
                        }
                        if(!flag) {
                            break;
                        }
                    }
                    if(!flag) {
                        break;
                    }
                }
            }
        }
        return false;
    }

    public String[] replaceStringNullValue(String str[]) {
        ArrayList<String> tmp = new ArrayList<String>();

        for(String word:str){
            if(word!=null && word.length()!=0){
                tmp.add(word);
            }
        }
        str= tmp.toArray(new String[0]);
        return str;
    }

    // 評分 import library
    public void detectJavaLibraryUse(String method) {
        // 紀錄是否有 使用 Http 函式庫
        boolean flag = false;
        // check 該 java document 所有 import
        for(String importLib : javaDocumentLibrary) {
            // 比對已知的 library
            String[] javaLibrary = importLib.replaceAll("\\."," ").replaceAll(";", " ").trim().split(" ");
            for(String library : librarys) {
                String[] javaKnowLibrary = library.replaceAll("\\."," ").replaceAll(";", " ").trim().split(" ");
//                log.info("lib 1 :{}", Arrays.toString(javaLibrary));
//                log.info("lib 2 :{}", Arrays.toString(javaKnowLibrary));
                // 比對內容
                for(int x = 0 ; x<javaKnowLibrary.length ; x++) {
                    if(javaLibrary[x].equals(javaKnowLibrary[x])) {
                        if(x == javaKnowLibrary.length-1) {
                            flag = true;
                            break;
                        }else {
                            continue;
                        }
                    }else if(x > 1 && javaLibrary[x].equals("*")) {
                        flag = true;
                        break;
                    }else if(!javaLibrary[x].equals(javaKnowLibrary[x])) {
                        // log.info("miss on :{}", javaLibrary[x]);
                        break;
                    }
                }

                if(flag) break;
            }
            if(flag) {
                double score = methodResult.get(method) + 2.0;
                methodResult.put(method, score);
                break;
            }
        }
    }


//    private static class ClassVisitor extends VoidVisitorAdapter<Void> {
//        @Override
//        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
//        /* here you can access the attributes of the method.
//         this method will be called for all methods in this
//         CompilationUnit, including inner class methods */
//            System.out.println(n.getFields());
//            System.out.println("-----------------------------------------------------");
//
//            // get class variable name and content
//            for(FieldDeclaration field : n.getFields()) {
//                System.out.println(field.getVariables());
//                for(VariableDeclarator variable : field.getVariables()) {
//                    System.out.println(variable.getName() + " : " + variable.getInitializer().get());
//                }
//            }
//
//            // get class methods
//            for(MethodDeclaration method : n.getMethods()) {
//                System.out.println("Name :" + method.getName());
//                System.out.println("Body :" + method.getBody().get());
//            }
//
//            super.visit(n, arg);
//        }
//    }
}
