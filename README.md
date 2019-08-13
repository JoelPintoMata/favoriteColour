# Favorite colour
Based on Kafka Udemy course

## How to use
__start zookeeper__
```
bin/zookeeper-server-start.sh config/zookeeper.properties
```

__start kafka server__
```
bin/kafka-server-start.sh config/server.properties 
```
__create input topic__
```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic streams-favorite-colour-input
```
```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic streams-favorite-colour-output
```
__create a kafka consumer on the output topic__
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic streams-favorite-colour-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```
__start a kafka producer on the input topic__
```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic streams-favorite-colour-input
```