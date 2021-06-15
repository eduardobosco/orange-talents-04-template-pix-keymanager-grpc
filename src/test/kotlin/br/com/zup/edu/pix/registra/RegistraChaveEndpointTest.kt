package br.com.zup.edu.pix.registra

import br.com.zup.edu.KeyManagerGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.externo.itau.ErpItauClient
import br.com.zup.edu.TipoDeChave
import br.com.zup.edu.TipoDeConta

import br.com.zup.edu.pix.*
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {


    @Inject
    lateinit var itauClient: ErpItauClient;

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    // TODO: escrever cenários de exceptions na chamada do serviços satelites?

    @Test
    fun `deve registrar nova chave pix`() {
        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))


        // ação
        val response = grpcClient.registra(RegistraChavePixRequest.newBuilder()
            .setClientId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("rponte@gmail.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build())

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clientId)
            assertNotNull(pixId)
        }
    }

    @MockBean(ErpItauClient::class)
    fun itauClient(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @Factory
    class Clients  {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub? {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse(id = CLIENTE_ID, "Rafael Ponte", "63657520325")
        )
    }
}
