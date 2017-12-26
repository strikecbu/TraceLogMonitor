package com.citi;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by VALLA on 2017/12/26.
 */
public class App {
    public static void main(String[] args) throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("config.properties");
        Properties prop = new Properties();
        prop.load(resource.openStream());
    }

}
