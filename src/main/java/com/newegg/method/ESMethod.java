package com.newegg.method;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ESMethod {


    public String insert(String index, String id, String body) throws IOException;
    public String insert(String index, String body) throws IOException;

    /**
     * 修改数据
     * @param index
     * @param id
     * @param body 原来的数据将被body覆盖
     * @return
     * @throws IOException
     */
    public String update(String index, String id, String body) throws IOException;

    public String bulk(String body) throws IOException;

    /**
     * 批量执行
     * @param index
     * @param body
     * @return
     * @throws IOException
     */
    public String bulk(String index, String body) throws IOException;

    public String searchWithBody(String body) throws IOException;
    public String search(String index) throws IOException;
    public String search(String index, String body) throws IOException;

    /**
     *
     * @param index
     * @return 200:exist ; 404:do not exist
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
}
