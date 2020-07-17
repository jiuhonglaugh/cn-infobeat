package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private static Properties properties;

    private String filePath;
    private InputStream in;

    public PropertiesUtil(String filePath) {
        this.filePath = filePath;
    }

    private boolean initPro() {

        try {
            if (properties == null) {
                properties = new Properties();
            }
            File file = new File(filePath);
            in = new FileInputStream(file);
            properties.load(in);
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String propValue(String key) {
        if (properties == null) {
            initPro();
        }
        return properties.getProperty(key);
    }
}
