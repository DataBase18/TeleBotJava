package org.codeInge.utilities;

import javax.imageio.IIOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private Properties prop;


    public Config(String pathProperties) throws IOException {
        prop = new Properties();
        FileInputStream fis = new FileInputStream(pathProperties);
        prop.load(fis);
        fis.close();
    }

    public String getProperty(String key) {
        if (prop.containsKey(key) && prop.get(key) != null) {
            return prop.getProperty(key);
        }
        return "";
    }
}
