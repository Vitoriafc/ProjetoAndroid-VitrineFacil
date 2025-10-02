package com.example.vitrinefacil

import java.text.NumberFormat
import java.util.Locale

// Garante que terei apenas uma instância do carrinho em todo o aplicativo,
// mantendo os dados consistentes entre as diferentes telas.
object CarrinhoManager {

    // Lista principal que guarda os itens do carrinho.
    // Ela não guarda apenas o Produto, mas um objeto 'ItemCarrinho', que controla também a quantidade e se o item está selecionado.
    private val itensNoCarrinho = mutableListOf<ItemCarrinho>()

    interface CarrinhoListener {
        fun onCarrinhoAtualizado()
    }
    private val listeners = mutableListOf<CarrinhoListener>()

    // As telas (Activities) usam esta função para se registrarem.
    fun addListener(listener: CarrinhoListener) {
        if (!listeners.contains(listener)) listeners.add(listener)
    }

    // E usam esta para deixar de ouvir, evitando problemas de memória quando a tela é fechada.
    fun removeListener(listener: CarrinhoListener) {
        listeners.remove(listener)
    }

    // Sempre que o carrinho muda, eu chamo essa função, que avisa todas as telas registradas.
    private fun notificarListeners() {
        listeners.forEach { it.onCarrinhoAtualizado() }
    }

    // Função para adicionar um produto ao carrinho.
    fun adicionarProduto(produto: Produto, quantidade: Int = 1) {
        // Verifico se o produto já existe na lista.
        val itemExistente = itensNoCarrinho.find { it.produto == produto }
        if (itemExistente != null) {
            // Se já existe, eu apenas aumento a quantidade.
            itemExistente.quantidade += quantidade
        } else {
            // Se é um produto novo, eu crio um novo ItemCarrinho e o adiciono à lista.
            itensNoCarrinho.add(ItemCarrinho(produto = produto, quantidade = quantidade))
        }
        // Notifico as telas para que elas atualizem o contador da sacola.
        notificarListeners()
    }

    // Retorna uma cópia da lista de itens, para que as telas possam exibi-la.
    fun getItensDoCarrinho(): List<ItemCarrinho> {
        return itensNoCarrinho.toList()
    }

    // Função para remover um item da lista, usada pelo botão da lixeira na tela da sacola.
    fun removerItem(item: ItemCarrinho) {
        itensNoCarrinho.remove(item)
        notificarListeners()
    }

    // Usada pelos botões de "+" e "-" na sacola para mudar a quantidade de um item.
    fun atualizarQuantidade(item: ItemCarrinho, novaQuantidade: Int) {
        if (novaQuantidade > 0) {
            item.quantidade = novaQuantidade
        } else {
            // Se a quantidade chegar a zero, o item é removido do carrinho.
            removerItem(item)
        }
        notificarListeners()
    }

    // Usada pelo CheckBox na sacola para marcar ou desmarcar um item.
    fun toggleSelecao(item: ItemCarrinho) {
        item.isSelected = !item.isSelected
        notificarListeners() // Notifico para o total ser recalculado.
    }

    // Calcula o número total de produtos na sacola, somando todas as quantidades.
    fun getNumeroTotalDeItens(): Int {
        return itensNoCarrinho.sumOf { it.quantidade }
    }

    // Limpa completamente o carrinho, usado depois de finalizar um pedido.
    fun limparCarrinho() {
        itensNoCarrinho.clear()
        notificarListeners()
    }

    // Esta função calcula o valor total, mas leva em conta apenas os itens que estão selecionados.
    fun getTotalFormatado(): String {
        var total = 0.0
        // Primeiro, eu filtro a lista para pegar apenas os itens com "isSelected = true".
        itensNoCarrinho.filter { it.isSelected }.forEach { item ->
            // Como o preço vem como texto (ex: "R$ 259,90"), eu preciso limpá-lo
            // para poder converter para um número e fazer o cálculo.
            val precoLimpo = item.produto.preco
                .replace("R$", "")
                .replace(".", "")
                .replace(",", ".")
                .trim()
            try {
                // Multiplico o preço pela quantidade.
                total += precoLimpo.toDouble() * item.quantidade
            } catch (e: NumberFormatException) {
                // Se o preço estiver formatado errado, eu apenas ignoro o item.
            }
        }
        // No final, eu uso o NumberFormat para formatar o total como moeda brasileira (R$).
        val formatoBrasil = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatoBrasil.format(total)
    }
}
