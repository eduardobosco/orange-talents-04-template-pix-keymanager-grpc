package br.com.zup.edu.pix

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoDeChave {

    CHAVE_ALEATORIA{
        override fun valida(chave: String?)= chave.isNullOrEmpty() //não deve se preenchida
    },
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrEmpty() || !chave.matches("[0-9]{11}".toRegex())){
                return false
            }

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    TEL_CELULAR{
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrEmpty()){
                return false
            }

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())
        }
    },
    EMAIL{
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrEmpty()){
                return false
            }

            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }

    };

    abstract fun valida(chave: String?): Boolean
}