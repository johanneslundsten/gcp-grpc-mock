package app.lundsten.gcpmock

import org.junit.jupiter.api.AfterEach
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
}
