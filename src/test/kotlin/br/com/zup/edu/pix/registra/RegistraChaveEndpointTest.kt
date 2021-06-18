package br.com.zup.edu.pix.registra

import br.com.zup.edu.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.externo.itau.ErpItauClient
import br.com.zup.edu.TipoDeChave
import br.com.zup.edu.TipoDeConta

import br.com.zup.edu.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
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
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub
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

    @Test
    fun `nao deve registrar chave pix quando chave existente`() {
        // cenário
        repository.save(chave(
            tipo = TipoDeChave.CPF,
            chave = "63657520325",
            clientId = CLIENTE_ID
        ))

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClientId(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("63657520325")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '63657520325' ja cadastrada", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`() {
        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClientId(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("rponte@gmail.com")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente nao encontrado no itau", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("Dados invalidos", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos - chave invalida`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClientId(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("378.930.cpf-invalido.389-73")
                .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
                .build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
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
                KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub? {
            return KeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
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

    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clientId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clientId,
            tipoDeChave = br.com.zup.edu.pix.TipoDeChave.CHAVE_ALEATORIA,
            chave = chave,
            tipoDeConta = br.com.zup.edu.pix.TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}
