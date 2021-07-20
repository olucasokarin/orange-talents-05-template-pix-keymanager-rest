package br.com.zup.edu.pix.controllers

import br.com.zup.edu.grpc.KeyManagerGrpcFactory
import br.com.zupedu.grpc.RetrievePixReply
import br.com.zupedu.grpc.RetrievePixServiceGrpc
import br.com.zupedu.grpc.TypeAccount
import br.com.zupedu.grpc.TypeKey
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
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RetrievePixControllerTest {

    @field:Inject
    lateinit var retrievePixService : RetrievePixServiceGrpc.RetrievePixServiceBlockingStub

    @field:Inject
    @field:Client("/")
    lateinit var clientHttp: HttpClient

    companion object {
        val CLIENT_ID: UUID? = UUID.randomUUID()
        val PIX_ID: UUID? = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        Mockito.reset(retrievePixService)
    }

    @Test
    fun`should be retrieve a key`(){
        //scenario
        `when`(retrievePixService.retrieve(Mockito.any()))
            .thenReturn(createRetrieveResponseGrpc())

        //actions
        val deleteRequestRest = HttpRequest.GET<Any>("/api/pix/${CLIENT_ID}/details/${PIX_ID}")
        val responseRest = clientHttp.toBlocking().exchange(deleteRequestRest, Any::class.java)

        //assertions
        with(responseRest) {
            assertEquals(HttpStatus.OK.code, status.code)
            assertNotNull(body())
        }
    }

    @Test
    fun`should not be retrieve when not found in server grpc`(){
        //scenario
        given(retrievePixService.retrieve(Mockito.any()))
            .willThrow(StatusRuntimeException(Status.NOT_FOUND))

        //actions
        val deleteRequestRest = HttpRequest.GET<Any>("/api/pix/${CLIENT_ID}/details/${PIX_ID}")
        val assertThrows = assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(deleteRequestRest, Any::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun`should not be retrieve when exception unknown in server grpc`(){
        //scenario
        given(retrievePixService.retrieve(Mockito.any()))
            .willThrow(StatusRuntimeException(Status.UNKNOWN))

        //actions
        val deleteRequestRest = HttpRequest.GET<Any>("/api/pix/${CLIENT_ID}/details/${PIX_ID}")
        val assertThrows = assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(deleteRequestRest, Any::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
        }
    }

    private fun createRetrieveResponseGrpc() =
        RetrievePixReply.newBuilder()
            .setIdClient(CLIENT_ID.toString())
            .setIdPix(PIX_ID.toString())
            .setValueKey("test@email.com")
            .setTypeKey(TypeKey.EMAIL)
            .setInstitution(RetrievePixReply.Institution.newBuilder().setTypeAccount(TypeAccount.CHECKING_ACCOUNT).build())
            .build()

    @Factory
    @Replaces(factory = KeyManagerGrpcFactory::class)
    class Clients {
        @Singleton
        fun retrievePixService() = Mockito.mock(RetrievePixServiceGrpc.RetrievePixServiceBlockingStub::class.java)
    }
}
