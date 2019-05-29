/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.sitech.csd.codecheck.sql.xml;

import com.sitech.csd.codecheck.sql.Check;
import org.apache.ibatis.builder.*;
import org.apache.ibatis.builder.xml.XMLIncludeTransformer;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * @author shixq
 * 负责Mapper.xml 解析的类
 */
public class XMLMapperBuilder extends BaseBuilder {

  private final XPathParser parser;
  private final MapperBuilderAssistant builderAssistant;
  private final Map<String, XNode> sqlFragments;
  private final String resource;

  @Deprecated
  public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
    this(reader, configuration, resource, sqlFragments);
    this.builderAssistant.setCurrentNamespace(namespace);
  }

  @Deprecated
  public XMLMapperBuilder(Reader reader, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
    this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()),
        configuration, resource, sqlFragments);
  }

  public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments, String namespace) {
    this(inputStream, configuration, resource, sqlFragments);
    this.builderAssistant.setCurrentNamespace(namespace);
  }

  public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
    this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()),
        configuration, resource, sqlFragments);
  }

  private XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource, Map<String, XNode> sqlFragments) {
    super(configuration);
    this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
    this.parser = parser;
    this.sqlFragments = sqlFragments;
    this.resource = resource;
  }

  /**
   * 解析某个 xxx.xml
   */
  public void parse(StringBuilder stringBuilder) {
    try {
      XNode context= parser.evalNode("/mapper");
      sqlElement(context.evalNodes("/mapper/sql"));
      List<XNode> list = context.evalNodes("select|insert|update|delete");
//      StringBuilder stringBuilder = new StringBuilder();
      for (XNode x:list
              ) {
        XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
        includeParser.applyIncludes(context.getNode());
        String sql2 = context.evalString("select|insert|update|delete").replaceAll("\n","").replaceAll("\\$\\{.*?}","?").replaceAll("(#)\\{.*?\\}","?").replaceAll("\t"," ");
        String sql = getTableName(sql2,stringBuilder);
        XNode xNode = context.evalNode("select|insert|update|delete");
        context.getNode().removeChild(x.getNode());
        FileWriter fileWriter = new FileWriter(Check.path2);
        fileWriter.write("\n"+sql.replace("`","")+";");
        fileWriter.close();
        System.out.println(sql);
      }
    } catch (Exception e) {
      throw new BuilderException("Error parsing Mapper XML. Cause: " + e, e);
    }
  }

  public String getTableName(String sql,StringBuilder res){
    String trimSql = this.trimSql(sql);
    String[] s3 =  trimSql.split(" ");
    for (int i = 0; i < s3.length; i++) {
      if ("from".equalsIgnoreCase(s3[i])){
        if (res.toString().indexOf(s3[i+1])==-1){
          res.append(s3[i+1]).append("\n");
        }
      }
      if ("update".equalsIgnoreCase(s3[i])){
        if (res.toString().indexOf(s3[i+1])==-1){
          res.append(s3[i+1]).append("\n");
        }
      }
      if ("into".equalsIgnoreCase(s3[i])){
        if (res.toString().indexOf(s3[i+1])==-1){
          res.append(s3[i+1]).append("\n");
        }
      }
    }
    return res.toString();
  }

  public String trimSql(String sql){
    StringBuilder stringBuilder = new StringBuilder();
    String[] s2 = sql.split(" ");
    for (int i = 0; i < s2.length; i++) {
      if (!"".equals(s2[i].trim())){
        stringBuilder.append(s2[i].split("\\(")[0]).append(" ");
      }
    }
    return stringBuilder.toString();
  }


    /**
     * 解析 <sql> 节点
     * @param list
     * @throws Exception
     */
  private void sqlElement(List<XNode> list) throws Exception {
    if (configuration.getDatabaseId() != null) {
      sqlElement(list, configuration.getDatabaseId());
    }
    sqlElement(list, null);
  }

    /**
     * 解析 <sql> 节点
     *
     * @param list
     * @param requiredDatabaseId
     * @throws Exception
     */
    private void sqlElement(List<XNode> list, String requiredDatabaseId) throws Exception {
        // 遍历 <sql> 节点
        for (XNode context : list) {
            // 获取 databaseId 属性
            String databaseId = context.getStringAttribute("databaseId");
            // 获取 id 属性
            String id = context.getStringAttribute("id");
            // 为 id 添加命名空间
            id = builderAssistant.applyCurrentNamespace(id, false);
            // 检查 sql 节点的 databaseId 与当前 Configuration 中的是否一致
            if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
                // 记录到 XMLMapperBuider.sqlFragments(Map<String, XNode>)中保存
                // 其最终是指向了 Configuration.sqlFragments(configuration.getSqlFragments) 集合
                sqlFragments.put(id, context);
            }
        }
    }

  private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
    if (requiredDatabaseId != null) {
      if (!requiredDatabaseId.equals(databaseId)) {
        return false;
      }
    } else {
      if (databaseId != null) {
        return false;
      }
      // skip this fragment if there is a previous one with a not null databaseId
      if (this.sqlFragments.containsKey(id)) {
        XNode context = this.sqlFragments.get(id);
        if (context.getStringAttribute("databaseId") != null) {
          return false;
        }
      }
    }
    return true;
  }




}
