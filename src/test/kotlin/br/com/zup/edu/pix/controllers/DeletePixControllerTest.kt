package br.com.zup.edu.pix.controllers

import br.com.zup.edu.grpc.KeyManagerGrpcFactory
import br.com.zupedu.grpc.RemovePixReply
import br.com.zupedu.grpc.RemovePixServiceGrpc
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
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeletePixControllerTest {

    @field:Inject
    lateinit var removePixService : RemovePixServiceGrpc.RemovePixServiceBlockingStub

    @field:Inject
    @field:Client("/")
    lateinit var clientHttp: HttpClient

    @BeforeEach
    internal fun setUp() {
        Mockito.reset(removePixService)
    }

    companion object {
        val CLIENT_ID = UUID.randomUUID()
        val PIX_ID = UUID.randomUUID()
    }

    @Test
    fun`should be removed a key`(){
        //scenario
        val removeResponse = RemovePixReply.newBuilder()
            .setStatus("Removed").build()

        given(removePixService.remove(Mockito.any()))
            .willReturn(removeResponse)

        //actions
        val requestRest = HttpRequest.DELETE("/api/pix/${CLIENT_ID}", createRemovePixRest())
        val responseHttp = clientHttp.toBlocking().exchange(requestRest, RemoveRequestRest::class.java)

        //assertions
        with(responseHttp) {
            assertEquals(HttpStatus.OK.code, status.code)
        }
    }

    @Test
    fun`should not be removed when a key not found on system grpc`(){
        //scenario
        given(removePixService.remove(Mockito.any()))
            .willThrow(StatusRuntimeException(Status.NOT_FOUND))

        //actions
        val requestRest = HttpRequest.DELETE("/api/pix/${CLIENT_ID}", createRemovePixRest())
        val assertThrows = assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(requestRest, RemoveRequestRest::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun`should not be removed when a key was invalid data on system grpc`(){
        //scenario
        given(removePixService.remove(Mockito.any()))
            .willThrow(StatusRuntimeException(Status.INVALID_ARGUMENT))

        //actions
        val requestRest = HttpRequest.DELETE("/api/pix/${CLIENT_ID}", createRemovePixRest())
        val assertThrows = assertThrows<HttpClientResponseException> {
            clientHttp.toBlocking().exchange(requestRest, RemoveRequestRest::class.java)
        }

        //assertions
        with(assertThrows) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
        }
    }

    private fun createRemovePixRest() =
        RemoveRequestRest(idPix = PIX_ID)

    @Factory
    @Replaces(factory = KeyManagerGrpcFactory::class)
    internal class Clients {
        @Singleton
        fun removeStubMock() = Mockito.mock(RemovePixServiceGrpc.RemovePixServiceBlockingStub::class.java)
    }
}