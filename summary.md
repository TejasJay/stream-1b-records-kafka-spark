
# 🧠 Full System Summary: Kafka + Spark + Monitoring + ELK Stack


| Component           | Why It's Used                                         | How It's Connected                                                  | Example Use Case                                      | Ports Exposed         | Advantages                                  | Disadvantages                           |
|---------------------|------------------------------------------------------|----------------------------------------------------------------------|--------------------------------------------------------|------------------------|----------------------------------------------|------------------------------------------|
| **Kafka Controllers**  | Manage metadata, leader election (KRaft)             | Brokers connect to them over internal port `9093`                    | Elect new leader for a partition                       | 9093 (internal only)   | Removes need for ZooKeeper                 | Not exposed externally, harder to debug |
| **Kafka Brokers**      | Store and serve messages, handle topics              | Talk to controllers, expose ports to clients                        | Serve real-time events to Spark                        | 29092/39092/49092      | Scalable, high throughput                  | Needs tuning & monitoring               |
| **Schema Registry**    | Enforce data format contracts (schemas)              | Connected to all brokers and console                                | Reject incompatible Avro message                       | 18081                  | Prevents bad data from entering Kafka      | One more component to maintain          |
| **Console (UI)**       | Visualize Kafka topics, schemas, groups              | Connects to brokers and schema registry                             | View topic partitions and consumer lag                 | 8080                   | Easy-to-use Kafka UI                       | Read-only, basic for ops                |
| **Spark Master/Workers** | Real-time stream processing                        | Connect to Kafka (input) and Prometheus (metrics)                   | Filter and enrich Kafka messages                       | 7077, 8080, 4040        | Fast, scalable compute                     | Needs memory management                 |
| **Prometheus**         | Collect and store metrics                            | Pulls from brokers, Spark, sends alerts to Alertmanager             | Track consumer group lag                               | 9090                   | Flexible, powerful queries (PromQL)        | Needs Grafana for visualization         |
| **Alertmanager**       | Route alerts from Prometheus                         | Only connected to Prometheus                                        | Send Slack alert on broker failure                     | 59093                  | Multi-channel alerting                     | Needs alert tuning                      |
| **Grafana**            | Build visual dashboards from Prometheus/ES data      | Connects to Prometheus and Elasticsearch                            | Kafka & Spark health dashboards                        | 3000                   | Great dashboards and UI                    | Initial config required                 |
| **Elasticsearch**      | Store and search logs                                | Gets data from Filebeat/Logstash, powers Kibana                     | Search all ERROR logs in 24h                           | 9200                   | Super fast search, flexible schema         | Memory intensive                        |
| **Kibana**             | Explore/search logs in Elasticsearch                 | Connects only to Elasticsearch                                      | Visualize Kafka controller logs                        | 5601                   | Real-time log dashboards                   | Dependent on Elasticsearch availability |
| **Filebeat**           | Lightweight log shipper                              | Reads logs from disk and sends to Elasticsearch                     | Ship Kafka broker logs to ES                           | N/A                    | Simple and resource-friendly               | No parsing ability                      |
| **Logstash**           | Advanced log parser and transformer                  | Reads Kafka logs, transforms, sends to Elasticsearch                | Parse logs and add tags                                | 5000                   | Super flexible log pipelines               | Config complexity, more resource usage  |


* * *

## 🔸 **Kafka Cluster**

### 🎯 Components:

-   **Kafka Controllers**: `kafka-controller-1`, `2`, `3`
-   **Kafka Brokers**: `kafka-broker-1`, `2`, `3`

### 🔗 Connections:

-   Brokers connect to controllers using `KRaft` mode (no ZooKeeper).
-   Controllers manage **metadata**, topic assignments, and leadership.
-   Brokers store and serve topic data to producers and consumers (like Spark, Console).

### 📌 Ports:

-   `9093`: Internal controller listener (not exposed to host)
-   `19092`: Internal broker communication
-   `29092/39092/49092`: Host-mapped broker ports for client access

### ✅ Use Case Example:

-   Kafka receives customer orders from apps and distributes them to Spark for processing.

### ➕ Advantages:

-   High-throughput, scalable messaging.
-   Decouples data producers and consumers.

### ➖ Disadvantages:

-   Requires tuning and monitoring to avoid bottlenecks.
-   Can be complex to debug in production.
* * *

## 🔸 **Schema Registry**

### 📦 Container: `schema-registry`

### 📌 Purpose:

-   Stores **message schemas** (Avro, JSON Schema, Protobuf).
-   Ensures **producers and consumers agree** on data format.

### 🔗 Connections:

-   Connects to all Kafka brokers.
-   Integrated with Redpanda Console.

### 🌐 Port: `18081` → `http://localhost:18081`

### ✅ Use Case Example:

-   Reject invalid messages missing required fields.

### ➕ Advantages:

-   Prevents schema drift.
-   Enables versioning and compatibility checks.

### ➖ Disadvantages:

-   Adds one more dependency to maintain.
* * *

## 🔸 **Redpanda Console**

### 📦 Container: `console`

### 📌 Purpose:

-   Web UI for Kafka — browse topics, view messages, inspect schemas and consumer groups.

### 🔗 Connections:

-   Talks to Kafka brokers and Schema Registry.

### 🌐 Port: `8080` → `http://localhost:8080`

### ✅ Use Case Example:

-   Inspect consumer lag in real time.

### ➕ Advantages:

-   Zero-config UI for Kafka.
-   Great for debugging and development.

### ➖ Disadvantages:

-   Read-only; no advanced operations.
* * *

## 🔸 **Spark Cluster**

### 📦 Containers:

-   `spark-master`
-   `spark-worker-1`, `2`, `3`

### 📌 Purpose:

