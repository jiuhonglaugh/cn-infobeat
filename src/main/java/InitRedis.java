import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import utils.PropertiesUtil;
import utils.redis.RedisUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class InitRedis {

    public static void conditionFiled(String filePath) {


        PropertiesUtil propertiesUtil = new PropertiesUtil(filePath);

        String address = propertiesUtil.propValue("mysql.host");
        String dBName = propertiesUtil.propValue("mysql.dBName");
        String password = propertiesUtil.propValue("mysql.password");
        String userName = propertiesUtil.propValue("mysql.userName");
        String tables = propertiesUtil.propValue("mysql.tables");
        String redisHost = propertiesUtil.propValue("redis.host");
        String auth = propertiesUtil.propValue("redis.auth");
        RedisUtil redisUtil = new RedisUtil(redisHost, auth);

        try {
            System.out.println("mysql user: " + userName + " pwd: " + password + " host: " + address + " dbName: " + dBName);
            System.out.println("redis host: " + redisHost + " auth: " + auth);
            String driver = "com.mysql.jdbc.Driver";
            String url = "jdbc:mysql://" + address + "/" + dBName
                    + "?characterEncoding=utf8&serverTimezone=GMT&useSSL=false";
            Class.forName(driver);
            Connection c = DriverManager.getConnection(url, userName, password);
            if (!c.isClosed()) {
                System.out.println("Succeeded connecting to the Database!");
            }
            Statement statement = c.createStatement();
            for (String table : tables.split(",")) {
                String sql = "select * from " + table;
                ResultSet rs = statement.executeQuery(sql);
                while (rs.next()) {
                    JsonObject jsonObject = new JsonObject();
                    if ("action_unique_fields".equals(table)) {
                        jsonObject.addProperty("id", rs.getString("id"));
                        jsonObject.addProperty("system_name",
                                rs.getString("system_name"));
                        jsonObject.addProperty("client_type",
                                rs.getString("client_type"));
                        jsonObject.addProperty("update_time",
                                rs.getString("update_time"));
                        if ((rs.getString("create_time") != null)
                                && (!"".equals(rs.getString("create_time")))) {
                            jsonObject.addProperty("create_time",
                                    rs.getString("create_time"));
                        } else {
                            jsonObject.addProperty("create_time", "");
                        }
                        if ((rs.getString("field_url") != null)
                                && (!"".equals(rs.getString("field_url")))) {
                            jsonObject.addProperty("field_url",
                                    rs.getString("field_url"));
                        } else {
                            jsonObject.addProperty("field_url", "");
                        }
                        if ((rs.getString("unique_fields") != null)
                                && (!"".equals(rs.getString("unique_fields")))) {
                            jsonObject.addProperty("unique_fields",
                                    rs.getString("unique_fields"));
                        } else {
                            jsonObject.addProperty("unique_fields", "");
                        }
                        if (redisUtil.isCluster) {
                            JedisCluster jedisCluster = (JedisCluster) redisUtil.getjedis();
                            jedisCluster.hset("term_action_win", rs.getString("id"), jsonObject.toString());

                        } else {
                            Jedis jedis = (Jedis) redisUtil.getjedis();
                            jedis.hset("term_action_win", rs.getString("id"), jsonObject.toString());
                        }

                    } else {
                        jsonObject.addProperty("field", rs.getString("field"));
                        jsonObject.addProperty("only_field",
                                rs.getString("only_field"));
                        jsonObject.addProperty("field_name",
                                rs.getString("field_name"));
                        jsonObject.addProperty("field_url",
                                rs.getString("field_url"));
                        jsonObject.addProperty("return_field",
                                rs.getString("return_field"));
                        if ((rs.getString("avtivity_name") != null)
                                && (!"".equals(rs.getString("avtivity_name")))) {
                            jsonObject.addProperty("avtivity_name",
                                    rs.getString("avtivity_name"));
                        } else {
                            jsonObject.addProperty("avtivity_name", "");
                        }
                        if ((rs.getString("event_name") != null)
                                && (!"".equals(rs.getString("event_name")))) {
                            jsonObject.addProperty("event_name",
                                    rs.getString("event_name"));
                        } else {
                            jsonObject.addProperty("event_name", "");
                        }
                        if ((rs.getString("add_condition") != null)
                                && (!"".equals(rs.getString("add_condition")))) {
                            jsonObject.addProperty("add_condition",
                                    rs.getString("add_condition"));
                        } else {
                            jsonObject.addProperty("add_condition", "");
                        }
                        jsonObject.addProperty("query_type",
                                rs.getString("query_type"));
                        jsonObject.addProperty("field_type",
                                rs.getString("field_type"));
                        jsonObject.addProperty("table_field",
                                rs.getString("table_field"));
                        jsonObject.addProperty("is_fill_value",
                                rs.getString("is_fill_value"));
                        jsonObject.addProperty("is_encrypt",
                                rs.getString("is_encrypt"));
                        jsonObject.addProperty("is_line", rs.getString("is_line"));
                        jsonObject.addProperty("lable_type",
                                rs.getString("lable_type"));
                        jsonObject.addProperty("curr_type",
                                rs.getString("curr_type"));
                        jsonObject.addProperty("create_user_id",
                                rs.getString("create_user_id"));
                        jsonObject.addProperty("create_user_name",
                                rs.getString("create_user_name"));
                        jsonObject.addProperty("create_time",
                                rs.getString("create_time"));
                        jsonObject.addProperty("update_user_id",
                                rs.getString("update_user_id"));
                        jsonObject.addProperty("update_user_name",
                                rs.getString("update_user_name"));
                        jsonObject.addProperty("update_time",
                                rs.getString("update_time"));

                        if (redisUtil.isCluster) {
                            JedisCluster jedisCluster = (JedisCluster) redisUtil.getjedis();
                            jedisCluster.hset("action_unique_fields", rs.getString("only_field"), jsonObject.toString());
                        } else {
                            Jedis jedis = (Jedis) redisUtil.getjedis();
                            jedis.hset("action_unique_fields", rs.getString("only_field"), jsonObject.toString());
                        }
                    }
                }
                rs.close();
                System.out.println(table + ":成功");
            }
            c.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Sorry,can not find the Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length == 1 && new File(args[0]).exists()) {
            System.out.println("加载的配置文件路径为：" + args[0]);
            conditionFiled(args[0]);
        } else {
            System.err.println("参数1:配置存放的文件地址");
        }
        System.exit(1);
    }
}
