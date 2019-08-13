import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "favorite-colour-java");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> textLines = builder.stream("streams-favorite-colour-input");
        KStream<String, String> usersAndColours = textLines
                // we select a key that will be the user id (lowercase for safety)
                .selectKey((key, value) -> value.split(",")[0].toLowerCase())
                // we get the colour from the value (lowercase for safety)
                .mapValues(value -> value.split(",")[1].toLowerCase().trim())
                .filter((key, value) -> Arrays.asList("blue", "red", "green").contains(value));

        usersAndColours.to(Serdes.String(), Serdes.String(), "streams-favorite-colour-inter");

        // we read that topic as a KTable so that updates are read correctly
        KTable<String, String> usersAndColoursTable = builder.table("streams-favorite-colour-inter");

        // we count the occurrences of colours
        KTable<String, Long> favouriteColours = usersAndColoursTable
                // 5 - we group by colour within the KTable
                .groupBy((user, colour) -> new KeyValue<>(colour, colour))
                .count("CountsByColours");

        // print the results to the standard output
        favouriteColours.foreach((w, c) -> System.out.println("word: " + w + " -> " + c));

        // we output the results to a Kafka Topic - don't forget the serializers
        favouriteColours.to(Serdes.String(), Serdes.Long(),"streams-favorite-colour-output");

        KafkaStreams streams = new KafkaStreams(builder, properties);
        // only do this in dev - not in prod
        streams.cleanUp();
        streams.start();

        // print the topology
        System.out.println(streams.toString());

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
