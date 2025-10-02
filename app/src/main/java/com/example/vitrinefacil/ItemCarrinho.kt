package com.example.vitrinefacil

// Eu criei esta 'data class' para representar um único item dentro da sacola de compras.
// Ela é mais do que apenas um produto, pois também controla a quantidade e o estado de seleção.
data class ItemCarrinho(
    // Guarda a referência para o objeto 'Produto' original, com todas as suas informações (nome, preço, imagem).
    val produto: Produto,

    var quantidade: Int = 1,

    // Um campo booleano para controlar se o item está selecionado (marcado com o checkbox) para a compra.
    var isSelected: Boolean = true
)
