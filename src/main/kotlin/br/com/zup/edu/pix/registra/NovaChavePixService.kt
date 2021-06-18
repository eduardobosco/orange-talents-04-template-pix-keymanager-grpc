package br.com.zup.edu.pix.registra

import br.com.zup.edu.errors.exceptions.ChavePixExistenteException
import br.com.zup.edu.externo.bcb.BancoCentralClient
import br.com.zup.edu.externo.bcb.CreatePixKeyRequest
import br.com.zup.edu.externo.itau.ErpItauClient
import br.com.zup.edu.pix.ChavePix
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val itauClient: ErpItauClient,
                          @Inject val bcbClient : BancoCentralClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix?): ChavePix {
        //Verifica se a chave ja existe no sistema
        if(repository.existsByChave(novaChavePix!!.chave)) {
            throw ChavePixExistenteException("Chave Pix '${novaChavePix.chave}' ja cadastrada")
        }

        //consulta no sistema do ERP do ITAU
        val erpItauResponse = itauClient.buscaContaPorTipo(novaChavePix.clientId, novaChavePix.tipoDeConta!!.name)
        val conta = erpItauResponse.body()?.toModel() ?: throw IllegalStateException("Cliente nao encontrado no itau")

        //salva no banco de dados
        val novaChave = novaChavePix.toModel(conta)
        repository.save(novaChave)

        //registra Chave no BCB
        val bcbRequest = CreatePixKeyRequest.of(novaChave).also {
            LOGGER.info("Registrando Chave Pix no Banco Central do Brasil (BCB) : $it")
        }

        val bcbResponse = bcbClient.create(bcbRequest)
        if(bcbResponse.status!=HttpStatus.CREATED)
            throw java.lang.IllegalStateException("Erro ao registrar chave Pix no Banco Central do Basil (BCB)")

        //atualiza chave do dominio com chave gerada pelo BCB
        novaChave.atualiza(bcbResponse.body()!!.key)

        return novaChave
    }
}