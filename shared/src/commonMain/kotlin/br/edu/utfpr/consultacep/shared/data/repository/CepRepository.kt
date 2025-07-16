package br.edu.utfpr.consultacep.shared.data.repository

import br.edu.utfpr.consultacep.shared.data.model.Endereco
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class CepRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }
    }

    suspend fun buscarCep(cep: String): Endereco {
        val cepLimpo = cep.replace("-", "")

        val urls = listOf(
            "https://viacep.com.br/ws/$cepLimpo/json/",
            "http://viacep.com.br/ws/$cepLimpo/json/"
        )

        for (url in urls) {
            try {
                val response: HttpResponse = client.get(url) {
                    header("User-Agent", "ConsultaCEP/1.0")
                }

                if (response.status == HttpStatusCode.OK) {
                    val responseBody = response.bodyAsText()

                    if (responseBody.isNotEmpty()) {
                        val jsonElement = Json.parseToJsonElement(responseBody)
                        val jsonObject = jsonElement as JsonObject

                        // Verifica se há erro na resposta
                        if (jsonObject.containsKey("erro")) {
                            throw Exception("CEP não encontrado")
                        }

                        return Endereco(
                            cep = jsonObject["cep"]?.jsonPrimitive?.content ?: "",
                            logradouro = jsonObject["logradouro"]?.jsonPrimitive?.content ?: "",
                            complemento = jsonObject["complemento"]?.jsonPrimitive?.content ?: "",
                            bairro = jsonObject["bairro"]?.jsonPrimitive?.content ?: "",
                            localidade = jsonObject["localidade"]?.jsonPrimitive?.content ?: "",
                            uf = jsonObject["uf"]?.jsonPrimitive?.content ?: "",
                            ibge = jsonObject["ibge"]?.jsonPrimitive?.content ?: "",
                            gia = jsonObject["gia"]?.jsonPrimitive?.content ?: "",
                            ddd = jsonObject["ddd"]?.jsonPrimitive?.content ?: "",
                            siafi = jsonObject["siafi"]?.jsonPrimitive?.content ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                if (url == urls.last()) {
                    throw Exception("Erro ao consultar CEP: ${e.message}")
                }
            }
        }

        throw Exception("Erro ao consultar CEP")
    }
}