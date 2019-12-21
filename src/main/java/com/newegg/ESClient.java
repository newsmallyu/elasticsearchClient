package com.newegg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newegg.common.Constant;
import com.newegg.method.ESMethod;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
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

    private ESClient(RestClientBuilder builder) {
        this.builder = builder;
        this.restHighLevelClient = new RestHighLevelClient(builder);
        this.lowLevelClient = this.restHighLevelClient.getLowLevelClient();
    }

    public static Builder builder() {
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


    @Override
    public String clusterHealth() throws IOException {
        Request request = new Request(HttpGet.METHOD_NAME, "_cluster/health");
        Response response = lowLevelClient.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public String getTemplate(String templateName) throws IOException {
        String endpoint = Constant.TEMPLATE_STR + Constant.SLASH + templateName;
        Response response = customerRequest(HttpGet.METHOD_NAME, endpoint);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public String putTemplate(String templateName, String body) throws IOException {
        String endpoint = Constant.TEMPLATE_STR + Constant.SLASH + templateName;
        Response response = customerRequest(HttpPut.METHOD_NAME, endpoint, body);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public String delTemplate(String templateName) throws IOException {
        String endpoint = Constant.TEMPLATE_STR + Constant.SLASH + templateName;
        Response response = customerRequest(HttpDelete.METHOD_NAME, endpoint);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public String getMapping(String indexName) throws IOException {
        String endpoint = Constant.SLASH + indexName + Constant.MAPPING_STR;
        Response response = customerRequest(HttpGet.METHOD_NAME, endpoint);
        return EntityUtils.toString(response.getEntity());
    }

    @Override
    public String putMapping(String indexName, String body) throws IOException {
        String endpoint = Constant.SLASH + indexName + Constant.MAPPING_STR;
        Response response = customerRequest(HttpPut.METHOD_NAME, endpoint, body);
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
    public String creatIndex(String indexName) throws IOException {
        String result = creatIndex(indexName, null);
        return result;
    }

    @Override
    public String creatIndex(String indexName, String body) throws IOException {
        String endpoint = Constant.SLASH + indexName;
        if (indexExists(indexName)) {
            return "Index Exists";
        }else {
            Response response = customerRequest(HttpPut.METHOD_NAME, endpoint, body);
            return EntityUtils.toString(response.getEntity());
        }
    }

    @Override
    public String insert(String index, String id, String body) throws IOException {
        String endpoint = null;
        if (id != null && !"".equals(id)) {
            endpoint = Constant.SLASH + index + Constant.SLASH + Constant.DOC_TYPE + Constant.SLASH + id;
        } else {
            endpoint = Constant.SLASH + index + Constant.SLASH + Constant.DOC_TYPE;
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
    public String update(String index, String id, String body) throws IOException {
        String endpoint = index + Constant.SLASH + Constant.DOC_TYPE + Constant.SLASH + id;
        Request request = new Request(HttpPut.METHOD_NAME, endpoint);
        request.setJsonEntity(body);
        Response response = lowLevelClient.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }


    @Override
    public void bulk(ArrayList<String> bodyList) throws IOException {
         bulk(null, bodyList,Constant.DEFAULT_BULK_FAIL_RETRY);
    }

    @Override
    public void bulk(ArrayList<String> bodyList, int bulkFailRetry) throws IOException {
         bulk(null, bodyList,bulkFailRetry);
    }

    @Override
    public void bulk(String index, ArrayList<String> bodyList, int bulkFailRetry) throws IOException {
        bulk(index, bodyList, bulkFailRetry,Constant.DEFAULT_BULK_FAIL_RETRY_INTERVAL);
    }

    @Override
    public void bulk(String index, ArrayList<String> bodyList, int bulkFailRetry ,int bulkFailRetryInterval) throws IOException {
        String endpoint = null;
        ArrayList failedList = new ArrayList();
        if (index != null && !"".equals(index)) {
            endpoint = Constant.SLASH + index + Constant.SLASH + "_bulk";
        } else {
            endpoint = "/_bulk";
        }
        Request request = new Request(HttpPost.METHOD_NAME, endpoint);
        String body = appendString(bodyList);
        request.setJsonEntity(body);
        Response response = lowLevelClient.performRequest(request);
        Map<String, Object> map = mapper.readValue(EntityUtils.toString(response.getEntity()), Map.class);
        if (map.get("errors") != null && (boolean)map.get("errors")) {
            ArrayList<HashMap> items = (ArrayList) map.get("items");
            for (int i = 0; i < items.size(); i++) {
                HashMap item = items.get(i);
                HashMap itemEach = (HashMap) item.get("index");
                if ( itemEach.get("error")!= null) {
                    failedList.add(bodyList.get(i));
                }
            }
        }
        if (bulkFailRetry > 0 && failedList.size() > 0) {
            try {
                Thread.sleep(bulkFailRetryInterval);
            } catch (InterruptedException e) {
                LOGGER.error("bulk sleep Error");
            }
            LOGGER.warn("Bulk failedList will be retry");
            bulk(index, failedList, bulkFailRetry - 1,bulkFailRetryInterval);
        }
        if (bulkFailRetry == -1 && failedList.size()>0){
            try {
                Thread.sleep(bulkFailRetryInterval);
            } catch (InterruptedException e) {
                LOGGER.error("bulk sleep Error");
            }
            LOGGER.warn("Bulk failedList will be retry");
            bulk(index, failedList, -1,bulkFailRetryInterval);
        }
        if (bulkFailRetry == 0 && failedList.size() > 0){
            String failedString = appendString(bodyList);
            LOGGER.error("bulk Failed:{}",failedString);
        }
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
        if (index != null && !"".equals(index)) {
            endpoint = Constant.SLASH + index + Constant.SLASH + "_search";
        } else {
            endpoint = Constant.SLASH + "_search";
        }
        Request request = new Request(HttpGet.METHOD_NAME, endpoint);
        if (body != null && !"".equals(body)) {
            request.setJsonEntity(body);
        }
        Response response = lowLevelClient.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }


    /**
     * @param index 索引名
     * @param pageSize  每次scroll返回size的大小
     * @return
     * @throws IOException
     */
    @Override
    public List<Map> scrollAll(String index, int pageSize) throws IOException {
        List<Map> resultList = new ArrayList();
        String endpoint = Constant.SLASH + index + "/_search?scroll=10m";
        Request request = new Request(HttpPost.METHOD_NAME, endpoint);
        String body = "{\n" +
                "  \"size\":" + pageSize + "\n" +
                "}";
        request.setJsonEntity(body);
        Response response = lowLevelClient.performRequest(request);
        Map<String, Object> map = mapper.readValue(EntityUtils.toString(response.getEntity()), Map.class);
        String scrollId = (String) map.get("_scroll_id");
        HashMap hits = (HashMap) map.get("hits");
        ArrayList firstScrollList = (ArrayList) hits.get("hits");
        resultList.addAll(firstScrollList);
        while (firstScrollList.size() > 0) {
            List scrollList = scrollByScrollId(scrollId);
            if (scrollList.size() > 0) {
                resultList.addAll(scrollList);
            } else {
                deleteScrollId(scrollId);
                break;
            }
        }
        return resultList;
    }

    /**
     * 通过scrollId查询结果
     *
     * @param scrollId
     * @return
     * @throws IOException
     */
    public List<Map> scrollByScrollId(String scrollId) throws IOException {
        String endpoint = "/_search/scroll";
        String body = "{\n" +
                "  \n" +
                "    \"scroll\" : \"10m\",\n" +
                "    \"scroll_id\" : \"" + scrollId + "\"}";
        Request request = new Request(HttpPost.METHOD_NAME, endpoint);
        request.setJsonEntity(body);
        Response response = lowLevelClient.performRequest(request);
        Map<String, Object> map = mapper.readValue(EntityUtils.toString(response.getEntity()), Map.class);
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
    public boolean existsTemplate(String templateName) throws IOException {
        String endpoint = Constant.TEMPLATE_STR + Constant.SLASH + templateName;
        Request request = new Request(HttpHead.METHOD_NAME, endpoint);
        Response response = lowLevelClient.performRequest(request);
        return response.getStatusLine().getStatusCode() == 200 ? true : false;
    }

    @Override
    public void close() throws IOException {
        this.restHighLevelClient.close();
    }

    public String appendString(ArrayList list){
        StringBuilder stringBuilder = new StringBuilder();
        list.forEach((bodyString) -> {
            stringBuilder.append(bodyString);
        });
        return stringBuilder.toString();
    }

    public Response customerRequest(String method_name,String endpoint , String body) throws IOException {
        Request request = new Request(method_name, endpoint);
        if (body != null && !"".equals(body)) {
            request.setJsonEntity(body);
        }
        Response response = lowLevelClient.performRequest(request);
        return response;
    }
    public Response customerRequest(String method_name,String endpoint) throws IOException {
        Response response = customerRequest(method_name, endpoint, null);
        return response;
    }

    public static class Builder {
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
        private boolean soKeepAlive = false;

        private Builder() {
        }

        public Builder withHost(String... hosts) {
            this.hosts = new HttpHost[hosts.length];
            for (int i = 0; i < hosts.length; i++) {
                String host = hosts[i];
                if (hosts[i].contains(":")) {
                    String[] hosrArr = host.split(":");
                    this.hosts[i] = new HttpHost(hosrArr[0], Integer.parseInt(hosrArr[1]), null);
                } else {
                    this.hosts[i] = new HttpHost(host, 8200, null);
                }
            }
            return this;
        }

        public Builder withConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder withSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder withConnectionRequestTimeout(int connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }



        public Builder withSoKeepAlive(boolean soKeepAlive) {
            this.soKeepAlive = soKeepAlive;
            return this;
        }

        public Builder withMaxConnTotal(int maxConnTotal) {
            this.maxConnTotal = maxConnTotal;
            return this;
        }

        public Builder withMaxConnPerRoute(int maxConnPerRoute) {
            this.maxConnPerRoute = maxConnPerRoute;
            return this;
        }




        public ESClient build() {
            Builder thisBuilder = this;
            RestClientBuilder builder = RestClient.builder(hosts);
            builder.setRequestConfigCallback(requestConfigBuilder -> {
                        return requestConfigBuilder
                                .setConnectTimeout(thisBuilder.connectTimeout)
                                .setSocketTimeout(thisBuilder.socketTimeout)
                                .setConnectionRequestTimeout(thisBuilder.connectionRequestTimeout)
                                .setRedirectsEnabled(true);
                    })
                    .setHttpClientConfigCallback(httpClientBuilder -> {
                        return httpClientBuilder
                                .setDefaultIOReactorConfig(
                                        IOReactorConfig.custom()
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
