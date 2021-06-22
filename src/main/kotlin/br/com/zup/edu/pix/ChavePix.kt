package br.com.zup.edu.pix

import br.com.zup.edu.pix.TipoDeChave
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.validation.ValidUUID
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull


@Entity
class ChavePix(
    @ValidUUID
    @field:NotNull
    val clientId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeChave: br.com.zup.edu.pix.TipoDeChave,

    @field:NotBlank
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeConta: br.com.zup.edu.pix.TipoDeConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
)
{

    @Id
    @GeneratedValue
    val id: UUID? = null

    @NotNull
    @Column(nullable = false)
    val pixId = UUID.randomUUID()

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clientId=$clientId, tipoDeChave=$tipoDeChave, chave='$chave', tipoDaConta=$tipoDeConta, id=$id)"
    }

    fun isAleatoria(): Boolean = tipoDeChave == TipoDeChave.ALEATORIA

    fun pertenceAo(clienteId: UUID) = this.clientId == clienteId

    fun atualiza(key: String): Boolean {
        if(isAleatoria()){
            this.chave = key
            return true
        }
        return false
    }
}