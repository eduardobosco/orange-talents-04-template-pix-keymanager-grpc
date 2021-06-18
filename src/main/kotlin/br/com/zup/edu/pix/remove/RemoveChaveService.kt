package br.com.zup.edu.pix.remove

import br.com.zup.edu.errors.exceptions.ChavePixNaoEncontradaException
import br.com.zup.edu.externo.bcb.BancoCentralClient
import br.com.zup.edu.externo.bcb.DeletePixKeyRequest
import br.com.zup.edu.pix.registra.ChavePixRepository
import br.com.zup.edu.validation.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(@Inject val repository: ChavePixRepository,
                         @Inject val bcbClient : BancoCentralClient

) {

    @Transactional
    fun remove(
        @NotBlank
        @ValidUUID(message = "Cliente ID com formato invalido") clientId: String?,

        @NotBlank
        @ValidUUID(message = "pix ID com formato inválido") pixId: String?,

        ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString((clientId))

        val chave = repository.findByIdAndClientId(uuidPixId, uuidClienteId)
            .orElseThrow{ChavePixNaoEncontradaException("Chave pix nao cadastrada ou nao pertence ao cliente")}

        repository.deleteById(uuidPixId)

        val request = DeletePixKeyRequest(chave.chave)

        val bcbResponse = bcbClient.delete(key = chave.chave, request = request)
        if (bcbResponse.status != HttpStatus.OK){
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil (BCB)")
        }

    }

}
