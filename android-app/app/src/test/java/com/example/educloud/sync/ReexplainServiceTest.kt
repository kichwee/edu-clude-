package com.example.educloud.sync

import com.example.educloud.content.ContentLesson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress

private fun fakeLesson() = ContentLesson(
    id = "g3-t1-w2-l1-counting-twos",
    topic = "Counting in twos",
    keywords = setOf("twos"),
    source = "Grade 3 Mathematics Pupils Book",
    version = "grade3-rule-based-demo-0.4",
    passage = "Count in twos: 2, 4, 6, 8.",
    teachingSteps = listOf("Start at two", "Add two each time"),
    definition = "Counting in twos means adding two each time.",
    verifiedAnswer = "2, 4, 6, 8",
)

private fun payload(domain: String = "sports") = ReexplainRequestPayload(
    consent = true,
    learnerId = "c0a80101-7a7c-4e10-9e2a-f4f1ef2d6a01",
    lessonId = "g3-t1-w2-l1-counting-twos",
    analogyDomain = domain,
    sourceExcerpt = "Count in twos: 2, 4, 6, 8.",
    verifiedAnswer = "2, 4, 6, 8",
    learnerQuestion = "Why do we add two each time?",
)

/** Minimal loopback HTTP fixture; the service accepts cleartext on 127.0.0.1 for local dev only. */
private class LoopbackServer {
    private val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    private val requests = mutableListOf<String>()
    var handler: (HttpExchange) -> Unit = { exchange ->
        exchange.requestBody.readBytes()
        val body = (
            "{\"status\":\"ready\",\"lesson_id\":\"g3-t1-w2-l1-counting-twos\"," +
                "\"explanation\":\"Jump two goals each match: 2, 4, 6, 8 in twos!\"," +
                "\"preserves_verified_answer\":true,\"cached\":false,\"provider\":\"fixture\"}"
            ).toByteArray()
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(200, body.size.toLong())
        exchange.responseBody.use { it.write(body) }
    }

    init {
        server.createContext("/") { exchange ->
            requests.add(exchange.requestBody.readBytes().decodeToString())
            try {
                handler(exchange)
            } finally {
                exchange.close()
            }
        }
    }

    fun start() = server.start()
    fun stop() = server.stop(0)
    fun baseUrl() = "http://127.0.0.1:${server.address.port}"
    fun lastRequestBody() = requests.last()
}

class ReexplainServiceTest {

    private lateinit var server: LoopbackServer

    @Before
    fun setUp() {
        server = LoopbackServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.stop()
    }

    @Test
    fun `ready response is parsed and the bounded request is sent`() = runBlocking {
        val service = ReexplainService(server.baseUrl())

        val outcome = service.reexplain(payload())

        assertTrue(outcome is ReexplainOutcome.Ready)
        assertEquals(
            "Jump two goals each match: 2, 4, 6, 8 in twos!",
            (outcome as ReexplainOutcome.Ready).payload.explanation,
        )
        val sent = server.lastRequestBody()
        assertTrue("\"consent\":true" in sent.replace(" ", ""))
        assertTrue("\"lesson_id\":\"g3-t1-w2-l1-counting-twos\"" in sent.replace(" ", ""))
        assertTrue("\"analogy_domain\":\"sports\"" in sent.replace(" ", ""))
    }

    @Test
    fun `bad request maps to rejected`() = runBlocking {
        server.handler = { exchange ->
            exchange.sendResponseHeaders(400, -1)
        }
        val outcome = ReexplainService(server.baseUrl()).reexplain(payload())
        assertEquals(ReexplainOutcome.Rejected, outcome)
    }

    @Test
    fun `service disabled and throttled and failed all map to unavailable`() = runBlocking {
        listOf(404, 429, 500, 503).forEach { code ->
            server.handler = { exchange -> exchange.sendResponseHeaders(code, -1) }
            val outcome = ReexplainService(server.baseUrl()).reexplain(payload())
            assertEquals("HTTP $code", ReexplainOutcome.Unavailable, outcome)
        }
    }

