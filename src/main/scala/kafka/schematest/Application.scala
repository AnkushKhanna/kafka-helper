package kafka.schematest

import java.util.Properties

import com.lightbend.kafka.scala.streams.StreamsBuilderS
import com.typesafe.scalalogging.LazyLogging
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import schemaregistry.kafka.test.{PersonKey, PersonRecord}

object Application extends App with LazyLogging {

  private val schemaRegistryUrl = "http://127.0.0.1:9092"

  private implicit val serde = new PersonSerde(schemaRegistryUrl)

  private val builder = new StreamsBuilderS()
  private val topology: Topology = PersonTopologyBuilder.createTopology(builder, "input-topic", "output-topic")

  private val streams = new KafkaStreams(topology, config())
  streams.setUncaughtExceptionHandler { case ex =>
    logger.error("Error in stream...", ex)
    sys.exit(1)
  }

  streams.start()

  private def config() = {
    val config = new Properties()
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "appId")
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "129.0.0.1:8081")
    // should be edited for production to 3
    config.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "1")
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, classOf[SpecificAvroSerde[PersonKey]])
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[SpecificAvroSerde[PersonRecord]])
    config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    config
  }
}
