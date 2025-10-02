package com.example.vitrinefacil

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vitrinefacil.databinding.ItemPedidoBinding

// Este é o adaptador para a lista de pedidos finalizados. Ele conecta os dados
// de cada Pedido ao layout visual do card na tela de Pedidos.
class PedidoAdapter(private val pedidos: List<Pedido>) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    // Esta função cria o "molde" visual vazio (o ViewHolder) para cada item da lista.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    // Esta função conecta os dados de um pedido específico a um ViewHolder.
    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        holder.bind(pedidos[position])
    }

    // Informa ao RecyclerView o número total de pedidos na lista.
    override fun getItemCount(): Int = pedidos.size

    // O ViewHolder que "segura" os componentes visuais de um único card de pedido.
    inner class PedidoViewHolder(private val binding: ItemPedidoBinding) : RecyclerView.ViewHolder(binding.root) {

        // A função que preenche o layout com os dados de um pedido.
        fun bind(pedido: Pedido) {
            binding.textViewPedidoId.text = pedido.id
            binding.textViewStatus.text = pedido.status
            binding.textViewTotal.text = pedido.total

            // Verifico se o pedido tem pelo menos um produto na lista.
            if (pedido.produtos.isNotEmpty()) {
                // Se tiver, eu pego a URL da imagem do primeiro produto.
                val primeiraImagemUrl = pedido.produtos[0].urlDaImagem
                // E uso a biblioteca Glide para carregar essa imagem na ImageView do card.
                Glide.with(binding.root.context)
                    .load(primeiraImagemUrl)
                    .placeholder(R.drawable.vitrinefacil_icon)
                    .into(binding.imageViewProdutoPedido)
            }

            // Defino a ação de clique para o botão "Ver Detalhes".
            binding.buttonVerDetalhes.setOnClickListener {
                val context = binding.root.context
                // Crio uma Intenção de abrir a tela de Detalhes do Pedido.
                val intent = Intent(context, DetalhePedidoActivity::class.java).apply {
                    // Anexo o ID do pedido clicado, para que a próxima tela saiba qual pedido buscar.
                    putExtra("PEDIDO_ID", pedido.id)
                }
                context.startActivity(intent)
            }
        }
    }
}
