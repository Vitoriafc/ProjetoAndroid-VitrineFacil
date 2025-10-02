package com.example.vitrinefacil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.vitrinefacil.databinding.ActivityDetalheProdutoBinding

// Esta é a classe da tela de detalhes de um produto.
// Ela implementa a interface CarrinhoListener para poder ser notificada sobre mudanças na sacola.
class DetalheProdutoActivity : AppCompatActivity(), CarrinhoManager.CarrinhoListener {

    // 'binding' me dá acesso fácil a todos os componentes do layout.
    private lateinit var binding: ActivityDetalheProdutoBinding
    // Criei uma variável para controlar a quantidade que o usuário seleciona.
    private var quantidade = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalheProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registro esta tela como "ouvinte" do CarrinhoManager para o contador da sacola funcionar.
        CarrinhoManager.addListener(this)

        // Garante que o layout da tela não fique por baixo da barra de status do celular.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        // Configuro a barra de ferramentas no topo da tela, com o botão de voltar.
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish() // Fecha a tela e volta para a lista de produtos.
        }

        // Adiciono a ação de clique no ícone da sacola para abrir a SacolaActivity.
        binding.sacolaLayout.setOnClickListener {
            startActivity(Intent(this, SacolaActivity::class.java))
        }

        // Pego os dados do produto que a tela anterior (ProdutoAdapter) me enviou.
        val nomeProduto = intent.getStringExtra("PRODUTO_NOME")
        val precoProduto = intent.getStringExtra("PRODUTO_PRECO")
        val imagemUrl = intent.getStringExtra("PRODUTO_IMAGEM_URL")
        val categoriaProduto = intent.getStringExtra("PRODUTO_CATEGORIA")

        // Crio um objeto 'Produto' com as informações recebidas.
        val produto = Produto(
            nome = nomeProduto ?: "",
            preco = precoProduto ?: "",
            urlDaImagem = imagemUrl ?: "",
            categoria = categoriaProduto ?: ""
        )

        // Preencho os campos da tela com os dados do produto.
        binding.textViewNomeProdutoDetalhe.text = produto.nome
        binding.textViewPrecoProdutoDetalhe.text = produto.preco
        // Uso a biblioteca Glide para carregar a imagem da internet.
        Glide.with(this).load(produto.urlDaImagem).into(binding.imageViewProdutoDetalhe)

        // Configuro as ações dos botões.
        setupQuantidadeButtons()
        atualizarBadge() // Chamo uma vez para garantir que o contador está correto ao abrir a tela.

        // Botão "Adicionar à Sacola": adiciona o produto e mostra uma mensagem.
        binding.buttonAdicionarSacola.setOnClickListener {
            adicionarProdutosAoCarrinho(produto, quantidade)
            Toast.makeText(this, "$quantidade x ${produto.nome} adicionado(s) à sacola!", Toast.LENGTH_SHORT).show()
        }

        // Botão "Comprar Agora": adiciona o produto e leva para a tela da sacola.
        binding.buttonComprarAgora.setOnClickListener {
            adicionarProdutosAoCarrinho(produto, quantidade)
            startActivity(Intent(this, SacolaActivity::class.java))
        }
    }

    // Esta função é chamada automaticamente pelo CarrinhoManager sempre que um item é adicionado.
    override fun onCarrinhoAtualizado() {
        atualizarBadge()
    }

    // Atualizo o contador também quando o usuário volta para esta tela.
    override fun onResume() {
        super.onResume()
        atualizarBadge()
    }

    // Quando a tela é destruída, eu a removo da lista de "ouvintes" para evitar problemas de memória.
    override fun onDestroy() {
        super.onDestroy()
        CarrinhoManager.removeListener(this)
    }

    // Esta é a função que controla a visibilidade e o texto do contador vermelho da sacola.
    private fun atualizarBadge() {
        val numeroDeItens = CarrinhoManager.getNumeroTotalDeItens()
        if (numeroDeItens > 0) {
            binding.sacolaBadge.visibility = View.VISIBLE
            binding.sacolaBadge.text = numeroDeItens.toString()
        } else {
            binding.sacolaBadge.visibility = View.GONE
        }
    }

    // A lógica para os botões de "+" e "-".
    private fun setupQuantidadeButtons() {
        binding.buttonMais.setOnClickListener {
            quantidade++
            binding.textViewQuantidade.text = quantidade.toString()
        }

        binding.buttonMenos.setOnClickListener {
            if (quantidade > 1) { // Impede que a quantidade seja menor que 1.
                quantidade--
                binding.textViewQuantidade.text = quantidade.toString()
            }
        }
    }

    // Esta função chama o CarrinhoManager para adicionar o produto na quantidade selecionada.
    private fun adicionarProdutosAoCarrinho(produto: Produto, quantidade: Int) {
        CarrinhoManager.adicionarProduto(produto, quantidade)
    }
}
