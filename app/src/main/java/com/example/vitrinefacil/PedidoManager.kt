package com.example.vitrinefacil

// Gerência a lista de pedidos de forma temporária,
// apenas enquanto o aplicativo está aberto.
// Garante que a lista de pedidos seja a mesma para todas as telas.
object PedidoManager {

    // Esta é a lista privada que guarda todos os pedidos finalizados.
    private val listaDePedidos = mutableListOf<Pedido>()

    fun adicionarPedido(pedido: Pedido) {
        listaDePedidos.add(0, pedido)
    }

    fun getPedidos(): List<Pedido> {
        return listaDePedidos.toList()
    }

    fun findPedidoById(id: String): Pedido? {
        return listaDePedidos.find { it.id == id }
    }
}
