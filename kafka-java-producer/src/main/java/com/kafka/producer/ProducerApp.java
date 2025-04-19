// Java version of the Python Kafka producer
// You will need the following Maven dependencies:
//
// <dependency>
//   <groupId>org.apache.kafka</groupId>
//   <artifactId>kafka-clients</artifactId>
//   <version>3.5.1</version>
// </dependency>
//
// <dependency>
//   <groupId>org.json</groupId>
//   <artifactId>json</artifactId>
//   <version>20231013</version>
// </dependency>

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.*;

public class TransactionProducer {

    private static final String KAFKA_BROKERS = "localhost:29092,localhost:39092,localhost:49092";
    private static final String TOPIC_NAME = "financial_transactions";
    private static final int NUM_PARTITIONS = 5;
    private static final short REPLICATION_FACTOR = 3;

    private static final Properties producerProps = new Properties();

    static {
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.ACKS_CONFIG, "1");
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, "10");
        producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 1000);
        producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 512000L);
    }

    public static void main(String[] args) throws Exception {
        createTopic(TOPIC_NAME);
        int numThreads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            int finalI = i;
            executor.submit(() -> produceTransaction(finalI));
        }
    }

    private static void createTopic(String topicName) {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);

        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            Set<String> existingTopics = adminClient.listTopics().names().get(10, TimeUnit.SECONDS);
            if (!existingTopics.contains(topicName)) {
                NewTopic newTopic = new NewTopic(topicName, NUM_PARTITIONS, REPLICATION_FACTOR);
                adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                System.out.println("✅ Topic created: " + topicName);
            } else {
                System.out.println("ℹ️ Topic already exists: " + topicName);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to create topic: " + e.getMessage());
        }
    }

    private static void produceTransaction(int threadId) {
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
        Random rand = new Random();

        while (true) {
            try {
                JSONObject txn = new JSONObject();
                txn.put("transactionID", UUID.randomUUID().toString());
                txn.put("userId", "user_" + rand.nextInt(100));
                txn.put("amount", Math.round((50000 + rand.nextDouble() * 100000) * 100.0) / 100.0);
                txn.put("transactionTime", System.currentTimeMillis() / 1000);
                txn.put("merchantId", "merchant_" + (rand.nextInt(3) + 1));
                txn.put("transactionType", rand.nextBoolean() ? "purchase" : "refund");
                txn.put("location", "location_" + (rand.nextInt(50) + 1));
                txn.put("paymentMethod", rand.nextBoolean());
                txn.put("isInternational", rand.nextBoolean());
                txn.put("currency", List.of("USD", "EUR", "GBP").get(rand.nextInt(3)));

                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, txn.getString("userId"), txn.toString());
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        System.out.println("Thread " + threadId + " ✅ Sent: " + txn);
                    } else {
                        System.err.println("Thread " + threadId + " ❌ Error: " + exception.getMessage());
                    }
                });

                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("Thread " + threadId + " ❌ Exception: " + e.getMessage());
            }
        }
    }
}

