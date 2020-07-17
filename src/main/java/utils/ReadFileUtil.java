package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ReadFileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadFileUtil.class);

    public static InputStream getFis(String path) {
        FileInputStream fileInputStream = null;
        try {
            if (path.length() == 0 && !new File(path).exists()) {
                path = System.getProperty("user.dir") + "/conf/proxy.properties";
                LOGGER.info("家目录为：==================== " + System.getProperty("user.dir"));
                if (new File(path).exists()) {
                    LOGGER.info("传入的参数路径不存在，或未传入路径参数，使用，默认路径:");
                }
            }
            if (path.length() > 1 && !new File(path).exists()) {
                LOGGER.info("默认路径中路径不存在，或未传入路径参数，使用，Jar 包中的配置文件");
                InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("proxy.properties");
                return resourceAsStream;
            }
            LOGGER.info("代理配置文件路径为： " + path);
            fileInputStream = new FileInputStream(path);
            return fileInputStream;
        } catch (IOException e) {
            LOGGER.error("配置文件路径不存在，路径为：" + path);
        }
        return null;
    }
}
