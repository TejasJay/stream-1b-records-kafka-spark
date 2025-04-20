# System Architecture

```mermaid
flowchart LR
    subgraph Producers
        java[Java Producer]
        python[Python Producer]
    end

    subgraph Kafka["Kafka Cluster"]
        subgraph Controllers
            C1[Controller 1]
            C2[Controller 2]
            C3[Controller 3]
        end
        subgraph Brokers
            B1[Broker 1]
            B2[Broker 2]
            B3[Broker 3]
        end
        Schema[Schema Registry]
        Control[Control Center]
    end

    subgraph StreamProcessing["Apache Spark Stream Processors"]
        S1[Spark Job 1]
        S2[Spark Job 2]
        S3[Spark Job 3]
    end

    subgraph ElasticStack["Elasticsearch + Visualization"]
        ES[Elasticsearch]
        Logstash
        Kibana
    end

    subgraph Monitoring
        Prometheus
        Grafana
    end

    java -->|Streaming| C1
    python -->|Streaming| C2
    C1 --> B1
    C2 --> B2
    C3 --> B3
    Schema --> Kafka
    Control --> Kafka
    B1 -->|Stream Data| S1
    B2 -->|Stream Data| S2
    B3 -->|Stream Data| S3
    S1 -->|Streamed Results| ES
    S2 -->|Streamed Results| ES
    S3 -->|Streamed Results| ES
    ES --> Logstash --> Kibana
    Prometheus --> Monitoring
    Grafana --> Monitoring
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

