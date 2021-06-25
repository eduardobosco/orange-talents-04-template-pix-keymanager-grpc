package br.com.zup.edu.pix.lista

import br.com.zup.edu.KeymanagerListaGrpcServiceGrpc
import br.com.zup.edu.ListaChavePixRequest
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.ContaAssociada
import br.com.zup.edu.pix.TipoDeChave
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.pix.registra.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
/**
 * TIP: Necessario desabilitar o controle transacional (transactional=false) pois o gRPC Server
 * roda numa thread separada, caso contrário não será possível preparar cenário dentro do método @Test
 */
@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub,
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoDeChave.EMAIL, chave = "rafael.ponte@zup.com.br", clientId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "randomkey-2", clientId = UUID.randomUUID()))
        repository.save(chave(tipo = TipoDeChave.ALEATORIA, chave = "randomkey-3", clientId = CLIENTE_ID))
    }

    /**
     * TIP: por padrão roda numa transação isolada
     */
    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

//    @Test
//    fun `deve listar todas as chaves do cliente`() {
//        // cenário
//        val clientId = CLIENTE_ID.toString()
//
//        // ação
//        val response = grpcClient.lista(ListaChavePixRequest.newBuilder()
//            .setClientId(clientId)
//            .build())
//
//        // validação
//        with (response.chavesList) {
//            assertThat(this, hasSize(2))
//            assertThat(
//                this.map { Pair(it.tipo, it.chave) }.toList(),
//                containsInAnyOrder(
//                    Pair(TipoDeChave.ALEATORIA, "randomkey-3"),
//                    Pair(TipoDeChave.EMAIL, "rafael.ponte@zup.com.br")
//                )
//            )
//        }
//    }
    @Test
    fun `nao deve listar as chaves do cliente quando cliente nao possuir chaves`() {
        // cenário
        val clienteSemChaves = UUID.randomUUID().toString()

        // ação
        val response = grpcClient.lista(ListaChavePixRequest.newBuilder()
            .setClientId(clienteSemChaves)
            .build())

        // validação
        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `nao deve listar todas as chaves do cliente quando clienteId for invalido`() {
        // cenário
        val clienteIdInvalido = ""

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.lista(ListaChavePixRequest.newBuilder()
                .setClientId(clienteIdInvalido)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerListaGrpcServiceGrpc.KeymanagerListaGrpcServiceBlockingStub? {
            return KeymanagerListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clientId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clientId,
            tipoDeChave = tipo,
            chave = chave,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}
