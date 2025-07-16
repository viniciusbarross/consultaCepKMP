package br.edu.utfpr.consultacep.ui

import br.edu.utfpr.consultacep.shared.data.model.Endereco

data class CepFormState(
    val endereco: Endereco = Endereco(),
    val isDataValid: Boolean = false,
    val isLoading: Boolean = false,
    val hasErrorLoading: Boolean = false
)


