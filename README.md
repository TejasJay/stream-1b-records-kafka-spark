# System Architecture

```mermaid
---
config:
  layout: elk
---
flowchart LR
 subgraph Producers["Producers"]
        java["Java Producer"]
        python["Python Producer"]
  end
 subgraph Controllers["Controllers"]
        C1["Controller 1"]
        C2["Controller 2"]
        C3["Controller 3"]
  end
 subgraph Brokers["Brokers"]
        B1["Broker 1"]
        B2["Broker 2"]
        B3["Broker 3"]
  end
 subgraph Kafka["Kafka Cluster"]
        Controllers
        Brokers
        Schema["Schema Registry"]
        Control["Control Center"]
  end
 subgraph StreamProcessing["Apache Spark Cluster"]
        Master["Spark Master"]
        S1["Spark Job 1"]
        S2["Spark Job 2"]
        S3["Spark Job 3"]
  end
 subgraph ElasticStack["Elasticsearch + Visualization"]
        ES["Elasticsearch"]
        Logstash["Logstash"]
        Kibana["Kibana"]
  end
 subgraph Monitoring["Monitoring (Global)"]
        Prometheus["Prometheus"]
        Grafana["Grafana"]
  end
    java -- Streaming --> Kafka
    python -- Streaming --> Kafka
    C1 --> B1
    C2 --> B2
    C3 --> B3
    Schema --> Kafka
    Control --> Kafka
    Kafka -- Stream Data --> S1 & S2 & S3
    Master --> S1 & S2 & S3
    S1 -- Streamed Results --> ES
    S2 -- Streamed Results --> ES
    S3 -- Streamed Results --> ES
    ES --> Logstash
    Logstash --> Kibana
    Monitoring --> Kafka & StreamProcessing & ElasticStack


```

```mermaid
---
config:
  layout: elk
  look: classic
---
flowchart TD
 subgraph Kafka_Cluster["Kafka_Cluster"]
        controller1["kafka-controller-1"]
        controller2["kafka-controller-2"]
        controller3["kafka-controller-3"]
        broker1["kafka-broker-1"]
        broker2["kafka-broker-2"]
        broker3["kafka-broker-3"]
  end
 subgraph Spark_Cluster["Spark_Cluster"]
        spark_master["spark-master"]
        spark_worker_1["spark-worker-1"]
        spark_worker_2["spark-worker-2"]
        spark_worker_3["spark-worker-3"]
  end
 subgraph Monitoring["Monitoring"]
        prometheus["Prometheus"]
        alertmanager["Alertmanager"]
        grafana["Grafana"]
  end
 subgraph Logging["Logging"]
        filebeat["Filebeat"]
        logstash["Logstash"]
        elasticsearch["Elasticsearch"]
        kibana["Kibana"]
  end
    controller1 <---> controller2
    controller2 <---> controller3
    controller3 <---> controller1
    broker1 --> controller1
    broker2 --> controller2
    broker3 --> controller3
    broker1 <--> spark_master
    broker2 <--> spark_master
    broker3 <--> spark_master
    spark_master --> spark_worker_1 & spark_worker_2 & spark_worker_3
    prometheus --> broker1 & broker2 & broker3 & spark_master & spark_worker_1 & spark_worker_2 & spark_worker_3 & alertmanager
    grafana --> prometheus
    filebeat --> elasticsearch
    filebeat -- reads logs --> broker1 & broker2 & broker3 & controller1 & controller2 & controller3
    logstash --> elasticsearch
    logstash -- reads logs --> broker1 & broker2 & broker3 & controller1 & controller2 & controller3
    kibana --> elasticsearch
    schema_registry["Schema Registry"] --> broker1 & broker2 & broker3
    console["Redpanda Console"] --> schema_registry & broker1 & broker2 & broker3
    style controller1 fill:#FFE0B2
    style controller2 fill:#FFE0B2
    style controller3 fill:#FFE0B2
    style broker1 fill:#E1BEE7
    style broker2 fill:#E1BEE7
    style broker3 fill:#E1BEE7
    style spark_master fill:#FF6D00
    style spark_worker_1 fill:#00C853
    style spark_worker_2 fill:#00C853
    style spark_worker_3 fill:#00C853
    style prometheus fill:#BBDEFB
    style alertmanager fill:#BBDEFB
    style grafana fill:#BBDEFB
    style filebeat fill:#FFD600
    style logstash fill:#FFD600
    style elasticsearch fill:#FFD600
    style kibana fill:#FFD600
    style schema_registry fill:#C8E6C9
    style console fill:#FFCDD2
    style Logging fill:#AA00FF
    style Spark_Cluster stroke:#FFD600
    style Kafka_Cluster fill:#FF6D00
    style Monitoring fill:#2962FF

```

# Real-Time Data Streaming Architecture (Kafka + Spark + Elasticsearch + Kibana)

This architecture demonstrates a **real-time streaming pipeline** using the following components:

---

## 🔄 Data Producers

- **Java** and **Python** applications act as producers that publish streaming data into Kafka topics.
- Each producer can send JSON or Avro messages to Kafka based on a defined schema.

---

## 🧠 Kafka Cluster

- Consists of:
  - **Kafka Controllers** (leader election, metadata management)
  - **Kafka Brokers** (actual message storage and retrieval)
  - **Schema Registry** to manage data schemas (e.g., Avro)
  - **Control Center** for visual monitoring of topics, consumers, lag, etc.

---

## ⚡ Stream Processing with Apache Spark

- Spark jobs continuously consume messages from Kafka.
- Transformations include:
  - Parsing & cleansing
  - Aggregations or joins
  - Enriching with metadata
- Output is streamed directly to **Elasticsearch** for indexing.

---

## 🔍 Elasticsearch Stack (ELK)

- **Elasticsearch** stores structured streaming data for fast querying.
- **Logstash** (optional) can further enrich, transform, or route data.
- **Kibana** provides visualization dashboards for real-time insights.

---

## 📈 Monitoring

- **Prometheus** collects system and application metrics.
- **Grafana** visualizes Spark/Kafka health and performance metrics.

---

## 🐳 Containerized Deployment

- All components are designed to run within Docker containers using Docker Compose or Kubernetes.
- Ensures reproducible, scalable, and isolated environments for testing and production.

---

## ✅ Use Cases

- Real-time financial fraud detection
- IoT device analytics
- Clickstream analysis
- Log ingestion & alerting

