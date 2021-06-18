package br.com.zup.edu.pix.remove

import br.com.zup.edu.errors.exceptions.ChavePixNaoEncontradaException
import br.com.zup.edu.errors.exceptions.PermissaoNegadaException
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
        @NotBlank @ValidUUID(message = "Cliente ID com formato invalido") clientId: String?,
        @NotBlank @ValidUUID(message = "Pix ID com formato invalido") pixId: String?
    ) {
        val uuidPixId = UUID.fromString(pixId)
        val uuidClientId = UUID.fromString(clientId)

        val chave = repository.findByPixId(uuidPixId)

        if (chave.isEmpty)
            throw ChavePixNaoEncontradaException("Chave Pix '${uuidPixId}' não existe")

        if (chave.get().clientId.toString() != uuidClientId.toString()) {
            throw PermissaoNegadaException("Cliente não tem permissão para apagar essa chave")
        }

        val request = DeletePixKeyRequest(chave.get().chave)

        val bcbResponse = bcbClient.delete(request = request, key = chave.get().chave)
        check(bcbResponse.status != HttpStatus.NOT_FOUND) { "Chave não existe" }
        check(bcbResponse.status != HttpStatus.FORBIDDEN) { "Não foi possivel cadastrar chave no BCB" }
        check(bcbResponse.status == HttpStatus.OK) { "Falha na remoção de chave no BCB" }

        repository.delete(chave.get())
    }

}
