package br.com.zup.edu.pix.controllers

import br.com.zup.edu.grpc.KeyManagerGrpcFactory
import br.com.zupedu.grpc.*
import com.google.protobuf.Timestamp
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RetrieveListPixControllerTest {
    @field:Inject
    lateinit var retrieveListPixService : RetrieveAllPixServiceGrpc.RetrieveAllPixServiceBlockingStub

    @field:Inject
    @field:Client("/")
    lateinit var clientHttp: HttpClient

    companion object {
        val CLIENT_ID: UUID? = UUID.randomUUID()
        val PIX_ID : UUID? = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        Mockito.reset(retrieveListPixService)
    }

    @Test
    fun`should be retrieve a list key`(){
        //scenario
        `when`(retrieveListPixService.retrieveAll(Mockito.any()))
            .thenReturn(createResponseGrpc())

        //actions
        val retrieveListRequestRest = HttpRequest.GET<Any>("/api/pix/${CLIENT_ID}/keys")
        val responseRest = clientHttp.toBlocking().exchange(retrieveListRequestRest, List::class.java)

        //assertions
        with(responseRest) {
            assertEquals(HttpStatus.OK.code, status.code)
            assertNotNull(body())
            assertTrue(body().isNotEmpty())
        }
    }


    @Test
    fun`should not be retrieve when not found in server grpc`(){
        //scenario
        `when`(retrieveListPixService.retrieveAll(Mockito.any()))
            .thenThrow(StatusRuntimeException(Status.NOT_FOUND))

        //actions
        val retrieveListRequestRest = HttpRequest.GET<Any>("/api/pix/${CLIENT_ID}/keys")
        val assertThrows = org.junit.jupiter.api.assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(retrieveListRequestRest, List::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun`should not be retrieve when exception unknown in server grpc`(){
        //scenario
        `when`(retrieveListPixService.retrieveAll(Mockito.any()))
            .thenThrow(StatusRuntimeException(Status.UNKNOWN))

        //actions
        val retrieveListRequestRest = HttpRequest.GET<Any>("/api/pix/${CLIENT_ID}/keys")
        val assertThrows = org.junit.jupiter.api.assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(retrieveListRequestRest, List::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
        }
    }

    private fun createResponseGrpc(): RetrieveListReply? {
        val listItem = RetrieveListReply.ListPix.newBuilder()
            .setIdPix(PIX_ID.toString())
            .setTypeKey(TypeKey.EMAIL)
            .setValueKey("test@email.com")
            .setTypeAccount(TypeAccount.CHECKING_ACCOUNT)
            .setCreatedAt(run {
                val formattedDate = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"))
                Timestamp.newBuilder()
                    .setSeconds(formattedDate.toEpochSecond(ZoneOffset.UTC))
                    .setNanos(formattedDate.nano)
                    .build()
            })
            .build()

        return RetrieveListReply.newBuilder()
            .setIdClient(CLIENT_ID.toString())
            .addAllListPix(listOf(listItem))
            .build()
    }

    @Factory
    @Replaces(factory = KeyManagerGrpcFactory::class)
    class Clients {
        @Singleton
        fun retrieveListService() = Mockito.mock(RetrieveAllPixServiceGrpc.RetrieveAllPixServiceBlockingStub::class.java)
    }
}
