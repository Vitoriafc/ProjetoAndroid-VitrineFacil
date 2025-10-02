package com.example.vitrinefacil

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Ela agrupa todas as informações importantes de uma compra em um único objeto.
@Parcelize // Esta anotação do Android me permite passar o objeto Pedido inteiro de uma tela para outra.
data class Pedido(
    // O ID único do pedido, que eu gero aleatoriamente ("PEDIDO-1234-5678").
    val id: String = "",

    // A data e hora exatas em que o pedido foi criado.
    val data: String = "",

    // O status atual do pedido. Para o meu app, ele sempre começa como "Pendente".
    val status: String = "Pendente",

    // O valor total da compra, já formatado como moeda ("R$ 259,90").
    val total: String = "",

    // Ela guarda todos os produtos que o cliente comprou neste pedido específico.
    val produtos: List<ProdutoPedido> = listOf()
) : Parcelable // A classe implementa Parcelable para poder ser "empacotada" e enviada entre telas.
