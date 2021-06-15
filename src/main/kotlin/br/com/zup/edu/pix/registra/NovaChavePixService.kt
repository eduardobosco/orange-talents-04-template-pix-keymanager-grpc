package br.com.zup.edu.pix.registra

import br.com.zup.edu.errors.exceptions.ChavePixExistenteException
import br.com.zup.edu.externo.itau.ErpItauClient
import br.com.zup.edu.pix.ChavePix
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(@Inject val repository: ChavePixRepository,
                          @Inject val erpItau: ErpItauClient
) {

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix?): ChavePix {
        //Verifica se a chave ja existe no sistema
        if(repository.existsByChave(novaChavePix!!.chave)) {
            throw ChavePixExistenteException("Chave '${novaChavePix!!.chave}' ja cadastrada")
        }

        //consulta no sistema do ERP do ITAU
        val erpItauResponse = erpItau.buscaContaPorTipo(novaChavePix?.clientId!!, novaChavePix.tipoDeConta!!.name)
        val conta = erpItauResponse.body()?.toModel() ?: throw IllegalStateException("Cliente nao encontrado no itau")

        //salva no banco de dados
        val novaChave = novaChavePix.toModel(conta)
        repository.save(novaChave)

        return novaChave
    }
}