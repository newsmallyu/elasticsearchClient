package com.newegg.method;

import java.io.IOException;
import java.util.Map;

public interface ESMethod {


    public String insert(String index, String id, String body) throws IOException;
    public String insert(String index, String body) throws IOException;


    //public String insert(String index, String id, Map<String, Object> body) throws IOException;
    //public String insert(String index, Map<String, Object> body) throws IOException;

    public String bulk(String body) throws IOException;
    public String bulk(String index, String body) throws IOException;

    public String search(String index) throws IOException;
    public String search(String index, String body) throws IOException;
   // public String search(String index, Map<String, Object> body);

    /**
     *
     * @param index
     * @return 200:exist ; 404:do not exist
     */
    public boolean indexExists(String index) throws IOException;

    public String scrollAll(String index, String body);

    public String clusterHealth() throws IOException;
}
