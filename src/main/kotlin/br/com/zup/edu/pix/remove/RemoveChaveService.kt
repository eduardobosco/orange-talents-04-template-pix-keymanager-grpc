package br.com.zup.edu.pix.remove

import br.com.zup.edu.errors.exceptions.ChavePixNaoEncontradaException
import br.com.zup.edu.pix.registra.ChavePixRepository
import br.com.zup.edu.validation.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(@Inject val repository: ChavePixRepository) {

    @Transactional
    fun remove(
        @NotBlank
        @ValidUUID(message = "Cliente ID com formato invalido") clientId: String?,

        @NotBlank
        @ValidUUID(message = "pix ID com formato inválido") pixId: String?,

        ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString((clientId))

        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow{ChavePixNaoEncontradaException("Chave Pix não encontrada ou não pertence ao cliente")}

        repository.deleteById(uuidPixId)

    }

}
