package com.example.vitrinefacil

// Eu defini esta data class como o "molde" para qualquer loja no meu aplicativo.
data class Loja(
    // A anotação @JvmField e o valor padrão "" são truques importantes
    // para garantir que o Firebase consiga criar e preencher este objeto
    // automaticamente quando ele lê os dados do banco de dados.

    @JvmField
    val nome: String = "",

    @JvmField
    val segmento: String = "",

    @JvmField
    val urlDaImagem: String = ""
)
