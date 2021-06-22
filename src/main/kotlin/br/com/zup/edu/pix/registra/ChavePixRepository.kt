package br.com.zup.edu.pix.registra

import br.com.zup.edu.pix.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface ChavePixRepository : CrudRepository<ChavePix, UUID> {
    fun existsByChave(chave: String): Boolean

    fun findByPixId(pixId: UUID): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

    fun findAllByClientId(clientId: UUID): List<ChavePix>

}