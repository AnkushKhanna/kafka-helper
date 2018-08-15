package kafka

import java.io.IOException

import com.github.tomakehurst.wiremock.client.WireMock.{get, urlEqualTo}
import com.github.tomakehurst.wiremock.client.{ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.common.base.Splitter
import com.google.common.collect.Iterables
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.{RegisterSchemaRequest, RegisterSchemaResponse}
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import org.apache.avro.Schema
import org.scalatest.{BeforeAndAfterAll, Suite}

trait SchemaRegistryAvroTest extends BeforeAndAfterAll {
  this: Suite =>

  private val registrationHandler = new RegistrationHandler()
  private val httpMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().extensions(registrationHandler))
  private val schemaRegistryClient = new MockSchemaRegistryClient

  httpMockRule.start()

  import io.confluent.kafka.schemaregistry.client.rest.entities.ErrorMessage

  httpMockRule.stubFor(WireMock.post(WireMock.urlPathMatching("/subjects/[^/]+/versions")).willReturn(WireMock.aResponse().withTransformers(registrationHandler.getName)))
  httpMockRule.stubFor(WireMock.get(WireMock.urlPathMatching("/schemas/ids/.*")).willReturn(ResponseDefinitionBuilder.okForJson(new ErrorMessage(40401, "Subject not found.")).withStatus(404)))

  final def register(subject: String, schema: Schema): Int = {
    val id = schemaRegistryClient.register(subject, schema)
    httpMockRule.stubFor(get(urlEqualTo("/schemas/ids/" + id)).willReturn(ResponseDefinitionBuilder.okForJson(new SchemaString(schema.toString))))
    id
  }

  final def httpMockPort() = {
    httpMockRule.port()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    httpMockRule.stop()
  }

  private final class RegistrationHandler extends ResponseDefinitionTransformer {
    override def applyGlobally = false

    def transform(request: Request, responseDefinition: ResponseDefinition, files: FileSource, parameters: Parameters): ResponseDefinition = {
      // url = "/subjects/.*-value/versions"
      val subject = Iterables.get(Splitter.on('/').omitEmptyStrings.split(request.getUrl), 1)
      try {
        val id = register(subject, new Schema.Parser().parse(RegisterSchemaRequest.fromJson(request.getBodyAsString).getSchema))
        val registerSchemaResponse = new RegisterSchemaResponse
        registerSchemaResponse.setId(id)
        ResponseDefinitionBuilder.jsonResponse(registerSchemaResponse)
      } catch {
        case e@(_: IOException | _: RestClientException) =>
          throw new RuntimeException("Error while registering the schema of " + subject, e)
      }
    }

    override def getName = "RegistrationHandler"
  }

}
