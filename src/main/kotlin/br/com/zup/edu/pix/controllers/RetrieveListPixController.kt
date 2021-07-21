package br.com.zup.edu.pix.controllers

import br.com.zup.edu.pix.enums.TypeAccount
import br.com.zup.edu.pix.enums.TypeKey
import br.com.zupedu.grpc.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject

@Controller("/api/pix/{idClient}")
class RetrieveListPixController(
    @Inject private val retrieveListPixService: RetrieveAllPixServiceGrpc.RetrieveAllPixServiceBlockingStub
) {

    @Get("/keys")
    fun retrieveKey(
        @PathVariable idClient: UUID,
    ): MutableHttpResponse<Any>? {
        val retrieveListResponse = retrieveListPixService.retrieveAll(createRequestListGrpc(idClient))
        return HttpResponse.ok(createListKeysResponse(retrieveListResponse))
    }
}

private fun createRequestListGrpc(idClient: UUID?) =
    RetrieveListRequest.newBuilder()
        .setIdClient(idClient.toString())
        .build()

fun createListKeysResponse(retrieveResponse: RetrieveListReply)  =
    retrieveResponse.listPixList
        .map { pix ->
            ListKeysResponse(
                idPix = pix.idPix,
                typeKey = TypeKey.valueOf(pix.typeKey.name),
                valueKey = pix.valueKey,
                typeAccount = TypeAccount.valueOf(pix.typeAccount.name),
                createdAt = LocalDateTime.ofEpochSecond(
                    pix.createdAt.seconds,
                    pix.createdAt.nanos,
                    ZoneOffset.UTC
                ).toString()
            )
        }

data class ListKeysResponse(
    val idPix: String?,
    val typeKey: TypeKey?,
    val valueKey: String?,
    val typeAccount: TypeAccount?,
    val createdAt: String?,
)
