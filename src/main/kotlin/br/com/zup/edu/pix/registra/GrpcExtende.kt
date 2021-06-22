package br.com.zup.edu.pix.registra

import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoDeChave.UNKNOWN_TIPO_CHAVE
import br.com.zup.edu.TipoDeConta.UNKNOWN_TIPO_CONTA
import br.com.zup.edu.pix.TipoDeChave
import br.com.zup.edu.TipoDeConta

fun RegistraChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clientId = clientId,
        tipoDeChave = when (tipoDeChave) {
            UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            UNKNOWN_TIPO_CONTA -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}