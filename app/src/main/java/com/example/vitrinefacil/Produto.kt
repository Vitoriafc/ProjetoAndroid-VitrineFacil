package com.example.vitrinefacil

// Eu defini esta 'data class' como o "molde" para qualquer produto no meu aplicativo.

data class Produto(

    @JvmField
    val nome: String = "",

    @JvmField
    val preco: String = "",

    @JvmField
    val urlDaImagem: String = "",

    @JvmField
    val categoria: String = ""
)

