package kafka.schematest

import com.lightbend.kafka.scala.streams.StreamsBuilderS
import schemaregistry.kafka.test.{PersonKey, PersonRecord}

object PersonTopologyBuilder {

  def createTopology(builder: StreamsBuilderS, inputTopic: String, outputTopic: String)(implicit serdes: PersonSerde) = {
    import serdes._
    import com.lightbend.kafka.scala.streams.ImplicitConversions._

    builder.stream[PersonKey, PersonRecord](inputTopic)
      .mapValues(person => person.copy(firstName = person.firstName.capitalize))
      .mapValues(person => person.copy(lastName = person.lastName.capitalize))
      .to(outputTopic)

    builder.build()
  }
}
