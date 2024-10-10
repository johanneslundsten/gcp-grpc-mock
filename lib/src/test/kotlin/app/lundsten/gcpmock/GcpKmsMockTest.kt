package app.lundsten.gcpmock

import com.google.cloud.kms.v1.CryptoKeyVersion
import com.google.cloud.kms.v1.ProtectionLevel
import com.google.protobuf.ByteString
import kotlin.text.Charsets.UTF_8
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.wiremock.grpc.dsl.GrpcResponseDefinitionBuilder
import org.wiremock.grpc.dsl.WireMockGrpc
import org.wiremock.grpc.dsl.WireMockGrpc.method

class GcpKmsMockTest {
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

    private val kmsMock = container.createKmsServiceMock()
    private val client = container.createKmsClient()

    @Test
    fun `Should be able to mock with decrypt`() {
        val secretValue = "my secret value"
        val (serialized, crc) = MockUtils.serializeString(secretValue)

        // language=JSON
        val json = """
            {
              "plaintext": "$serialized",
              "usedPrimary": true,
              "plaintext_crc32c": $crc
            }
        """.trimIndent()

        kmsMock.stubFor(
            method("Decrypt")
                .willReturn(GrpcResponseDefinitionBuilder(WireMockGrpc.Status.OK).fromJson(json)),
        )

        val response = client.decrypt("name", ByteString.copyFromUtf8("my secret value"))
        assertEquals(secretValue, response.plaintext.toString(UTF_8))
        assertTrue(response.usedPrimary)
    }

    @Test
    fun `Should be able to mock encrypt`() {
        val encryptedStuff = "encrypted-stuff"
        val (serialized, crc) = MockUtils.serializeString(encryptedStuff)

        // language=JSON
        val json = """
            {
              "name": "my name",
              "ciphertext": $serialized,
              "ciphertext_crc32c": $crc
            }
        """.trimIndent()
        kmsMock.stubFor(
            method("Encrypt")
                .willReturn(GrpcResponseDefinitionBuilder(WireMockGrpc.Status.OK).fromJson(json)),
        )

        val response = client.encrypt("my-key", ByteString.copyFromUtf8("content i want to encrypt"))
        assertEquals(encryptedStuff, response.ciphertext.toString(UTF_8))
        assertEquals(crc, response.ciphertextCrc32C.value)
        assertEquals("my name", response.name)

    }

    @Test
    fun `Should be able to mock destroy key`() {
        // language=JSON
        val json = """
            {
              "name":"name",
              "state":${CryptoKeyVersion.CryptoKeyVersionState.PENDING_IMPORT.number},
              "protectionLevel":${ProtectionLevel.HSM.number},
              "algorithm":${CryptoKeyVersion.CryptoKeyVersionAlgorithm.RSA_DECRYPT_OAEP_3072_SHA1.number},
              "importJob":"importJob",
              "importFailureReason":"importFailureReason",
              "generationFailureReason":"generationFailureReason",
              "externalDestructionFailureReason":"externalDestructionFailureReason"}
        """.trimIndent()

        kmsMock.stubFor(
            method("DestroyCryptoKeyVersion")
                .willReturn(GrpcResponseDefinitionBuilder(WireMockGrpc.Status.OK).fromJson(json)),
        )

        val response = client.destroyCryptoKeyVersion("keyId")
        assertEquals( "name", response.name)
        assertEquals( CryptoKeyVersion.CryptoKeyVersionState.PENDING_IMPORT, response.state)
        assertEquals( ProtectionLevel.HSM, response.protectionLevel)
        assertEquals( CryptoKeyVersion.CryptoKeyVersionAlgorithm.RSA_DECRYPT_OAEP_3072_SHA1, response.algorithm)
        assertEquals( "importJob", response.importJob)
        assertEquals( "importFailureReason", response.importFailureReason)
        assertEquals( "generationFailureReason", response.generationFailureReason)
        assertEquals( "externalDestructionFailureReason", response.externalDestructionFailureReason)
    }



    @Test
    fun `Should be able to mock DestroyCryptoKeyVersion`() {
        // language=JSON
        val json = """
            {
              "name":"name",
              "state":${CryptoKeyVersion.CryptoKeyVersionState.PENDING_IMPORT.number},
              "protectionLevel":${ProtectionLevel.HSM.number},
              "algorithm":${CryptoKeyVersion.CryptoKeyVersionAlgorithm.RSA_DECRYPT_OAEP_3072_SHA1.number},
              "importJob":"importJob",
              "importFailureReason":"importFailureReason",
              "generationFailureReason":"generationFailureReason",
              "externalDestructionFailureReason":"externalDestructionFailureReason"
            }
        """.trimIndent()

        kmsMock.stubFor(
            method("DestroyCryptoKeyVersion")
                .willReturn(GrpcResponseDefinitionBuilder(WireMockGrpc.Status.OK).fromJson(json)),
        )

        val response = client.destroyCryptoKeyVersion("keyId")
        assertEquals( "name", response.name)
        assertEquals( CryptoKeyVersion.CryptoKeyVersionState.PENDING_IMPORT, response.state)
        assertEquals( ProtectionLevel.HSM, response.protectionLevel)
        assertEquals( CryptoKeyVersion.CryptoKeyVersionAlgorithm.RSA_DECRYPT_OAEP_3072_SHA1, response.algorithm)
        assertEquals( "importJob", response.importJob)
        assertEquals( "importFailureReason", response.importFailureReason)
        assertEquals( "generationFailureReason", response.generationFailureReason)
        assertEquals( "externalDestructionFailureReason", response.externalDestructionFailureReason)
    }


}
