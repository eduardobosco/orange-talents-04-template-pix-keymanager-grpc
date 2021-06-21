package br.com.zup.edu.pix.carrega

import br.com.zup.edu.CarregaChavePixResponse
import br.com.zup.edu.TipoDeChave
import br.com.zup.edu.pix.ChavePixInfo
import br.com.zup.edu.TipoDeChave.*
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.TipoDeConta.*
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixResponseConverter{

    fun converter(chaveInfo: ChavePixInfo): CarregaChavePixResponse{
        return CarregaChavePixResponse.newBuilder()
            .setClientId(chaveInfo.clienteId?.toString() ?: "")
            .setPixId(chaveInfo.pixId?.toString() ?: "") // Protobuf usa "" como default value para String
            .setChave(CarregaChavePixResponse.ChavePix // 1
                .newBuilder()
                .setTipoDeChave(TipoDeChave.valueOf(chaveInfo.tipoDeChave.name)) // 2
                .setChave(chaveInfo.chave)
                .setConta(CarregaChavePixResponse.ChavePix.ContaInfo.newBuilder() // 1
                    .setTipoDeConta(TipoDeConta.valueOf(chaveInfo.tipoDeConta.name)) // 2
                    .setInstituicao(chaveInfo.conta.instituicao) // 1 (Conta)
                    .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }

}