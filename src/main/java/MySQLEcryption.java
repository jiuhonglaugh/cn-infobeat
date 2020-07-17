import com.alibaba.fastjson.JSONObject;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jetbrains.annotations.NotNull;

/**
 * FileName: MainActivity
 * Author:   MAIBENBEN
 * Date:     2019/11/1 16:48
 * History:
 * <author>          <time>          <version>          <desc>
 */
public class MySQLEcryption {
    public static void main(@NotNull String[] args) {

        if (args.length != 2) {
            System.out.println("请输入明文的  MysqlUser  和  MysqlPwd  ");
            System.out.println("格式为： java -jar xxxx.jar user pwd");
            System.exit(1);
        }
        String algorithm = "PBEWithMD5AndDES";
        String password = "EWRREWRERWECCCXC";
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        /*配置文件中配置如下的算法*/
        encryptor.setAlgorithm(algorithm);
        /*配置文件中配置的password*/
        encryptor.setPassword(password);
        String user = encryptor.encrypt(args[0]);
        String pwd = encryptor.encrypt(args[1]);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", user);
        jsonObject.put("algorithm", algorithm);
        jsonObject.put("pwd", pwd);
        jsonObject.put("password", password);
        System.out.println(jsonObject.toJSONString());
    }
}
