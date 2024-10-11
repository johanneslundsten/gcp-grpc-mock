package app.lundsten.gcpmock

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GcpMockContainerTest {

    private var container: GcpMockContainer? = null

    @AfterEach
    fun stop() {
        container?.stop()
    }

    @Test
    fun `Should set wiremock version at start`() {
        container = GcpMockContainer()
        assertNull(container!!.wiremockVersion)
        container!!.start()
        assertNotNull(container!!.wiremockVersion)
    }

    @Test
    fun `Should be able to activate verbose`() {
        container = GcpMockContainer(verbose = true)
        container!!.start()

        assertNotNull(container!!.isVerbose)
        assertTrue(container!!.isVerbose == true)
    }

    @Test
    fun `Should have verbose false as default`() {
        container = GcpMockContainer()
        assertNull(container!!.isVerbose)
        container!!.start()
        assertNotNull(container!!.isVerbose)
        assertTrue(container!!.isVerbose == false)
    }

    @Test
    fun `Should be able to set wiremock config`() {
        container = GcpMockContainer(wiremockConfigs = listOf("--disable-gzip", "--global-response-templating"))
        container!!.start()

        val env = container!!.env
        assertEquals(1, env.size)
        val envVariable = env.first()
        assertTrue(envVariable.startsWith("WIREMOCK_OPTIONS="))
        val configs = envVariable.removePrefix("WIREMOCK_OPTIONS=").split(" ")
        assertEquals(2, configs.size)
        assertTrue(configs.contains("--disable-gzip"))
        assertTrue(configs.contains("--global-response-templating"))
    }

    @Test
    fun `Should only configure --verbose once`() {
        container = GcpMockContainer(verbose = true, wiremockConfigs = listOf("--verbose"))
        container!!.start()

        val env = container!!.env
        assertEquals(1, env.size)
        val envVariable = env.first()
        assertTrue(envVariable.startsWith("WIREMOCK_OPTIONS="))
        val configs = envVariable.removePrefix("WIREMOCK_OPTIONS=").split(" ")
        assertEquals(1, configs.size)
        assertTrue(configs.contains("--verbose"))
    }
}
