/**
 * FileName: SSLServer
 * Author:   MAIBENBEN
 * Date:     2020/6/8 18:11
 * History:
 * <author>          <time>          <version>          <desc>
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyStore;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class SSLClient {
//    private static String CLIENT_KEY_STORE = "/D:/Projects/J2EE/JDK/src/ssl/keystore/client_ks";
//    private static String CLIENT_KEY_STORE_PASSWORD = "456456";
//    private static String host = "172.26.1.214";
    private static String host = "104.21.53.68";
    private static int port = 8100;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Socket createNonAuthenticationSocket()throws Exception{
//        System.setProperty("javax.net.ssl.trustStore", CLIENT_KEY_STORE);
        SocketFactory sf = SSLSocketFactory.getDefault();
        Socket s = sf.createSocket(host, port);
        return s;
    }

    private Socket createAuthenticationSocket() throws Exception{
//        System.setProperty("javax.net.ssl.trustStore", CLIENT_KEY_STORE);
        SSLContext context = SSLContext.getInstance("SSLv3");
//        KeyStore ks = KeyStore.getInstance("jceks");
//        ks.load(new FileInputStream(CLIENT_KEY_STORE), null);
        KeyManagerFactory kf = KeyManagerFactory.getInstance("SunX509");
//        kf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());
        context.init(kf.getKeyManagers(), null, null);

        SocketFactory factory = context.getSocketFactory();
        Socket s = factory.createSocket("172.26.1.214", 8100);
        return s;
    }

    private void connect()throws Exception{
        Socket s = createNonAuthenticationSocket();
//     Socket s = createAuthenticationSocket();

        PrintWriter writer = new PrintWriter(s.getOutputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        writer.println("hello");
        writer.flush();
        logger.info(reader.readLine());
        s.close();
    }


    public static void main(String[] args) throws Exception {
        new SSLClient().connect();
    }
}