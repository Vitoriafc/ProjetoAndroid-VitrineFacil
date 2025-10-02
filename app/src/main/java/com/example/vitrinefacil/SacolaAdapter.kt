package com.example.vitrinefacil

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vitrinefacil.databinding.ItemSacolaBinding

// Este é o adaptador para a lista de itens na tela da Sacola.
// Ele é responsável por exibir cada produto e gerenciar as interações do usuário.
class SacolaAdapter(private val itens: MutableList<ItemCarrinho>) : RecyclerView.Adapter<SacolaAdapter.SacolaViewHolder>() {

    // Cria o "molde" visual vazio (o ViewHolder) para cada item da lista.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SacolaViewHolder {
        val binding = ItemSacolaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SacolaViewHolder(binding)
    }

    // Conecta os dados de um ItemCarrinho específico a um ViewHolder.
    override fun onBindViewHolder(holder: SacolaViewHolder, position: Int) {
        val item = itens[position]
        holder.bind(item)
    }

    // Informa ao RecyclerView o número total de itens na sacola.
    override fun getItemCount(): Int = itens.size

    // O ViewHolder que "segura" os componentes visuais de um único item na sacola.
    inner class SacolaViewHolder(private val binding: ItemSacolaBinding) : RecyclerView.ViewHolder(binding.root) {
        // A função que preenche o layout com os dados e configura as ações.
        fun bind(item: ItemCarrinho) {
            binding.textViewNomeProdutoSacola.text = item.produto.nome
            binding.textViewPrecoProdutoSacola.text = item.produto.preco
            binding.textViewQuantidade.text = item.quantidade.toString()
            binding.checkboxSelecionar.isChecked = item.isSelected

            Glide.with(binding.root.context)
                .load(item.produto.urlDaImagem)
                .placeholder(R.drawable.vitrinefacil_icon)
                .into(binding.imageViewProdutoSacola)

            // Defino a ação de clique para o CheckBox.
            // Ele chama o CarrinhoManager para inverter o estado de seleção do item.
            binding.checkboxSelecionar.setOnClickListener {
                CarrinhoManager.toggleSelecao(item)
            }

            // Ação do botão de Mais: aumenta a quantidade do item no carrinho.
            binding.buttonMais.setOnClickListener {
                CarrinhoManager.atualizarQuantidade(item, item.quantidade + 1)
            }

            // Ação do botão de Menos: diminui a quantidade.
            binding.buttonMenos.setOnClickListener {
                CarrinhoManager.atualizarQuantidade(item, item.quantidade - 1)
            }

            // Ação do botão de Excluir: remove o item completamente do carrinho.
            binding.buttonExcluir.setOnClickListener {
                CarrinhoManager.removerItem(item)
            }
        }
    }
}

