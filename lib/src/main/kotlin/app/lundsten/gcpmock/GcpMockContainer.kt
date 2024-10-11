package app.lundsten.gcpmock

import com.github.tomakehurst.wiremock.client.WireMock
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.cloud.kms.v1.KeyManagementServiceSettings
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings
import io.grpc.ManagedChannelBuilder
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.util.concurrent.TimeoutException
import org.testcontainers.containers.GenericContainer
import org.wiremock.grpc.dsl.WireMockGrpcService

class GcpMockContainer(
    dockerImageName: String = "wiremock-gcp-grpc:latest",
    verbose: Boolean = false,
) :
    GenericContainer<GcpMockContainer>(dockerImageName) {

    init {
        withExposedPorts(8080)
        if (verbose) {
            addWiremockConfig("--verbose")
        }
    }

    override fun start() {
        super.start()
        val startMessage = waitForStartMessage()
        this.wiremockVersion = "version:\\s*(\\d+\\.\\d+\\.\\d+)".toRegex()
            .find(startMessage)
            .let { it?.groups?.get(1)?.value ?: throw RuntimeException("Could not find version") }
        this.isVerbose = "verbose:\\s*(true|false)".toRegex()
            .find(startMessage)
            .let { it?.groups?.get(1)?.value?.toBoolean() ?: throw RuntimeException("Could not find verbose") }
    }

    override fun stop() {
        super.stop()
        this.wiremockVersion = null
        this.isVerbose = null
        cleanWiremockSettings()
    }

    fun waitForStartMessage(
        timeout: LocalDateTime = LocalDateTime.now().plusSeconds(30),
    ): String {
        while (timeout > LocalDateTime.now()) {
            val logs = logs
            if (logs.contains("version:\\s*(\\d+\\.\\d+\\.\\d+)".toRegex())) {
                return logs
            }
            sleep(100)
        }

        throw TimeoutException("Get start message timed out")
    }

    var isVerbose: Boolean? = null
    var wiremockVersion: String? = null

    private val channelProvider by lazy {
        assertRunning()
        val channel = ManagedChannelBuilder.forTarget("localhost:$firstMappedPort").usePlaintext().build()
        FixedTransportChannelProvider.create(
            GrpcTransportChannel.create(channel),
        )
    }

    private fun addWiremockConfig(config: String) {
        assertNotRunning()
        val wiremockOptions = env.firstOrNull() { it.contains("WIREMOCK_OPTIONS") }

        if (wiremockOptions != null) {
            if (!wiremockOptions.contains(config)) {
                env = env
                    .filter { it != wiremockOptions }
                    .toMutableList()
                    .also { it.add("$wiremockOptions $config") }
            }
        } else {
            env = env.toMutableList().also { it.add("WIREMOCK_OPTIONS=$config") }
        }
    }

    private fun cleanWiremockSettings() {
        assertNotRunning()
        env = env.filter { !it.contains("WIREMOCK_OPTIONS") }
    }

    fun createSecretManagerMock(): WireMockGrpcService {
        assertRunning()
        return WireMockGrpcService(
            WireMock(firstMappedPort),
            "google.cloud.secretmanager.v1.SecretManagerService",
        )
    }

    fun createKmsServiceMock(): WireMockGrpcService {
        assertRunning()
        return WireMockGrpcService(
            WireMock(firstMappedPort),
            "google.cloud.kms.v1.KeyManagementService",
        )
    }

    fun createSecretManagerClient(
        builder: SecretManagerServiceSettings.Builder = SecretManagerServiceSettings.newBuilder(),
    ): SecretManagerServiceClient {
        val settings = builder
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build()
        return SecretManagerServiceClient.create(settings)
    }

    fun createKmsClient(
        builder: KeyManagementServiceSettings.Builder = KeyManagementServiceSettings.newBuilder(),
    ): KeyManagementServiceClient {
        val settings = builder
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build()
        return KeyManagementServiceClient.create(settings)
    }

    private fun assertRunning() {
        if (!this.isRunning) {
            throw IllegalStateException("Container not running")
        }
    }

    private fun assertNotRunning() {
        if (this.isRunning) {
            throw IllegalStateException("Container is running")
        }
    }
}
