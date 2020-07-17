import org.apache.commons.net.util.Base64;

import java.io.IOException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * FileName: RedisECRYPTION
 * Author:   MAIBENBEN
 * Date:     2020/5/9 15:15
 * History:
 * <author>          <time>          <version>          <desc>
 */
public class RedisEcryption {
    private static String keys = "cyl!@#$%";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("请输入Redis明文密码：==============》》》》");
            System.exit(1);
        }

        System.out.println("明文密码为  ： " + args[0]);
        System.err.println("加密密码为  ： " + encrypt(args[0]));
    }

    /**
     * 自定义 key 加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, String key) throws Exception {
        byte[] bt = encrypt(data.getBytes(), key.getBytes());
        String strs = Base64.encodeBase64String(bt);
        return strs.trim();
    }

    /**
     * 加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        byte[] bt = encrypt(data.getBytes(), keys.getBytes());
        String strs = Base64.encodeBase64String(bt);
        return strs.trim();
    }

    /**
     * 自定义key 解密
     *
     * @param data
     * @param key
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws IOException, Exception {
        if (data == null) {
            return null;
        } else {
            byte[] buf = Base64.decodeBase64(data);
            byte[] bt = decrypt(buf, key.getBytes());
            return new String(bt);
        }
    }

    /**
     * 解密
     *
     * @param data
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data) throws IOException, Exception {
        if (data == null) {
            return null;
        } else {
            byte[] buf = Base64.decodeBase64(data);
            byte[] bt = decrypt(buf, keys.getBytes());
            return new String(bt);
        }
    }

    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(1, securekey, sr);
        return cipher.doFinal(data);
    }

    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(2, securekey, sr);
        return cipher.doFinal(data);
    }

    private String byte2HexStr(byte[] buf) {
        StringBuilder sb = new StringBuilder();
        byte[] var3 = buf;
        int var4 = buf.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            byte aBuf = var3[var5];
            String hex = Integer.toHexString(aBuf & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            sb.append(hex.toUpperCase());
        }

        return sb.toString();
    }

    private byte[] hexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];

            for (int i = 0; i < hexStr.length() / 2; ++i) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }

            return result;
        }
    }
}
