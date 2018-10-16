package kafka.schematest

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import schemaregistry.kafka.test.{PersonKey, PersonRecord}

class PersonSerde(schemaRegistryUrl: String) {

  import SerdeHelper._

  implicit val personKeySerde: SpecificAvroSerde[PersonKey] = createSerde[PersonKey](true, schemaRegistryUrl)

  implicit val personRecordSerde: SpecificAvroSerde[PersonRecord] = createSerde[PersonRecord](false, schemaRegistryUrl)

}
