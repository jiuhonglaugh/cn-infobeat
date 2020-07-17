import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * FileName: Test
 * Author:   MAIBENBEN
 * Date:     2020/5/29 15:41
 * History:
 * <author>          <time>          <version>          <desc>
 */
public class Test {
    private static String topic = "wxgz";
    private static String host = "hadoop02:9092";
    private final static Logger LOGGER = LoggerFactory.getLogger(Test.class);


    static {
        System.setProperty("java.security.auth.login.config", "C:/Users/MAIBENBEN/Desktop/kafka_client_jaas1.conf");
//        System.setProperty("java.security.auth.login.config", "C:/Users/MAIBENBEN/Desktop/kafka_client_jaas.conf");
    }

    public static void main(String[] args) {
//        producter();

        consumer();
    }

    private static void producter() {
        KafkaProducer<String, String> producer = new KafkaProducer<>(initProps());
        IntStream.range(0, 10).forEach(i ->{
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, String.valueOf(i), "hello:"+i);
            Future<RecordMetadata> future = producer.send(record);
            try {
                RecordMetadata recordMetadata = future.get();
                LOGGER.info("THIS MESSAGE IS SEND DONE AND THE KEY IS {},OFFSET IS {}", i, recordMetadata.offset());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        producer.flush();
        producer.close();
    }

    private static Properties initProps(){
        final Properties prop = new Properties();
        prop.put("bootstrap.servers",host);
        prop.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");

        //producer-jaas.properties
        prop.put("security.protocol", "SASL_PLAINTEXT");
        prop.put("sasl.mechanism", "PLAIN");
        return prop;
    }

    private static void consumer() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(loadProp());
        consumer.subscribe(Collections.singleton(topic));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            records.forEach(record -> {
                LOGGER.info("key {}, value {}, offset {}, partition {}", record.key(), record.value(), record.offset(), record.partition());
            });
        }
    }

    private static Properties loadProp() {
        final Properties prop = new Properties();
        prop.put("bootstrap.servers", host);
        prop.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put("security.protocol", "SASL_PLAINTEXT");
        prop.put("sasl.mechanism", "PLAIN");
        prop.put("group.id", "test-group");
        prop.put("client.id", "hw2");
        prop.put("auto.offset.reset", "earliest");
        return prop;
    }
}
