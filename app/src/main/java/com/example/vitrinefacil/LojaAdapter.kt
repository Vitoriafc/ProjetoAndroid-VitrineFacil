package com.example.vitrinefacil

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vitrinefacil.databinding.ItemLojaBinding

// Ponte entre a minha lista de dados e a interface do usuário.
// Ela herda de RecyclerView.Adapter, que é o que dá a ela todos os "poderes" para gerenciar a lista.
class LojaAdapter(private val lojas: List<Loja>) : RecyclerView.Adapter<LojaAdapter.LojaViewHolder>() {

    // Esta função é chamada pelo RecyclerView quando ele precisa criar um novo "molde" visual
    // para um item da lista. Ele só cria alguns, o suficiente para preencher a tela.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LojaViewHolder {
        // Aqui eu pego o meu layout XML (item_loja.xml) e o transformo em um objeto visual (View).
        val binding = ItemLojaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Então, eu retorno esse objeto visual "embrulhado" em um LojaViewHolder.
        return LojaViewHolder(binding)
    }

    // Esta é uma função simples que apenas diz ao RecyclerView quantos itens no total existem na minha lista.
    override fun getItemCount(): Int = lojas.size

    // Ela é chamada toda vez que o RecyclerView precisa
    // mostrar os dados de um item em um "molde" específico.
    override fun onBindViewHolder(holder: LojaViewHolder, position: Int) {
        // Eu pego a loja correta da minha lista, usando a posição (ex: 0 para a primeira loja, 1 para a segunda).
        val loja = lojas[position]
        // E então eu chamo a função 'bind' do meu ViewHolder para conectar os dados da loja a esse molde visual.
        holder.bind(loja)
    }

    // O ViewHolder é como se fosse um "molde" ou um "contêiner" para cada card da lista.
    // Ele guarda as referências para os componentes visuais (TextView, ImageView, etc.).
    inner class LojaViewHolder(private val binding: ItemLojaBinding) : RecyclerView.ViewHolder(binding.root) {

        // Esta função é a que realmente "injeta" os dados da loja no layout.
        fun bind(loja: Loja) {
            // Eu pego o nome da loja e coloco no TextView correspondente.
            binding.textViewNomeLoja.text = loja.nome

            // Eu uso a biblioteca Glide, que é especialista em carregar imagens da internet.
            Glide.with(binding.root.context)
                .load(loja.urlDaImagem) // Pego a URL da imagem da loja.
                .placeholder(R.drawable.vitrinefacil_icon) // Mostro o logo enquanto a imagem carrega.
                .error(R.drawable.vitrinefacil_icon) // Mostro o logo se der erro ao carregar.
                .into(binding.imageViewLojaHeader) // E coloco a imagem no ImageView do cabeçalho.

            // Aqui eu defino o que acontece quando o botão "Acessar" de um card específico é clicado.
            binding.buttonAcessar.setOnClickListener {
                val context = binding.root.context
                // Eu crio uma "Intenção" de abrir a tela de produtos (ProdutosActivity).
                val intent = Intent(context, ProdutosActivity::class.java)
                // E eu "anexo" o nome da loja a essa intenção, para que a próxima tela saiba de qual loja mostrar os produtos.
                intent.putExtra("NOME_DA_LOJA", loja.nome)
                // Finalmente, eu inicio a nova tela.
                context.startActivity(intent)
            }
        }
    }
}
