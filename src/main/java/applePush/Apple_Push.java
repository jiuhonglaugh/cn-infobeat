package applePush;

import java.io.*;
import java.net.*;
import java.util.*;

import javapns.Push;
import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ReadFileUtil;


public class Apple_Push {

    private static final Logger LOGGER = LoggerFactory.getLogger(Apple_Push.class);
    private static final String PROXY_HTTP_USER = "proxy.http.user";
    private static final String PROXY_HTTP_PWD = "proxy.http.pwd";
    private static final String DEFUT_STRING = "";

    private static void test(String testUrl) {
        try {
            URL url = new URL(testUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();//此处可以用Stringbuffer等接收
            byte[] b = new byte[1024];
            int len = 0;
            while (true) {
                len = inputStream.read(b);
                if (len == -1) {
                    break;
                }
                byteArrayOutputStream.write(b, 0, len);
            }
            LOGGER.info(byteArrayOutputStream.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String confPath = "";
        if (args.length > 0) {
            confPath = args[0];
        }
        /**
         * 代理参数
         */
        InputStream fis = ReadFileUtil.getFis(confPath);
        Properties proper = System.getProperties();
        try {
            if (null != fis) {
                proper.load(fis);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * Apple push 只需要开通https或http其中一个代理即可
         * 为了不影响  内部服务正常使用所以只设置https代理
         */
        String proxUser = proper.getProperty(PROXY_HTTP_USER, "");
        String proxPwd = proper.getProperty(PROXY_HTTP_PWD, "");
        if (DEFUT_STRING.equals(proxPwd) || DEFUT_STRING.equals(proxUser)) {
            LOGGER.info("正向代理未配置验证： USER：" + proxUser + "            PWD:" + proxPwd);
        } else {
            LOGGER.info("正向代理配置的验证： USER：" + proxUser + "            PWD:" + proxPwd);
            Authenticator.setDefault(new MyAuthenticator(proxUser, proxPwd));
        }
        String url = proper.getProperty("test.url", "");
        if (!DEFUT_STRING.equals(url))
            test(url);
        /**
         * 推送参数
         */
        String cfPath = proper.getProperty("cf.path");
        if (!new File(cfPath).exists()) {
            cfPath = Thread.currentThread().getContextClassLoader().getResource("push123.p12").getPath();
            LOGGER.warn("设置的苹果证书路径不存在或未设置，设置的路径为 =========：" + cfPath);
        }
        LOGGER.info("加载的苹果证书路径为 =========：" + cfPath);
        LOGGER.info("http设置的代理地址为 =========：" + System.getProperty("http.proxyHost"));
        LOGGER.info("http设置的代理端口为 =========：" + System.getProperty("http.proxyPort"));
        LOGGER.info("https设置的代理地址为=========：" + System.getProperty("https.proxyHost"));
        LOGGER.info("https设置的代理端口为=========：" + System.getProperty("https.proxyPort"));

        boolean sendCount = true;
        String token = "25520d8db2160123c60e3317ff2eb59dfe0efa40fd3b275e3696c3ef2f879f16";

        List<String> tokens = new ArrayList<String>();
        tokens.add(token);
        String password = "123";// 证书密码
        String message = "{'aps':{'alert':'iphone推送测试 www.baidu.com'}}";
        Integer count = 1;
        sendpush(cfPath, password, tokens, message, count, sendCount);
    }

    /***************************************************************************
     * 测试推送服务器地址：gateway.sandbox.push.apple.com /2195
     * 产品推送服务器地址：gateway.push.apple.com / 2195 需要javaPNS_2.2.jar包
     **************************************************************************/
    /**
     * 这是一个比较简单的推送方法， apple的推送方法
     *
     * @param tokens    iphone手机获取的token
     * @param path      这里是一个.p12格式的文件路径，需要去apple官网申请一个
     * @param password  p12的密码 此处注意导出的证书密码不能为空因为空密码会报错
     * @param message   推送消息的内容
     * @param count     应用图标上小红圈上的数值
     * @param sendCount 单发还是群发 true：单发 false：群发
     */
    private static void sendpush(String path, String password, List<String> tokens
            , String message, Integer count, boolean sendCount) {
        try {
            if (tokens.size() > 1)
                sendCount = false;
            PushNotificationPayload payLoad = PushNotificationPayload
                    .fromJSON(message);
            payLoad.addAlert("iphone推送测试 www.baidu.com"); // 消息内容
            payLoad.addBadge(count); // iphone应用图标上小红圈上的数值
            payLoad.addSound("default"); // 铃音 默认
            PushNotificationManager pushManager = new PushNotificationManager();
            // true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
            pushManager.initializeConnection(
                    new AppleNotificationServerBasicImpl(path, password, false));//true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
            List<PushedNotification> notifications = new ArrayList<PushedNotification>();
            // 发送push消息
            if (sendCount) {
                LOGGER.info("------------------apple 推送 单条推送------------------");
                Device device = new BasicDevice();
                device.setToken(tokens.get(0));
                PushedNotification notification = pushManager.sendNotification(
                        device, payLoad, true);
                notifications.add(notification);
            } else {
                LOGGER.info("------------------apple 推送 多条推送------------------");
                List<Device> device = new ArrayList<Device>();
                for (String token : tokens) {
                    device.add(new BasicDevice(token));
                }
                notifications = pushManager.sendNotifications(payLoad, device);
            }
            List<PushedNotification> fail = PushedNotification
                    .findFailedNotifications(notifications);
            List<PushedNotification> success = PushedNotification
                    .findSuccessfulNotifications(notifications);
            int failed = fail.size();
            int successful = success.size();
            if (successful > 0 && failed == 0) {
                LOGGER.info("成功条数 = = = = = = = = = = = ( " + success.size() + " )");
            } else if (successful == 0 && failed > 0) {
                LOGGER.warn("失败条数 = = = = = = = = = = = ( " + fail.size() + " )");
            } else if (successful == 0 && failed == 0) {
                LOGGER.warn("No notifications could be sent, probably because of a critical error");
            } else {
                LOGGER.info("成功条数 = = = = = = = = = = = ( " + success.size() + " )");
                LOGGER.warn("失败条数 = = = = = = = = = = = ( " + fail.size() + " )");
            }
//			handleFeedback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path     证书路径
     * @param password 证书密码
     */
    public void handleFeedback(String path, String password) {
        long startTm = System.currentTimeMillis();
        List<Device> feedBacklist = new ArrayList<Device>();
        try {
            feedBacklist = Push.feedback(path, password, true);
            System.out.println("**********getFeedback:" + feedBacklist.size() + "****************cost:[" + (System.currentTimeMillis() - startTm) + "]ms");
            Map<Object, Object> map = new HashMap<Object, Object>();
            map.put("devList", feedBacklist);
            startTm = System.currentTimeMillis();
            if (feedBacklist != null && feedBacklist.size() > 0) {
//				pushDao.updateDeviceStatus(feedBacklist);
                for (int i = 0; i < feedBacklist.size(); i++) {
                    Device device = feedBacklist.get(i);
                    System.out.println("======" + device.getToken());
                }
            } else {
                System.out.println("*********feedBacklist is null or this size is zero");
            }
            System.out.println("**********update Device:" + feedBacklist.size() + "****************cost:[" + (System.currentTimeMillis() - startTm) + "]s");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }

}
