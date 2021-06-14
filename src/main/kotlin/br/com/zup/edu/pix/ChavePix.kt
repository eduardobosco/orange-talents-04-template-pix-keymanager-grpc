package br.com.zup.edu.pix

import br.com.zup.edu.validation.ValidUUID
import java.time.Instant
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
    val tipoDeChave: TipoDeChave,

    @field:NotBlank
    val chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoDeConta: TipoDeConta,

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

    @NotNull
    @Column(nullable = false)
    val criadoEm = Instant.now()

    override fun toString(): String {
        return "ChavePix(clientId=$clientId, tipoDaChave=$tipoDeChave, chave='$chave', tipoDaConta=$tipoDeConta, id=$id)"
    }
}