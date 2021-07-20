package br.com.zup.edu.pix.controllers

import br.com.zupedu.grpc.PixServiceGrpc
import br.com.zupedu.grpc.RegisterPixRequest
import br.com.zup.edu.pix.enums.TypeAccount
import br.com.zup.edu.pix.enums.TypeKey
import br.com.zup.edu.pix.validations.ValidPixKey
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.uri.UriBuilder
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Controller("api/pix")
@Validated
class CreatePixController(
    @Inject private val registerPixClient: PixServiceGrpc.PixServiceBlockingStub
) {

    @Post
    fun create(@Valid @Body pixKey: PixKeyRequest): MutableHttpResponse<Any>? {

        val registerResponse = registerPixClient.register(pixKey.requestRestToRequestGrpc())

        val uri = UriBuilder.of("api/pix/{idPix}")
            .expand(mutableMapOf(Pair("idPix", registerResponse.idPix)))

        return HttpResponse.created<Any?>(uri)
            .body(
                mutableMapOf(Pair("idClient", registerResponse.idClient),
                Pair("idPix", registerResponse.idPix)))
    }
}

@Introspected
@ValidPixKey
data class PixKeyRequest(
    @field:NotBlank
    val idClient: String?,
    @field:NotNull
    val typeKey: TypeKey?,
    @field:Size(max = 77)
    val valueKey: String?,
    @field:NotNull
    val typeAccount: TypeAccount?,
) {
    fun requestRestToRequestGrpc() =
        RegisterPixRequest.newBuilder()
            .setIdClient(idClient)
            .setTypeKey(br.com.zupedu.grpc.TypeKey.valueOf(typeKey!!.name))
            .setValueKey(valueKey)
            .setTypeAccount(br.com.zupedu.grpc.TypeAccount.valueOf(typeAccount!!.name))
            .build()
}
