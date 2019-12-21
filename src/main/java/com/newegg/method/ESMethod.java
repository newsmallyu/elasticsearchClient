package com.newegg.method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface ESMethod {


    public String creatIndex(String indexName) throws IOException;

    public String creatIndex(String indexName, String body) throws IOException;

    public String insert(String index, String id, String body) throws IOException;

    public String insert(String index, String body) throws IOException;

    /**
     * Template是否存在
     * @param templateName
     * @return  true 为已存在  false不存在
     */
    public boolean existsTemplate(String  templateName) throws IOException;
    /**
     * 修改数据
     * @param index
     * @param id
     * @param body 原来的数据将被body覆盖
     * @return
     * @throws IOException
     */
    public String update(String index, String id, String body) throws IOException;

    public void bulk(ArrayList<String> bodyList ) throws IOException;
    public void bulk(ArrayList<String> bodyList,int bulkFailRetry ) throws IOException;
    public void bulk(String index, ArrayList<String> bodyList,int bulkFailRetry) throws IOException;

    /**
     * 批量执行   List中的每一条数据格式如(为完整的可执行命令 \n不可省略)："{ \"index\" : { \"_id\" : \"1234\" } }\n{ \"name\" : \"aiden7\" }\n";
     * @param index
     * @param bodyList
     * @param bulkFailRetry 失败重试次数   设置为-1时，会一直重试，直至全部成功
     * @param bulkFailRetryInterval  重试间隔
     * @throws IOException
     */
    public void bulk(String index, ArrayList<String> bodyList,int bulkFailRetry,int bulkFailRetryInterval) throws IOException;

    public String searchWithBody(String body) throws IOException;
    public String search(String index) throws IOException;
    public String search(String index, String body) throws IOException;

    /**
     *
     * @param index
     * @return 200:true exist ; 404:false  not exist
     */
    public boolean indexExists(String index) throws IOException;

    /**
     * 获取index中的全部数据
     * @param index
     * @param size
     * @return
     * @throws IOException
     */
    public List scrollAll(String index, int size) throws IOException;

    public String clusterHealth() throws IOException;

    public String getTemplate(String templateName) throws IOException;

    public String putTemplate(String templateName,String body) throws IOException;

    public String delTemplate(String templateName) throws IOException;

    public String getMapping(String indexName) throws IOException;

    public String putMapping(String indexName, String body) throws IOException;
}
