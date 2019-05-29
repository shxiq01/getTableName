package com.sitech.csd.codecheck.sql.xml;

import com.sitech.csd.codecheck.sql.Check;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLIncludeTransformer;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by shixiaoqi on 2019/3/20.
 * 已废弃
 */
public class MyXMLStatementBuilder extends XMLStatementBuilder {

    private final MapperBuilderAssistant builderAssistant;
    private final XNode context;
    private final String requiredDatabaseId;
    public static Logger logger = LoggerFactory.getLogger(MyXMLStatementBuilder.class);
    public MyXMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context) {
        this(configuration, builderAssistant, context, null);
    }

    public MyXMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, XNode context, String databaseId) {
        super(configuration,builderAssistant,context,databaseId);
        this.builderAssistant = builderAssistant;
        this.context = context;
        this.requiredDatabaseId = databaseId;
    }

    @Override
    public void parseStatementNode() {
        SqlSource sqlSource = null;
        try {
            Class<?> parameterTypeClass = null;
            String lang = context.getStringAttribute("lang");
            LanguageDriver langDriver = getLanguageDriver(lang);
            // 引入include 解析出的 sql 节点内容
            XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
            includeParser.applyIncludes(context.getNode());

            sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
        } catch (Exception e) {
            return;
        }
        if (sqlSource.getClass().toString().equals("class org.apache.ibatis.scripting.defaults.RawSqlSource")){
            try {
                FileWriter fileWriter = new FileWriter(Check.FilePath,true);
                fileWriter.write("\n"+sqlSource.getBoundSql(null).getSql().replace("`","")+";");
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
//      System.out.println("动态"+sqlSource.getClass());
//      HashMap<String, Object> parameterObject = new HashMap<String, Object>() {{
//        put("list", new Integer[] {1});
//      }};
//      System.out.println("动态"+sqlSource.getBoundSql(parameterObject).getSql());
        }
    }

    private LanguageDriver getLanguageDriver(String lang) {
        Class<?> langClass = null;
        if (lang != null) {
            langClass = resolveClass(lang);
        }
        return builderAssistant.getLanguageDriver(langClass);
    }
}
