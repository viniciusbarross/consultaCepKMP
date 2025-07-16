package br.edu.utfpr.consultacep.shared.data.validator

class CepValidator {

    fun verificarCep(cep: String): Boolean {
        val cepLimpo = cep.replace("-", "").replace(" ", "")

        if (cepLimpo.length != 8) return false

        return cepLimpo.all { it.isDigit() }
    }
}