    @Test
    fun `malformed or non-ready bodies are rejected not trusted`() = runBlocking {
        server.handler = { exchange ->
            val body = "not json at all".toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        assertEquals(ReexplainOutcome.Rejected, ReexplainService(server.baseUrl()).reexplain(payload()))

        server.handler = { exchange ->
            val body = (
                "{\"status\":\"unavailable\",\"lesson_id\":\"g3-t1-w2-l1-counting-twos\",\"explanation\":\"x\"}"
                ).toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        assertEquals(ReexplainOutcome.Unavailable, ReexplainService(server.baseUrl()).reexplain(payload()))
    }

    @Test
    fun `unknown response keys violate the strict shape and are rejected`() = runBlocking {
        server.handler = { exchange ->
            val body = (
                "{\"status\":\"ready\",\"lesson_id\":\"g3-t1-w2-l1-counting-twos\",\"explanation\":\"ok explanation here\"," +
                    "\"surprise_field\":true}"
                ).toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        val outcome = ReexplainService(server.baseUrl()).reexplain(payload())
        assertEquals(ReexplainOutcome.Rejected, outcome)
    }

    @Test
    fun `connection refused maps to retry`() = runBlocking {
        server.stop()
        // Port reuse race is acceptable here: an unbound ephemeral port refuses connections.
        val closedPortServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val port = closedPortServer.address.port
        closedPortServer.stop(0)

        val outcome = ReexplainService("http://127.0.0.1:$port").reexplain(payload())
        assertEquals(ReexplainOutcome.Retry, outcome)
    }

    @Test
    fun `non-https non-loopback endpoints are disabled without any network call`() = runBlocking {
        assertEquals(ReexplainOutcome.Disabled, ReexplainService("example.com").reexplain(payload()))
        assertEquals(ReexplainOutcome.Disabled, ReexplainService("http://insecure.example.com").reexplain(payload()))
    }
}

class InterestSanitizerTest {
    @Test
    fun `chips are trimmed filtered capped and deduplicated`() {
        val sanitized = sanitizeInterestDomains(
            listOf(" sports ", "astronomy", "music", "games", "nature", "sports"),
        )
        assertEquals(listOf("sports", "music", "games"), sanitized)
    }

    @Test
    fun `empty input yields no chips`() {
        assertTrue(sanitizeInterestDomains(emptyList()).isEmpty())
    }
}

class ServerExplanationValidationTest {

    private val lesson = fakeLesson()

    private fun serverPayload(
        lessonId: String = lesson.id,
        explanation: String = "Jump two goals each match: 2, 4, 6, 8 in twos!",
        status: String = "ready",
        preserves: Boolean = true,
    ) = ReexplainResponsePayload(
        status = status,
        lessonId = lessonId,
        explanation = explanation,
        preservesVerifiedAnswer = preserves,
    )

    @Test
    fun `a compliant server explanation passes the shipped policy`() {
        val result = validatedServerExplanation(serverPayload(), lesson)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.contains("4"))
    }

    @Test
    fun `a changed verified answer fails even if the server claims preservation`() {
        val result = validatedServerExplanation(
            serverPayload(explanation = "Score two points each round with your friends!"),
            lesson,
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `a wrong lesson echo fails`() {
        val result = validatedServerExplanation(serverPayload(lessonId = "some-other-lesson"), lesson)
        assertTrue(result.isFailure)
    }

    @Test
    fun `an over-long explanation fails`() {
        val longExplanation = List(60) { "word$it" }.joinToString(" ")
        val result = validatedServerExplanation(serverPayload(explanation = longExplanation), lesson)
        assertTrue(result.isFailure)
    }

    @Test
    fun `non-ready status or missing preservation flag fails`() {
        assertTrue(validatedServerExplanation(serverPayload(status = "unavailable"), lesson).isFailure)
        assertTrue(validatedServerExplanation(serverPayload(preserves = false), lesson).isFailure)
    }

    @Test
    fun `a missing payload fails safely`() {
        assertTrue(validatedServerExplanation(null, lesson).isFailure)
    }
}
