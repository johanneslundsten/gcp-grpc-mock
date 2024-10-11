package app.lundsten.gcpmock

import com.google.api.gax.rpc.FailedPreconditionException
import kotlin.text.Charsets.UTF_8
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.wiremock.grpc.dsl.GrpcResponseDefinitionBuilder
import org.wiremock.grpc.dsl.WireMockGrpc
import org.wiremock.grpc.dsl.WireMockGrpc.method

class GcpSecretManagerMockTest {
    companion object {
        @JvmStatic
        @Container
        private val container = GcpMockContainer()

        @JvmStatic
        @BeforeAll
        fun start() {
            container.start()
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            container.stop()
        }
    }

    private val secretManagementServiceMock = container.createSecretManagerMock()
    private val secretManagerClient = container.createSecretManagerClient()

    @AfterEach
    fun resetMock() {
        secretManagementServiceMock.resetAll()
    }

    @Test
    fun `Should be able to set delay`() {
        val secret = "my secret value"
        val (serialized, _) = MockUtils.serializeString(secret)
        // language=JSON
        val json = """
            {
              "payload":{
                  "data":$serialized
                }
            }
        """.trimIndent()

        secretManagementServiceMock.stubFor(
            method("AccessSecretVersion")
                .willReturn(
                    GrpcResponseDefinitionBuilder(WireMockGrpc.Status.OK).fromJson(json)
                        .withFixedDelay(1001),
                ),
        )

        val duration = measureTime {
            secretManagerClient.accessSecretVersion("my-secret")
        }

        assertTrue(duration > 1.seconds)
        assertTrue(duration < 2.seconds)
    }

    @Test
    fun `Should be able to mock AccessSecretVersion`() {
        val secret = "my secret value"
        val (serialized, _) = MockUtils.serializeString(secret)
        // language=JSON
        val json = """
            {
              "payload":{
                  "data":$serialized
                }
            }
        """.trimIndent()

        secretManagementServiceMock.stubFor(
            method("AccessSecretVersion")
                .willReturn(GrpcResponseDefinitionBuilder(WireMockGrpc.Status.OK).fromJson(json)),
        )

        val accessSecretVersion = secretManagerClient.accessSecretVersion("my-secret")
        assertEquals(secret, accessSecretVersion.payload.data.toString(UTF_8))
    }

    @Test
    fun `Should be able to respond with a fault`() {
        secretManagementServiceMock.stubFor(
            method("AccessSecretVersion")
                .willReturn(GrpcResponseDefinitionBuilder(WireMockGrpc.Status.FAILED_PRECONDITION)),
        )

        assertThrows<FailedPreconditionException> { secretManagerClient.accessSecretVersion("my-secret") }
    }
}
