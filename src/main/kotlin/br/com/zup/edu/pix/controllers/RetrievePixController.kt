package br.com.zup.edu.pix.controllers

import br.com.zup.edu.pix.enums.TypeAccount
import br.com.zup.edu.pix.enums.TypeKey
import br.com.zupedu.grpc.RetrievePixReply
import br.com.zupedu.grpc.RetrievePixRequest
import br.com.zupedu.grpc.RetrievePixServiceGrpc
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
class RetrievePixController(
    @Inject private val retrievePixService: RetrievePixServiceGrpc.RetrievePixServiceBlockingStub
) {

    @Get("/details/{idPix}")
    fun retrieveKey(
        @PathVariable idClient: UUID,
        @PathVariable idPix: UUID
    ): MutableHttpResponse<DetailsResponse>? {
        val retrieveResponse = retrievePixService.retrieve(createRequestGrpc(idClient, idPix))

        createDetailsResponse(retrieveResponse)
        return HttpResponse.ok(createDetailsResponse(retrieveResponse))
    }
}

private fun createRequestGrpc(idClient: UUID, idPix: UUID) =
    RetrievePixRequest.newBuilder()
        .setIdPix(RetrievePixRequest.MessageInternal.newBuilder()
            .setIdClient(idClient.toString())
            .setIdPix(idPix.toString())
            .build())
        .build()

fun createDetailsResponse(retrieveResponse: RetrievePixReply): DetailsResponse =
    DetailsResponse(
        idPix = retrieveResponse.idPix,
        idClient = retrieveResponse.idClient,
        typeKey = TypeKey.valueOf(retrieveResponse.typeKey.name),
        valueKey = retrieveResponse.valueKey,
        ownerName = retrieveResponse.ownerName,
        ownerCPF = retrieveResponse.ownerCPF,
        institution = Institution(
            name = retrieveResponse.institution.name,
            branch = retrieveResponse.institution.branch,
            numberAccount = retrieveResponse.institution.numberAccount,
            typeAccount = TypeAccount.valueOf(retrieveResponse.institution.typeAccount.name)
        ),
        createdAt = LocalDateTime.ofEpochSecond(
            retrieveResponse.createdAt.seconds,
            retrieveResponse.createdAt.nanos,
            ZoneOffset.UTC
        ).toString()
    )

data class DetailsResponse(
    val idPix: String?,
    val idClient: String?,
    val typeKey: TypeKey?,
    val valueKey: String?,
    val ownerName: String?,
    val ownerCPF: String?,
    val institution: Institution?,
    val createdAt: String?,
)

data class Institution (
    val name: String?,
    val branch: String?,
    val numberAccount: String?,
    val typeAccount: TypeAccount?,
)
