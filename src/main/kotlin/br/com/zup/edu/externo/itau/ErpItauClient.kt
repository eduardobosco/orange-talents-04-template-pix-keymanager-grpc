package br.com.zup.edu.externo.itau

import br.com.zup.edu.pix.DadosDaContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("\${itau.contas.url}")
interface ErpItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun buscaContaPorTipo(@PathVariable clienteId:String, @QueryValue tipo:String)
    : HttpResponse<DadosDaContaResponse>
}