package com.example.vitrinefacil

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Eu criei esta data class para representar um produto específico DENTRO de um pedido já finalizado.
// É uma versão simplificada da classe 'Produto', contendo apenas as informações
// necessárias para o histórico de pedidos.
@Parcelize // Permite passar este objeto entre telas de forma eficiente.
data class ProdutoPedido(

    val nome: String = "",
    val preco: String = "",
    val urlDaImagem: String = "",
    val quantidade: Int = 0
) : Parcelable // Poder ser empacotada e enviada entre telas.

