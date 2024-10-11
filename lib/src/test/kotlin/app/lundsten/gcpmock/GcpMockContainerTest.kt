package app.lundsten.gcpmock

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container

class GcpMockContainerTest {

    companion object {
        @JvmStatic
        @Container
        private val container = GcpMockContainer()
    }

    @AfterEach
    fun stop() {
        container.stop()
    }

    @BeforeEach
    fun asserContainerIsNotRunning() {
        assertFalse(container.isRunning)
    }

    @Test
    fun `Should set wiremock version at start`() {
        assertNull(container.wiremockVersion)
        container.start()
        assertNotNull(container.wiremockVersion)
    }

    @Test
    fun `Should be able to activate verbose`() {
        container.activateVerboseLogging()
        container.start()

        assertNotNull(container.isVerbose)
        assertTrue(container.isVerbose == true)
    }

    @Test
    fun `Should not have verbose false as default`() {
        assertNull(container.isVerbose)
        container.start()
        assertNotNull(container.isVerbose)
        assertTrue(container.isVerbose == false)
    }

    @Test
    fun `Should not be able to activate verbose on a started container`() {
        container.start()
        val illegalStateException = assertThrows<IllegalStateException> { container.activateVerboseLogging() }
        assertEquals("Container is running", illegalStateException.message)
    }
}
