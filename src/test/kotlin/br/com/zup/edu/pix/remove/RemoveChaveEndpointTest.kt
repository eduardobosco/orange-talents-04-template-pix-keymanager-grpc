package br.com.zup.edu.pix.remove


import br.com.zup.edu.KeymanagerRemoveGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.externo.bcb.BancoCentralClient
import br.com.zup.edu.externo.bcb.DeletePixKeyRequest
import br.com.zup.edu.externo.bcb.DeletePixKeyResponse
import br.com.zup.edu.pix.ChavePix
import br.com.zup.edu.pix.ContaAssociada
import br.com.zup.edu.pix.TipoDeChave
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.pix.registra.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcCliente: KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub
){

    lateinit var CHAVE_EXISTENTE: ChavePix

    @Inject
    lateinit var bcbClient: BancoCentralClient;

    @BeforeEach
    fun setup(){
        CHAVE_EXISTENTE = repository.save(chave(
            tipoDeChave = TipoDeChave.EMAIL,
            chave = "rponte@gmail.com",
            clientId = UUID.randomUUID()
        ))
    }

//    @Test
//    fun `deve remover chave pix existente`() {
//        // cenário
//        `when`(bcbClient.delete("rponte@gmail.com", DeletePixKeyRequest("rponte@gmail.com")))
//            .thenReturn(
//                HttpResponse.ok(
//                    DeletePixKeyResponse(key = "rponte@gmail.com",
//                participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
//                deletedAt = LocalDateTime.now())
//                )
//            )
//
//        // ação
//        val response = grpcCliente.remove(RemoveChavePixRequest.newBuilder()
//            .setPixId(CHAVE_EXISTENTE.id.toString())
//            .setClientId(CHAVE_EXISTENTE.clientId.toString())
//            .build())
//
//        // validação
//        with(response) {
//            assertEquals(CHAVE_EXISTENTE.id.toString(), pixId)
//            assertEquals(CHAVE_EXISTENTE.clientId.toString(), clientId)
//        }
//    }

    @Test
    fun `nao deve remover chave pix quando chave inexistente`() {
        val chavePixDeferente = UUID.randomUUID().toString()

        // ação
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcCliente.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(chavePixDeferente)
                    .setClientId(CHAVE_EXISTENTE.clientId.toString())
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix nao cadastrada ou nao pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando chave existir mas pertencer a outro cliente`() {
        val outroClienteId = UUID.randomUUID().toString()

        // ação
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcCliente.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(CHAVE_EXISTENTE.id.toString())
                    .setClientId(outroClienteId)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix nao cadastrada ou nao pertence ao cliente", status.description)
        }
    }


    @Factory
    class Clients  {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRemoveGrpcServiceGrpc.KeymanagerRemoveGrpcServiceBlockingStub? {
            return KeymanagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    private fun chave(
        tipoDeChave: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clientId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clientId = clientId,
            tipoDeChave = tipoDeChave,
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