package br.com.zup.edu.pix.controllers

import br.com.zup.edu.grpc.KeyManagerGrpcFactory
import br.com.zup.edu.pix.enums.TypeAccount
import br.com.zup.edu.pix.enums.TypeKey
import br.com.zupedu.grpc.*
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CreatePixControllerTest {

    @field:Inject
    lateinit var registerPixService : PixServiceGrpc.PixServiceBlockingStub

    @field:Inject
    @field:Client("/")
    lateinit var clientHttp: HttpClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
        val PIX_ID = UUID.randomUUID()
    }

    @Test
    fun`should be register a new key`(){
        //scenario
        val registerResponse = RegisterPixReply.newBuilder()
            .setIdClient(CLIENT_ID.toString())
            .setIdPix(PIX_ID.toString())
            .build()

        given(registerPixService.register(Mockito.any()))
            .willReturn(registerResponse)

        //actions
        val requestRest = HttpRequest.POST("/api/pix", createRequestRest())
        val responseRest = clientHttp.toBlocking().exchange(requestRest, PixKeyRequest::class.java)

        //assertions
        with(responseRest) {
            assertEquals(HttpStatus.CREATED.code, status.code)
            assertTrue(headers.contains("Location"))
            assertTrue(header("Location")!!.contains(PIX_ID.toString()))
        }
    }

    @Test
    fun`should not be register when type key is incompatible with value key`(){
        //scenario
        val requestInvalid = PixKeyRequest(
            idClient = CLIENT_ID.toString(),
            typeKey = TypeKey.CPF,
            valueKey = "invalid_value@email.com",
            typeAccount = TypeAccount.CHECKING_ACCOUNT
        )

        //actions
        val requestRest = HttpRequest.POST("/api/pix", requestInvalid)
        val assertThrows = assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(requestRest, PixKeyRequest::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
            assertEquals("pixKey.key: Key Pix invalid 'CPF'", message)
        }
    }

    // errors handler grpc

    /*@Test
    fun`should be validate exception unknown grpc`(){
        //scenario
        given(registerPixService.register(Mockito.any()))
            .willThrow(StatusRuntimeException(Status.UNKNOWN))

        //actions
        val requestRest = HttpRequest.POST("/api/pix", createRequestRest())
        val assertThrows = assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(requestRest, PixKeyRequest::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
        }
    }*/

    @Test
    fun`should be validate exception already exist grpc`(){
        //scenario
        given(registerPixService.register(Mockito.any()))
            .willThrow(StatusRuntimeException(Status.ALREADY_EXISTS))

        //actions
        val requestRest = HttpRequest.POST("/api/pix", createRequestRest())
        val assertThrows = assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(requestRest, PixKeyRequest::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.code, status.code)
        }
    }

    private fun createRequestRest() =
        PixKeyRequest(
            idClient = CLIENT_ID.toString(),
            typeKey = TypeKey.EMAIL,
            valueKey = "test@email.com",
            typeAccount = TypeAccount.CHECKING_ACCOUNT
        )

    @Factory
    @Replaces(factory = KeyManagerGrpcFactory::class)
    internal class Clients {
        @Singleton
        fun stubMock() = Mockito.mock(PixServiceGrpc.PixServiceBlockingStub::class.java)
    }
}
