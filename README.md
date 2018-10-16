## Using schema registry with MockedStreams Kafka
Using mocked streams with AVRO and schema registry in Kafka have been a problem.

Mocked streams works fine for StringSerdes, but using SpecificAvroSerde or Avro type did not work so well.

### Test suit 
I have created a WireMock schema-registry client in [SchemaRegistryAvroTest](src/test/scala/kafka/SchemaRegistryAvroTest.scala)

You can simply extend to any of your test using `extend` as show in [PersonTopologyBuilderTest](src/test/scala/kafka/schematest/PersonTopologyBuilderTest.scala)

For now there is no library available, you can use it via just copying the `SchemaRegistryAvroTest` code in your code base.

#### Usage
You have to specifiy your schema registry url as `s"http://localhost:${httpMockPort()}"` in your test and register your schema as follow:

```$scala
  register(s"input-topic-key", PersonKey.SCHEMA$)
  register(s"input-topic-value", PersonRecord.SCHEMA$)
```

where `input-topic` is the name of your input topic.

#### Example
[PersonTopologyBuilderTest](src/test/scala/kafka/schematest/PersonTopologyBuilderTest.scala)

