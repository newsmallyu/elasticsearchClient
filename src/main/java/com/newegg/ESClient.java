package com.newegg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newegg.common.Constant;
import com.newegg.method.ESMethod;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ESClient implements Closeable, ESMethod {
    private static final Logger LOGGER = LoggerFactory.getLogger(ESClient.class);

    private RestClientBuilder builder;
    private RestHighLevelClient restHighLevelClient;
    private RestClient lowLevelClient;
    private ObjectMapper mapper = new ObjectMapper();

    private ESClient(RestClientBuilder builder){
        this.builder = builder;
        this.restHighLevelClient =  new RestHighLevelClient(builder);
        this.lowLevelClient = this.restHighLevelClient.getLowLevelClient();
    }

    public static  Builder builder() {
        return new Builder();
    }

    public RestClientBuilder getBuilder() {
        return builder;
    }
    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }
    public RestClient getLowLevelClient() {
        return lowLevelClient;
    }

    public static void main(String[] args) throws IOException {
        ESClient esClient = ESClient.builder()
                .withHost("10.16.236.126:9200", "10.16.236.127:9200", "10.16.236.128:9200")
                .build();
        String json = "{\n" +
                "  \"name\":\"aiden2\",\n" +
                "  \"age\":\"19\"\n" +
                "}";
        HashMap map = new HashMap();
        map.put("name", "aiden4");
        map.put("age", 18);
        //boolean s = esClient.indexExists("test_index");
        //String aiden_devtest9 = esClient.insert("aiden_devtest9","adas", json);
       // String aiden_devtest9 = esClient.insert("aiden_devtest9", "123sadasd",map);
        /*String index = "aiden_devtest9";
        String body1 = "{ \"index\" : { \"_index\" : \"aiden_devtest9\", \"_id\" : \"123\" } }\n" +
                "{ \"name\" : \"aiden5\" }\n" +
                "{ \"index\" : { \"_index\" : \"aiden_devtest9\", \"_id\" : \"456\" } }\n" +
                "{ \"name\" : \"aiden6\" }\n";

        String body2 = "{ \"index\" : { \"_id\" : \"1234\" } }\n" +
                "{ \"name\" : \"aiden7\" }\n" +
                "{ \"index\" : {\"_id\" : \"4567\" } }\n" +
                "{ \"name\" : \"aiden8\" }\n";*/
        //String bulk = esClient.bulk(index,body2);
        String body3 = "{\n" +
                "    \"size\": 10\n" +
                "}";
        String aiden_devtest9 = esClient.search("aiden_devtest9", body3);
        esClient.scrollAll("aiden_devtest9",20);
        esClient.close();
        System.out.println(aiden_devtest9);
        esClient.close();
    }



    @Override
    public String clusterHealth() throws IOException {
        Request request = new Request(HttpGet.METHOD_NAME, "_cluster/health");
        Response response = lowLevelClient.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public boolean indexExists(String index) throws IOException {
        String endpoint = Constant.SLASH + index;
        Request request = new Request(HttpHead.METHOD_NAME, endpoint);
        Response response = lowLevelClient.performRequest(request);
        return response.getStatusLine().getStatusCode() == 200 ? true : false;
    }


    @Override
    public String insert(String index, String id, String body) throws IOException {
        String endpoint = null;
        if (id != null && !"".equals(id)) {
            endpoint = Constant.SLASH + index + Constant.SLASH + "_doc" + Constant.SLASH + id;
        }else {
            endpoint = Constant.SLASH + index + Constant.SLASH + "_doc";
        }
        Request request = new Request(HttpPost.METHOD_NAME, endpoint);
        request.setJsonEntity(body);
        Response response = lowLevelClient.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public String insert(String index, String body) throws IOException {
        String result = insert(index, null, body);
        return result;
    }



    @Override
    public String bulk(String body) throws IOException {
        String result = bulk(null, body);
        return result;
    }

    @Override
    public String bulk(String index, String body) throws IOException {
        String endpoint = null;
        if (index != null && !"".equals(index)){
            endpoint = Constant.SLASH+index+Constant.SLASH+"_bulk";
        }else {
            endpoint = "/_bulk";
        }
        Request request = new Request(HttpPost.METHOD_NAME,endpoint);
        request.setJsonEntity(body);
        Response response = lowLevelClient.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public String search(String index) throws IOException {
        String result = search(index, null);
        return result;
    }

    @Override
    public String searchWithBody(String body) throws IOException {
        String result = search(null, body);
        return result;
    }

    @Override
    public String search(String index, String body) throws IOException {
        String endpoint = null;
        if (index != null && !"".equals(index) ){
            endpoint = Constant.SLASH + index + Constant.SLASH + "_search";
        }else {
            endpoint = Constant.SLASH + "_search";
        }
        Request request = new Request(HttpGet.METHOD_NAME, endpoint);
        if (body != null && !"".equals(body)){
            request.setJsonEntity(body);
        }
        Response response = lowLevelClient.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }


    /**
     *
     * @param index   索引名
     * @param size   每次scroll返回size的大小
     * @return
     * @throws IOException
     */
    @Override
    public List<Map> scrollAll(String index, int size) throws IOException {
        List<Map> resultList = new ArrayList();
        String endpoint =Constant.SLASH+index+"/_search?scroll=10m";
        Request request = new Request(HttpPost.METHOD_NAME, endpoint);
        String body = "{\n" +
                "  \"size\":"+size+"\n" +
                "}";
        request.setJsonEntity(body);
        Response response = lowLevelClient.performRequest(request);
        Map<String,Object> map = mapper.readValue(EntityUtils.toString(response.getEntity()), Map.class);
        String scrollId = (String) map.get("_scroll_id");
        HashMap hits = (HashMap) map.get("hits");
        ArrayList firstScollList = (ArrayList) hits.get("hits");
        resultList.addAll(firstScollList);
        while (firstScollList.size()>0){
            List scrollList = constractScrollRequest(scrollId);
            if (scrollList.size()>0){
                resultList.addAll(scrollList);
            }else {
                deleteScrollId(scrollId);
                break;
            }
        }
        return resultList;
    }


    public List<Map> constractScrollRequest(String scrollId) throws IOException {
        String endpoint = "/_search/scroll";
        String body = "{\n" +
                "  \n" +
                "    \"scroll\" : \"10m\",\n" +
                "    \"scroll_id\" : \""+ scrollId+"\"}";
        Request request = new Request(HttpPost.METHOD_NAME, endpoint);
        request.setJsonEntity(body);
        Response response = lowLevelClient.performRequest(request);
        Map<String,Object> map = mapper.readValue(EntityUtils.toString(response.getEntity()), Map.class);
        HashMap hits = (HashMap) map.get("hits");
        ArrayList arrayList = (ArrayList) hits.get("hits");
        return arrayList;
    }

    public void deleteScrollId(String scrollId) throws IOException {
        String endpoint = "/_search/scroll";
        String body = "{\n" +
                "    \"scroll_id\" : \"" + scrollId + "\"\n" +
                "}";
        Request request = new Request(HttpDelete.METHOD_NAME, endpoint);
        request.setJsonEntity(body);
        lowLevelClient.performRequest(request);
    }

    @Override
    public void close() throws IOException {
        this.restHighLevelClient.close();
    }
    public static class Builder{
        private HttpHost[] hosts;
        public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 1000;
        public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 30000;
        public static final int DEFAULT_MAX_CONN_PER_ROUTE = 10;
        public static final int DEFAULT_MAX_CONN_TOTAL = 30;
        private int maxConnTotal = DEFAULT_MAX_CONN_TOTAL;
        private int maxConnPerRoute = DEFAULT_MAX_CONN_PER_ROUTE;
        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT_MILLIS;
        private int socketTimeout = DEFAULT_SOCKET_TIMEOUT_MILLIS;
        private int connectionRequestTimeout = DEFAULT_CONNECT_TIMEOUT_MILLIS;
        private static int DefaultMaxIoThreadCount = -1;
        private int ioThreadCount;
        private boolean soKeepAlive = false;
        private NodeSelector nodeSelector = NodeSelector.ANY;

        private Builder() {

        }

        public static int getDefaultMaxIoThreadCount() {
            return DefaultMaxIoThreadCount > 0 ? DefaultMaxIoThreadCount : Runtime.getRuntime().availableProcessors();
        }

        public Builder withHost(String... hosts){
            this.hosts = new HttpHost[hosts.length];
            for(int i = 0 ;i < hosts.length; i++)  {
                String host  = hosts[i];
                if(hosts[i].contains(":")) {
                    String[] hosrArr = host.split(":");
                    this.hosts[i] = new HttpHost(hosrArr[0], Integer.parseInt(hosrArr[1]), null);
                }else {
                    this.hosts[i] = new HttpHost(host, 8200, null);
                }
            }
            return this;
        }

        public Builder withConnectTimeout(int connectTimeout){
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder withSocketTimeout(int socketTimeout){
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder withConnectionRequestTimeout(int connectionRequestTimeout){
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        public Builder withIoThreadCount(int ioThreadCount){
            this.ioThreadCount = ioThreadCount;
            return this;
        }

        public Builder withSoKeepAlive(boolean soKeepAlive){
            this.soKeepAlive = soKeepAlive;
            return this;
        }

        public Builder withMaxConnTotal(int maxConnTotal){
            this.maxConnTotal = maxConnTotal;
            return this;
        }

        public Builder withMaxConnPerRoute(int maxConnPerRoute){
            this.maxConnPerRoute = maxConnPerRoute;
            return this;
        }

        public Builder withNodeSelector(NodeSelector nodeSelector) {
            this.nodeSelector = nodeSelector;
            return this;
        }


        public ESClient build(){
            Builder thisBuilder = this;
            ioThreadCount = ioThreadCount <= 0? getDefaultMaxIoThreadCount():ioThreadCount;
            RestClientBuilder builder = RestClient.builder(hosts);
            builder.setNodeSelector(this.nodeSelector)
                    .setRequestConfigCallback(requestConfigBuilder-> {
                        return   requestConfigBuilder
                                .setConnectTimeout(thisBuilder.connectTimeout)
                                .setSocketTimeout(thisBuilder.socketTimeout)
                                .setConnectionRequestTimeout(thisBuilder.connectionRequestTimeout)
                                .setRedirectsEnabled(true);
                    })
                    .setHttpClientConfigCallback(httpClientBuilder -> {
                        return httpClientBuilder
                                .setDefaultIOReactorConfig(
                                        IOReactorConfig.custom()
                                                .setIoThreadCount(thisBuilder.ioThreadCount)
                                                .setConnectTimeout(thisBuilder.connectTimeout)
                                                .setSoTimeout(thisBuilder.socketTimeout)
                                                .setSoKeepAlive(thisBuilder.soKeepAlive)
                                                .build())
                                .setMaxConnPerRoute(thisBuilder.maxConnPerRoute)
                                .setMaxConnTotal(thisBuilder.maxConnTotal);
                    });

            return new ESClient(builder);
        }

    }

}
