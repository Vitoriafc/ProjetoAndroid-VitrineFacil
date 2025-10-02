package com.example.vitrinefacil

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vitrinefacil.databinding.ItemProdutoBinding

// Este é o adaptador para a lista de produtos exibida dentro de uma loja.
// Ele conecta os dados de cada Produto ao layout visual do card.
class ProdutoAdapter(private val produtos: List<Produto>) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    // Cria o "molde" visual vazio (o ViewHolder) para cada item da lista.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProdutoViewHolder(binding)
    }

    // Conecta os dados de um produto específico a um ViewHolder.
    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = produtos[position]
        holder.bind(produto)
    }

    // Informa ao RecyclerView o número total de produtos na lista.
    override fun getItemCount(): Int = produtos.size

    // O ViewHolder que "segura" os componentes visuais de um único card de produto.
    inner class ProdutoViewHolder(private val binding: ItemProdutoBinding) : RecyclerView.ViewHolder(binding.root) {

        // A função que preenche o layout com os dados de um produto.
        fun bind(produto: Produto) {
            binding.textViewNomeProduto.text = produto.nome
            binding.textViewPrecoProduto.text = produto.preco

            // Uso a biblioteca Glide para carregar a imagem do produto.
            Glide.with(binding.root.context)
                .load(produto.urlDaImagem)
                .placeholder(R.drawable.vitrinefacil_icon) // Mostra o logo enquanto a imagem carrega.
                .into(binding.imageViewProduto)

            // Eu defini que o clique no card inteiro do produto levará o usuário
            // para a tela de detalhes.
            itemView.setOnClickListener {
                val context = binding.root.context
                // Crio uma Intenção de abrir a tela de detalhes do produto.
                val intent = Intent(context, DetalheProdutoActivity::class.java).apply {
                    // Anexo todas as informações do produto a essa intenção,
                    // para que a próxima tela saiba exatamente qual produto mostrar.
                    putExtra("PRODUTO_NOME", produto.nome)
                    putExtra("PRODUTO_PRECO", produto.preco)
                    putExtra("PRODUTO_IMAGEM_URL", produto.urlDaImagem)
                    putExtra("PRODUTO_CATEGORIA", produto.categoria)
                }
                // Inicio a nova tela.
                context.startActivity(intent)
            }
        }
    }
}

