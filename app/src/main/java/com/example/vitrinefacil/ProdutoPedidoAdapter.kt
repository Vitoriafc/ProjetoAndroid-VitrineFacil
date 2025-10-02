package com.example.vitrinefacil

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vitrinefacil.databinding.ItemProdutoPedidoBinding

// Eu criei este adaptador especificamente para a lista de produtos que aparece
// DENTRO da tela de "Detalhes do Pedido". Ele é mais simples que os outros.
class ProdutoPedidoAdapter(private val produtos: List<ProdutoPedido>) : RecyclerView.Adapter<ProdutoPedidoAdapter.ViewHolder>() {

    // Cria o "molde" visual vazio (o ViewHolder) para cada item da lista.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProdutoPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Conecta os dados de um produto específico de um pedido a um ViewHolder.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(produtos[position])
    }

    // Informa ao RecyclerView o número total de produtos neste pedido.
    override fun getItemCount(): Int = produtos.size

    // O ViewHolder que "segura" os componentes visuais de um único item na lista de detalhes do pedido.
    inner class ViewHolder(private val binding: ItemProdutoPedidoBinding) : RecyclerView.ViewHolder(binding.root) {

        // A função que preenche o layout com os dados de um produto comprado.
        fun bind(produto: ProdutoPedido) {
            // Eu formato o texto para mostrar a quantidade (ex: "1x").
            binding.textViewQuantidadeItem.text = "${produto.quantidade}x"
            binding.textViewNomeItem.text = produto.nome
            binding.textViewPrecoItem.text = produto.preco

            // Assim como nas outras listas, eu uso a biblioteca Glide para carregar a imagem do produto.
            Glide.with(binding.root.context)
                .load(produto.urlDaImagem)
                .placeholder(R.drawable.vitrinefacil_icon)
                .into(binding.imageViewProdutoItem)
        }
    }
}

