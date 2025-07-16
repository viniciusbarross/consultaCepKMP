package br.edu.utfpr.consultacep.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CepScreen(
    viewModel: CepViewModel = viewModel(factory = CepViewModelFactory())
) {
    val formState by viewModel.formState.observeAsState(CepFormState())
    var cepText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar snackbar quando há erro
    LaunchedEffect(formState.hasErrorLoading) {
        if (formState.hasErrorLoading) {
            snackbarHostState.showSnackbar(
                message = "Erro ao consultar CEP. Verifique o CEP e tente novamente.",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Consulta CEP") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo de entrada do CEP
            OutlinedTextField(
                value = cepText,
                onValueChange = { newValue ->
                    // Formatação automática do CEP
                    val filteredValue = newValue.replace("\\D".toRegex(), "")
                    var formattedValue = ""
                    
                    filteredValue.mapIndexed { index, char ->
                        if (index == 5) {
                            formattedValue += "-"
                        }
                        if (index <= 7) {
                            formattedValue += char
                        }
                    }
                    
                    cepText = formattedValue
                    viewModel.onCepChanged(formattedValue)
                },
                label = { Text("CEP") },
                placeholder = { Text("00000-000") },
                enabled = !formState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (formState.isDataValid) {
                            viewModel.buscarCep(cepText)
                            keyboardController?.hide()
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Botão de buscar
            ElevatedButton(
                onClick = {
                    viewModel.buscarCep(cepText)
                    keyboardController?.hide()
                },
                enabled = formState.isDataValid && !formState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Buscar CEP")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resultados da consulta
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CEP: ${formState.endereco.cep}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Logradouro: ${formState.endereco.logradouro}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Bairro: ${formState.endereco.bairro}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Localidade: ${formState.endereco.localidade}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "UF: ${formState.endereco.uf}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

