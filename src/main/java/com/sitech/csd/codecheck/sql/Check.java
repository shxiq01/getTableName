package com.sitech.csd.codecheck.sql;

import com.sitech.csd.codecheck.sql.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shixiaoqi on 2019/3/20.
 */
public class Check {
    public static List<String> xmlFiles = new ArrayList<String>();
    public static String FilePath;
    public static String path = "/Users/shixiaoqi/IdeaProjects/ga-userinfo/src/main/resources/sqlxml";
    public static String path2 = "/Users/shixiaoqi/IdeaProjects/ga-userinfo/src/main/resources/sqlxml/test.txt";
    public static StringBuilder stringBuilder = new StringBuilder();

    public static Logger logger = LoggerFactory.getLogger(Check.class);
    public static void main(String[] args) {
//        String path = args[0];
        File files = new File(path);
        logger.info("开始递归获取目录："+path+"下的Mapper.xml结尾的文件");
        xmlFiles = print(files);
        for (String s:xmlFiles
                ) {
            FilePath = s.replace(".xml",".sql");
            File file = new File(FilePath);
            if (file.exists()){
                file.delete();
            }
            logger.info("解析xml文件,并将静态sql写入新的sql文件中--->"+FilePath);
            writeFile(s,stringBuilder);
        }
    }
    /**
     * 利用Mybatis-XMLMapperBuilder解析xml文件并写入.sql文件
     * @param resource
     */
    public static void writeFile(String resource,StringBuilder stringBuilder){
        Configuration configuration = new Configuration();
        try {
            InputStream inputStream = new FileInputStream(new File(resource));
            XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
            builder.parse(stringBuilder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取项目下所有Mapper.xml结尾的文件
     * @param file
     * @return
     */
    public static List<String> print(File file){
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().endsWith("Mapper.xml")) {
                        xmlFiles.add(files[i].getPath());
                    }
                    print(files[i]);
                }
            }
        }
        return xmlFiles;
    }
}