-   Distributed computation engine used for **stream processing**, batch jobs, or ML.

### 🔗 Connections:

-   Consumes data from Kafka.
-   Sends metrics to Prometheus.

### 🌐 Ports:

-   `7077`: Spark Master internal communication
-   `8080`: Spark Master UI
-   `4040`: Job execution UI (when running)
-   `8081`: Extra UI/metrics

### ✅ Use Case Example:

-   Enrich, aggregate, or filter real-time Kafka events (e.g., filter high-value orders).

### ➕ Advantages:

-   Extremely fast and scalable processing engine.
-   Supports Python, Scala, and SQL-based data pipelines.

### ➖ Disadvantages:

-   Can consume a lot of memory.
-   Requires careful tuning for stability.
* * *

## 🔸 **Prometheus**

### 📦 Container: `prometheus`

### 📌 Purpose:

-   Collects and stores **time-series metrics** from services like Kafka and Spark.

### 🔗 Connections:

-   Scrapes metrics from all brokers, controllers, Spark nodes.
-   Sends alerts to Alertmanager.
-   Used as data source in Grafana.

### 🌐 Port: `9090` → `http://localhost:9090`

### ✅ Use Case Example:

-   Track JVM memory usage or Kafka message throughput.

### ➕ Advantages:

-   Fast, pull-based metrics system.
-   Powerful query language (PromQL).

### ➖ Disadvantages:

-   No native dashboards — needs Grafana for visuals.
* * *

## 🔸 **Alertmanager**

### 📦 Container: `alertmanager`

### 📌 Purpose:

-   Receives alerts from Prometheus and **sends notifications** (email, Slack, etc.)

### 🔗 Connections:

-   Connected only to Prometheus.

### 🌐 Port: `59093` → `http://localhost:59093`

### ✅ Use Case Example:

-   Send alert when broker is down or consumer lag spikes.

### ➕ Advantages:

-   Flexible routing and silencing rules.

### ➖ Disadvantages:

-   Requires good alert tuning to avoid noise.
* * *

## 🔸 **Grafana**

### 📦 Container: `grafana`

### 📌 Purpose:

-   Dashboarding tool that reads from Prometheus (and optionally Elasticsearch).

### 🔗 Connections:

-   Connects to Prometheus and Elasticsearch (if logs are visualized).

### 🌐 Port: `3000` → `http://localhost:3000`

### ✅ Use Case Example:

-   Build a Kafka dashboard showing consumer group lag and broker health.

### ➕ Advantages:

-   Beautiful, interactive dashboards.
-   Can alert from visual panels too.

### ➖ Disadvantages:

-   Initial setup/config might feel overwhelming.
* * *

## 🔸 **Elasticsearch**

### 📦 Container: `es-container`

### 📌 Purpose:

-   Full-text search engine that stores logs and system events.

### 🔗 Connections:

-   Receives logs from Filebeat and Logstash.
-   Feeds Kibana dashboards.

### 🌐 Port: `9200` → `http://localhost:9200`

### ✅ Use Case Example:

-   Query all Kafka error logs from the last hour.

### ➕ Advantages:

-   Blazing fast search.
-   Great integration with Kibana.

### ➖ Disadvantages:

-   High memory usage.
-   Slower at scale if not tuned.
* * *

## 🔸 **Kibana**

### 📦 Container: `kb-container`

### 📌 Purpose:

-   UI for viewing logs stored in Elasticsearch.

### 🔗 Connections:

-   Connects directly to Elasticsearch.

### 🌐 Port: `5601` → `http://localhost:5601`

### ✅ Use Case Example:

-   Build dashboards to monitor log trends over time.

### ➕ Advantages:

-   Real-time log search, filtering, visualization.
-   Rich ecosystem of visual tools.

### ➖ Disadvantages:

-   Totally dependent on Elasticsearch health.
* * *

## 🔸 **Filebeat**

### 📦 Container: `filebeat`

### 📌 Purpose:

-   Lightweight log shipper that tails `.log` files and sends them to Elasticsearch.

### 🔗 Connections:

-   Reads logs from Kafka containers.
-   Sends to Elasticsearch.

### 🔗 Config:

-   `filebeat.yml` defines log paths and Elasticsearch target.

### ✅ Use Case Example:

-   Ship Kafka controller logs to Elasticsearch for central viewing.

### ➕ Advantages:

-   Very low resource usage.
-   Easy to deploy.

### ➖ Disadvantages:

-   No log parsing — just ships raw logs.
* * *

## 🔸 **Logstash**

### 📦 Container: `ls-container`

### 📌 Purpose:

-   Advanced **log processor** that can parse, transform, filter, and enrich log data.

### 🔗 Connections:

-   Reads from the same Kafka logs as Filebeat.
-   Sends structured logs to Elasticsearch.

### 🌐 Port: `5000` (optional for receiving logs)

### 🔗 Config:

-   `pipeline/logstash.conf` defines full flow (input → filter → output)

### ✅ Use Case Example:

-   Parse custom log format into JSON and send to Elasticsearch with enriched fields.

### ➕ Advantages:

-   Very flexible (regex, conditionals, tagging, etc.)

### ➖ Disadvantages:

-   Heavy on CPU/memory compared to Filebeat.
-   Takes more effort to configure correctly.
* * *


-   **All services are on the same Docker network (`codewithyu`)** so they can talk via container names.
-   JMX Exporter is embedded in Kafka and Spark to expose Prometheus-friendly metrics.
-   Logging is **dual-pathed**:
    -   Filebeat for lightweight log shipping
    -   Logstash for parsing/enrichment
-   Metrics (Prometheus) and logs (Elasticsearch) are **joined visually via Grafana and Kibana**.
* * *

