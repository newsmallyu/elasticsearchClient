package com.newegg.method;

import org.elasticsearch.client.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface ESClient {
    /**
     * 自定义请求  如其他方法不能满足需求，可使用此方法
     * @param method_name  ex:POST PUT GET DELETE
     * @param endpoint   ex:/_cat/health
     * @param body    {}
     * @return
     * @throws IOException
     */
    public Response customerRequest(String method_name, String endpoint, String body) throws IOException;

    /**
     * 自定义请求  如其他方法不能满足需求，可使用此方法
     * @param method_name   ex:POST PUT GET DELETE
     * @param endpoint    ex:/_cat/health
     * @return
     * @throws IOException
     */
    public Response customerRequest(String method_name, String endpoint) throws IOException;

    /**
     * 创建index
     * @param indexName
     * @return
     * @throws IOException
     */
    public String creatIndex(String indexName) throws IOException;

    /**
     * 创建index  在body可带需要的设置
     * @param indexName
     * @param body
     * @return
     * @throws IOException
     */
    public String creatIndex(String indexName, String body) throws IOException;

    /**
     * 自定义id 插入单条数据
     * @param index
     * @param id
     * @param body
     * @return
     * @throws IOException
     */
    public String insert(String index, String id, String body) throws IOException;

    /**
     * 插入单条数据
     * @param index
     * @param body
     * @return
     * @throws IOException
     */
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

    /**
     * 直接传完整的body   注：执行失败不会重试
     * @param index
     * @param body
     * @throws IOException
     */
    public String bulk(String index, String body) throws IOException;
    /**
     * 直接传完整的body   注：执行失败不会重试
     * @param body
     * @throws IOException
     */
    public String bulk(String body) throws IOException;
    /**
     * 批量执行   List中的每一条数据格式如(为完整的可执行命令 \n不可省略)："{ \"index\" : { \"_id\" : \"1234\" } }\n{ \"name\" : \"aiden7\" }\n";
     * @param bodyList
     * @throws IOException
     */
    public void bulk(ArrayList<String> bodyList ) throws IOException;
    /**
     * 批量执行   List中的每一条数据格式如(为完整的可执行命令 \n不可省略)："{ \"index\" : { \"_id\" : \"1234\" } }\n{ \"name\" : \"aiden7\" }\n";
     * @param bodyList
     * @param bulkFailRetry 失败重试次数   设置为-1时，会一直重试，直至全部成功
     * @throws IOException
     */
    public void bulk(ArrayList<String> bodyList,int bulkFailRetry ) throws IOException;
    /**
     * 批量执行   List中的每一条数据格式如(为完整的可执行命令 \n不可省略)："{ \"index\" : { \"_id\" : \"1234\" } }\n{ \"name\" : \"aiden7\" }\n";
     * @param index
     * @param bodyList
     * @param bulkFailRetry 失败重试次数   设置为-1时，会一直重试，直至全部成功，请确保语句正确
     * @throws IOException
     */
    public void bulk(String index, ArrayList<String> bodyList,int bulkFailRetry) throws IOException;
    /**
     * 批量执行   List中的每一条数据格式如(为完整的可执行命令 \n不可省略)："{ \"index\" : { \"_id\" : \"1234\" } }\n{ \"name\" : \"aiden7\" }\n";
     * @param bodyList
     * @param bulkFailRetry 失败重试次数   设置为-1时，会一直重试，直至全部成功
     * @param bulkFailRetryInterval  重试间隔
     * @throws IOException
     */
    public void bulk(ArrayList<String> bodyList, int bulkFailRetry, int bulkFailRetryInterval) throws IOException;
    /**
     * 批量执行   List中的每一条数据格式如(为完整的可执行命令 \n不可省略)："{ \"index\" : { \"_id\" : \"1234\" } }\n{ \"name\" : \"aiden7\" }\n";
     * @param index
     * @param bodyList
     * @param bulkFailRetry 失败重试次数   设置为-1时，会一直重试，直至全部成功
     * @param bulkFailRetryInterval  重试间隔
     * @throws IOException
     */
    public void bulk(String index, ArrayList<String> bodyList,int bulkFailRetry,int bulkFailRetryInterval) throws IOException;

    /**
     * 注意： 批量执行出现错误 此方法会一直重试  批量执行   List中的每一条数据格式如(为完整的可执行命令 \n不可省略)："{ \"index\" : { \"_id\" : \"1234\" } }\n{ \"name\" : \"aiden7\" }\n";
     * @param index
     * @param bodyList
     * @param bulkFailRetryInterval  重试间隔
     * @throws IOException
     */
    public void bulkAlwaysRetry(String index, ArrayList<String> bodyList,int bulkFailRetryInterval) throws IOException;

    /**
     *
     * @param index
     * @param bodyList
     * @return
     */
    public List bulkWithReturnFailedBody(String index, ArrayList<String> bodyList) throws IOException;

    /**
     *
     * @param bodyList
     * @return
     */
    public List bulkWithReturnFailedBody(ArrayList<String> bodyList) throws IOException;
    /**
     * ex : /{index_name}/_search?size=2
     * @param endpoint
     * @return
     * @throws IOException
     */
    public String searchWithEndpoint(String endpoint) throws IOException;

    /**
     *
     * @param body
     * @return
     * @throws IOException
     */
    public String searchWithBody(String body) throws IOException;

    /**
     *
     * @param index
     * @return
     * @throws IOException
     */
    public String search(String index) throws IOException;

    /**
     *
     * @param index
     * @param body
     * @return
     * @throws IOException
     */
    public String search(String index, String body) throws IOException;

    /**
     * index 是否存在
     * @param index
     * @return 200:true exist ; 404:false  not exist
     */
    public boolean indexExists(String index) throws IOException;

    /**
     * 获取index中的全部数据
     * @param index
     * @param pageSize
     * @return
     * @throws IOException
     */
    public List scrollAll(String index, int pageSize) throws IOException;

    /**
     * 获取index中的全部数据
     * @param index
     * @param pageSize
     * @param scroll   scroll_id保存的时间
     * @return
     * @throws IOException
     */
    public List scrollAll(String index, int pageSize, int scroll) throws IOException;


    public List scrollAllWithBody(String index, int scroll,String body) throws IOException;
    /**
     * 集群状态
     * @return
     * @throws IOException
     */
    public String clusterHealth() throws IOException;

    /**
     * 获得 template
     * @param templateName
     * @return
     * @throws IOException
     */
    public String getTemplate(String templateName) throws IOException;

    /**
     * 设置template
     * @param templateName
     * @param body
     * @return
     * @throws IOException
     */
    public String putTemplate(String templateName,String body) throws IOException;

    /**
     * 删除template
     * @param templateName
     * @return
     * @throws IOException
     */
    public String delTemplate(String templateName) throws IOException;

    /**
     * 获取index的mapping
     * @param indexName
     * @return
     * @throws IOException
     */
    public String getMapping(String indexName) throws IOException;

    /**
     * 给index设置mapping
     * @param indexName
     * @param body
     * @return
     * @throws IOException
     */
    public String putMapping(String indexName, String body) throws IOException;
}
