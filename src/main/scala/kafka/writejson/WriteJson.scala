package kafka.writejson

import java.io.{File, PrintWriter}
import java.time.Duration
import java.util.Properties
import java.{lang, util}

import io.confluent.kafka.streams.serdes.avro.GenericAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRebalanceListener, KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import scala.collection.JavaConverters._

object WriteJson extends App {

  if (args.length < 3) throw new Exception("Argument exception")

  val bootstrapServer = args(0)
  val topic = args(1)
  val fileName = args(2)
  val hours =
    if (args.length > 3) args(3).toInt
    else 0

  val milliSecondsBack: Long = hours * 60 * 60 * 1000

  println(s"Hours going back: $hours, milliseconds going back: $milliSecondsBack")

  val consumer = new KafkaConsumer[GenericRecord, GenericRecord](prop())

  val consumerRebalanceListener = new ConsumerRebalanceListener {

    def seekBack(millis: Long, partitions: util.Collection[TopicPartition]) = {
      val partitionTime: util.Map[TopicPartition, lang.Long] = partitions.asScala.map(p => new TopicPartition(p.topic(), p.partition()) -> new lang.Long(System.currentTimeMillis() - milliSecondsBack)).toMap.asJava

      val offsetsForTimes = consumer.offsetsForTimes(partitionTime)

      offsetsForTimes.asScala.foreach(x => consumer.seek(x._1, x._2.offset))
    }

    override def onPartitionsRevoked(partitions: util.Collection[TopicPartition]): Unit = {
      println("on partition revoked")
      seekBack(milliSecondsBack, partitions)
    }

    override def onPartitionsAssigned(partitions: util.Collection[TopicPartition]): Unit = {
      println("on partition assigned")
      seekBack(milliSecondsBack, partitions)
    }
  }

  consumer.subscribe(List(topic).asJavaCollection, consumerRebalanceListener)

  val file = new File(fileName)
  if (!file.exists()) file.createNewFile()

  val printWriter = new PrintWriter(file)

  var startTime = System.currentTimeMillis()

  var recordTimeStamp: Long = 0

  while (startTime > recordTimeStamp) {
    val records = consumer.poll(Duration.ofSeconds(2)).iterator().asScala.toList
    println(s"Looping ${records.size}")

    if (records.nonEmpty) recordTimeStamp = records.lastOption.get.timestamp()

    records.foreach(r => printWriter.write(r.value().toString + "\n"))
    records.foreach(r => println(r.key().toString + "\n"))
    printWriter.flush()
  }

  sys.addShutdownHook(() => {
    printWriter.flush()
    printWriter.close()
    consumer.close(Duration.ofSeconds(30))
  })

  def prop() = {
    val properties = new Properties()
    properties.put(ConsumerConfig.CLIENT_ID_CONFIG, s"WriteJson-${System.currentTimeMillis()}")
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, s"WriteJson-${System.currentTimeMillis()}")
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
    properties.put("schema.registry.url", "http://localhost:8082")
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[GenericAvroDeserializer])
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[GenericAvroDeserializer])
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    properties
  }
}
