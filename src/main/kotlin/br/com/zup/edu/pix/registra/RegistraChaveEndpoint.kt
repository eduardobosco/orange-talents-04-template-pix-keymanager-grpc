package br.com.zup.edu.pix.registra


import br.com.zup.edu.KeymanagerRegistraGrpcServiceGrpc
import br.com.zup.edu.ListaChavePixResponse
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.validation.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChaveEndpoint(
    @Inject private val service: NovaChavePixService
) : KeymanagerRegistraGrpcServiceGrpc.KeymanagerRegistraGrpcServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<ListaChavePixResponse>
    ) {
        val novaChave = request.toModel()
        val chaveCriada = service.registra(novaChave)

        responseObserver.onNext(
            ListaChavePixResponse.newBuilder()
                .setClientId(chaveCriada.clientId.toString())
                .setPixId(chaveCriada.pixId.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}