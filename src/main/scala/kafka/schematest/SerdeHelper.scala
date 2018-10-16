package kafka.schematest

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecordBase

object SerdeHelper {
  def createSerde[T <: SpecificRecordBase](isKey: Boolean, schemaRegistryUrl: String): SpecificAvroSerde[T] = {
    val serde = new SpecificAvroSerde[T]()

    val properties = new java.util.HashMap[String, String]()
    properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)

    serde.configure(properties, isKey)
    serde
  }
}

