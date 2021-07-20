package br.com.zup.edu.pix.controllers

import br.com.zupedu.grpc.RemovePixRequest
import br.com.zupedu.grpc.RemovePixServiceGrpc
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.validation.Valid

@Controller("api/pix")
@Validated
class DeletePixController(
    @Inject private val removePixServiceGrpc: RemovePixServiceGrpc.RemovePixServiceBlockingStub
) {

    @Delete("/{idClient}")
    fun remove(@PathVariable idClient: UUID?,
               @Valid @Body request: RemoveRequestRest?) : HttpResponse<Any> {

        val removeResponse = removePixServiceGrpc.remove(createRequestRemovePix(request?.idPix, idClient))

        return HttpResponse.ok(removeResponse.status)
    }
}

fun createRequestRemovePix(idPix: UUID?, idClient: UUID?): RemovePixRequest =
    RemovePixRequest.newBuilder()
        .setIdClient(idClient.toString())
        .setIdPixKey(idPix.toString())
        .build()

@Introspected
data class RemoveRequestRest(
    val idPix: UUID?
)
