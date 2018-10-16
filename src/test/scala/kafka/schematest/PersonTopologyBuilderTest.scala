package kafka.schematest

import com.lightbend.kafka.scala.streams.StreamsBuilderS
import com.madewithtea.mockedstreams.MockedStreams
import kafka.SchemaRegistryAvroTest
import org.scalatest.FlatSpec
import schemaregistry.kafka.test.{PersonKey, PersonRecord}

class PersonTopologyBuilderTest extends FlatSpec with SchemaRegistryAvroTest {
  private val inputTopic = "input-topic"
  private val outputTopic = "output-topic"
  private val schemaRegistryUrl = s"http://localhost:${httpMockPort()}"
  private implicit val serde: PersonSerde = new PersonSerde(schemaRegistryUrl)

  register(s"$inputTopic-key", PersonKey.SCHEMA$)
  register(s"$inputTopic-value", PersonRecord.SCHEMA$)

  import serde._

  "WaypointServiceTopology" should "create store with provided StoreName" in {
    val key = PersonKey(1)
    val person = PersonRecord("ankush", "khanna")

    val mstreams = MockedStreams().topology { builder =>
      PersonTopologyBuilder.createTopology(new StreamsBuilderS(inner = builder), inputTopic, outputTopic)
    }
      .input(inputTopic, personKeySerde, personRecordSerde, Seq((key, person)))

    val outputRecord = mstreams.output(outputTopic, personKeySerde, personRecordSerde, 1)
    assert(outputRecord.size === 1)
    assert(outputRecord.head._2.firstName === "Ankush")
    assert(outputRecord.head._2.lastName === "Khanna")

  }
}
