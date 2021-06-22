package br.com.zup.edu.pix.registra



import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.ContaAssociada
import br.com.zup.edu.pix.TipoDeChave
import br.com.zup.edu.pix.TipoDeChave.*
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.validation.ValidPixKey
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @field:NotBlank
    val clientId: String,

    @field:NotNull
    val tipoDeChave: TipoDeChave?,

    @field:NotNull
    val tipoDeConta: TipoDeConta?,

    @field:Size(max = 77)
    val chave: String
) {


    fun toModel(conta: ContaAssociada): ChavePix{
        return ChavePix(
            clientId = UUID.fromString(this.clientId),
            tipoDeChave = valueOf(this.tipoDeChave!!.name),
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
            //se o tipo da chave for aleatorio entao ira gerar uma chave aleatoria, senao ira pegar a chave que esta vindo
            chave = if(this.tipoDeChave == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
            conta = conta
        )
    }

}