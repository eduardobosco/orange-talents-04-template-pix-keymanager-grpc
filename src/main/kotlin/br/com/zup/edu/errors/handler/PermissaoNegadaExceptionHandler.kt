package br.com.zup.edu.errors.handler

import br.com.zup.edu.errors.exceptions.PermissaoNegadaException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class PermissaoNegadaExceptionHandler : ExceptionHandler<PermissaoNegadaException> {
    override fun handle(e: PermissaoNegadaException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.PERMISSION_DENIED
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is PermissaoNegadaException
    }

}