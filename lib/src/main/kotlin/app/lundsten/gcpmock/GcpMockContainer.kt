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
import org.testcontainers.containers.GenericContainer
import org.wiremock.grpc.dsl.WireMockGrpcService

class GcpMockContainer(dockerImageName: String = "wiremock-gcp-grpc:latest") :
    GenericContainer<GcpMockContainer>(dockerImageName) {
    init {
        withExposedPorts(8080)
    }

    val channelProvider by lazy {
        assertRunning()
        val channel = ManagedChannelBuilder.forTarget("localhost:$firstMappedPort").usePlaintext().build()
        FixedTransportChannelProvider.create(
            GrpcTransportChannel.create(channel)
        )
    }

    fun activateVerboseLogging() {
        val wiremockOptions = env.firstOrNull() { it.contains("WIREMOCK_OPTIONS") }

        if (wiremockOptions != null) {
            if (!wiremockOptions.contains("--verbose")) {
                env = env
                    .filter { it != wiremockOptions }
                    .toMutableList()
                    .also { it.add("$wiremockOptions --verbose") }
            }
        } else {
            env.add("WIREMOCK_OPTIONS=--verbose")
        }
    }

    fun createSecretManagerMock(): WireMockGrpcService {
        assertRunning()
        return WireMockGrpcService(
            WireMock(firstMappedPort),
            "google.cloud.secretmanager.v1.SecretManagerService"
        )
    }

    fun createKmsServiceMock(): WireMockGrpcService {
        assertRunning()
        return WireMockGrpcService(
            WireMock(firstMappedPort),
            "google.cloud.kms.v1.KeyManagementService"
        )
    }

    fun createSecretManagerClient(
        builder: SecretManagerServiceSettings.Builder = SecretManagerServiceSettings.newBuilder()
    ): SecretManagerServiceClient {
        val settings = builder
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build()
        return SecretManagerServiceClient.create(settings)
    }

    fun createKmsClient(
        builder: KeyManagementServiceSettings.Builder = KeyManagementServiceSettings.newBuilder()
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
