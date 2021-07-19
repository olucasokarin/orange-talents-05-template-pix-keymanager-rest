package br.com.zup.edu.pix.controllers

import br.com.zup.edu.pix.enums.TypeAccount
import br.com.zup.edu.pix.enums.TypeKey
import br.com.zup.edu.pix.validations.ValidPixKey
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.validation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Controller("api/pix")
@Validated
class CreatePixController {

    @Post
    fun create(@Valid @Body pixKey: PixKeyRequest) {

        println(pixKey)

    }
}

@Introspected
@ValidPixKey
data class PixKeyRequest(
    @field:NotBlank
    val idClient: String,
    @field:NotNull
    val typeKey: TypeKey,
    @field:Size(max = 77)
    val valueKey: String?,
    @field:NotNull
    val typeAccount: TypeAccount,
)
