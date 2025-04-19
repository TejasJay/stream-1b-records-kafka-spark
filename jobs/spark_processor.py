from pyspark.sql import SparkSession
from pyspark.sql.functions import col, from_json, sum, count, to_json, struct
from pyspark.sql.types import StructType, StructField, StringType, DoubleType, LongType

KAFKA_BROKERS = "kafka-broker-1:19092,kafka-broker-2:19092,kafka-broker-3:19092"
SOURCE_TOPIC = "financial_transactions"
AGGREGATES_TOPIC = "transaction_aggregates"
ANOMALIES_TOPIC = 'transaction_anomalies'
CHECKPOINT_DIR = '/mnt/spark-checkpoints'
STATE_DIR = '/mnt/spark-state'

# SparkSession setup
spark = (SparkSession.builder
         .appName('FinancialTransactionsProcessor')
         .config('spark.jars.packages', 'org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.3')
         .config('spark.sql.streaming.checkpointLocation', CHECKPOINT_DIR)
         .config('spark.sql.streaming.stateStore.stateStoreProviderClass', 'org.apache.spark.sql.execution.streaming.state.HDFSBackedStateStoreProvider')
         .config('spark.sql.shuffle.partitions', '20')  # typo fix: 'suffle' -> 'shuffle'
         ).getOrCreate()

spark.sparkContext.setLogLevel("WARN")

# Schema for incoming Kafka messages
transaction_schema = StructType([
    StructField('transactionId', StringType(), True),
    StructField('userId', StringType(), True),
    StructField('amount', DoubleType(), True),
    StructField('transactionTime', LongType(), True),
    StructField('merchantId', StringType(), True),
    StructField('transactionType', StringType(), True),
    StructField('location', StringType(), True),
    StructField('paymentMethod', StringType(), True),
    StructField('isInternational', StringType(), True),
    StructField('currency', StringType(), True),
])

# Reading from Kafka
kafka_stream = (spark.readStream
                .format("kafka")
                .option('kafka.bootstrap.servers', KAFKA_BROKERS)
                .option('subscribe', SOURCE_TOPIC)
                .option('startingOffsets', 'earliest')
                .load())

# Parse the JSON from Kafka
transaction_df = kafka_stream.selectExpr("CAST(value as STRING)") \
    .select(from_json(col('value'), transaction_schema).alias('data')) \
    .select('data.*')

# Add proper timestamp column
transaction_df = transaction_df.withColumn('transactionTimestamp',
                                           (col('transactionTime') / 1000).cast("timestamp"))

# Group by merchant and calculate aggregates
aggregated_df = transaction_df.groupBy("merchantId") \
    .agg(
        sum("amount").alias('totalAmount'),
        count("*").alias("transactionCount")
    )

# Write aggregates to another Kafka topic
aggregation_query = aggregated_df \
    .withColumn("key", col('merchantId').cast("string")) \
    .withColumn("value", to_json(struct(
        col('merchantId'),
        col('totalAmount'),
        col('transactionCount')
    ))) \
    .selectExpr("key", "value") \
    .writeStream \
    .format('kafka') \
    .outputMode('update') \
    .option('kafka.bootstrap.servers', KAFKA_BROKERS) \
    .option('topic', AGGREGATES_TOPIC) \
    .option('checkpointLocation', f'{CHECKPOINT_DIR}/aggregates') \
    .start().awaitTermination()

# create topics before you run the below command 

# Run in terminal
# docker exec -it spark-master spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.3 jobs/spark_processor.py

