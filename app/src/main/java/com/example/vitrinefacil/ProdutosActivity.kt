package com.example.vitrinefacil

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vitrinefacil.databinding.ActivityProdutosBinding
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

// Esta classe controla a tela que exibe os produtos de uma loja específica.
// Ela também implementa a interface CarrinhoListener para atualizar o contador da sacola.
class ProdutosActivity : AppCompatActivity(), CarrinhoManager.CarrinhoListener {

    // Acesso aos componentes do layout.
    private lateinit var binding: ActivityProdutosBinding
    // Conexão com o banco de dados Firebase.
    private lateinit var database: FirebaseDatabase
    // Criei duas listas: uma para guardar todos os produtos da loja,
    // e outra para guardar apenas os produtos que aparecem na tela após a filtragem.
    private val listaCompletaDeProdutos = mutableListOf<Produto>()
    private val listaExibidaDeProdutos = mutableListOf<Produto>()
    private lateinit var produtoAdapter: ProdutoAdapter
    // Variável para guardar qual filtro de categoria está ativo.
    private var filtroCategoriaAtual = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdutosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registro esta tela como "ouvinte" do carrinho.
        CarrinhoManager.addListener(this)

        // Ajusto o layout para não ficar por baixo da barra de status.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Pego o nome da loja que foi enviado pela MainActivity.
        val nomeDaLoja = intent.getStringExtra("NOME_DA_LOJA")

        // Configuro o clique no ícone da sacola.
        binding.sacolaLayout.setOnClickListener {
            startActivity(Intent(this, SacolaActivity::class.java))
        }

        // Inicializo todos os componentes da tela.
        setupToolbar(nomeDaLoja)
        database = FirebaseDatabase.getInstance()
        setupRecyclerView()
        setupSearchListener()
        setupBottomNavigation()
        atualizarBadge()

        // Se o nome da loja foi recebido corretamente, eu busco os produtos.
        if (nomeDaLoja != null) {
            buscarProdutosDoFirebase(nomeDaLoja)
        }
    }

    // Função chamada pelo CarrinhoManager sempre que o carrinho muda.
    override fun onCarrinhoAtualizado() {
        atualizarBadge()
    }

    // Atualizo o contador sempre que o usuário volta para esta tela.
    override fun onResume() {
        super.onResume()
        atualizarBadge()
    }

    // Removo o "ouvinte" quando a tela é fechada para evitar problemas de memória.
    override fun onDestroy() {
        super.onDestroy()
        CarrinhoManager.removeListener(this)
    }

    // Controla a visibilidade e o número do contador (badge) da sacola.
    private fun atualizarBadge() {
        val numeroDeItens = CarrinhoManager.getNumeroTotalDeItens()
        if (numeroDeItens > 0) {
            binding.sacolaBadge.visibility = View.VISIBLE
            binding.sacolaBadge.text = numeroDeItens.toString()
        } else {
            binding.sacolaBadge.visibility = View.GONE
        }
    }

    // Configuro a barra de ferramentas, exibindo o nome da loja.
    private fun setupToolbar(titulo: String?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.textViewNomeLojaToolbar.text = titulo
        binding.toolbar.setNavigationOnClickListener {
            finish() // A seta de voltar simplesmente fecha esta tela.
        }
    }

    // Configuro a lista de produtos, usando um GridLayoutManager para ter duas colunas.
    private fun setupRecyclerView() {
        produtoAdapter = ProdutoAdapter(listaExibidaDeProdutos)
        binding.recyclerViewProdutos.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewProdutos.adapter = produtoAdapter
    }

    // Configuro o "ouvinte" da barra de pesquisa para filtrar em tempo real.
    private fun setupSearchListener() {
        binding.searchProdutosEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Configuro a barra de navegação inferior.
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_catalogo -> {
                    finish() // O botão "Catálogo" aqui funciona como um "voltar".
                    true
                }
                R.id.navigation_pedidos -> {
                    startActivity(Intent(this, PedidosActivity::class.java))
                    true
                }
                R.id.navigation_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // Função que cria os filtros de categoria dinamicamente.
    private fun criarChipsDeCategoria() {
        binding.chipGroup.removeAllViews() // Limpo os filtros antigos.

        // Pego a lista de produtos, extraio apenas as categorias, e uso 'distinct()' para não ter filtros repetidos.
        val categorias = listaCompletaDeProdutos.map { it.categoria }.distinct()

        // Crio o chip "Todos".
        val chipTodos = Chip(this).apply {
            text = "Todos"
            isCheckable = true
            isChecked = true
            id = View.generateViewId()
        }
        binding.chipGroup.addView(chipTodos)

        // Crio um chip para cada categoria encontrada.
        categorias.forEach { categoria ->
            if (categoria.isNotEmpty()) {
                val chip = Chip(this).apply {
                    text = categoria
                    isCheckable = true
                    id = View.generateViewId()
                }
                binding.chipGroup.addView(chip)
            }
        }

        // Configuro a ação de clique nos chips.
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                chipTodos.isChecked = true
                return@setOnCheckedStateChangeListener
            }
            val chip = group.findViewById<Chip>(checkedIds[0])
            filtroCategoriaAtual = chip.text.toString()
            filtrarLista(binding.searchProdutosEditText.text.toString())
        }
    }

    // A função principal que combina o filtro por categoria com a busca por texto.
    private fun filtrarLista(textoBusca: String) {
        listaExibidaDeProdutos.clear()
        val textoBuscaMinusculo = textoBusca.lowercase(Locale.getDefault())
        // Primeiro, eu filtro a lista completa de produtos pela categoria selecionada.
        val produtosFiltradosPorCategoria = if (filtroCategoriaAtual.equals("Todos", ignoreCase = true)) {
            listaCompletaDeProdutos
        } else {
            listaCompletaDeProdutos.filter {
                it.categoria.equals(filtroCategoriaAtual, ignoreCase = true)
            }
        }
        // Depois, sobre essa lista já filtrada, eu aplico o filtro da barra de pesquisa.
        if (textoBuscaMinusculo.isEmpty()) {
            listaExibidaDeProdutos.addAll(produtosFiltradosPorCategoria)
        } else {
            for (produto in produtosFiltradosPorCategoria) {
                if (produto.nome.lowercase(Locale.getDefault()).contains(textoBuscaMinusculo)) {
                    listaExibidaDeProdutos.add(produto)
                }
            }
        }
        // No final, eu notifico o adapter que a lista de exibição mudou.
        produtoAdapter.notifyDataSetChanged()
    }

    // Função que busca no Firebase os produtos da loja específica.
    private fun buscarProdutosDoFirebase(nomeDaLoja: String) {
        val referenciaLojas = database.getReference("lojas")
        // Eu faço uma consulta para encontrar a loja cujo campo "nome" é igual ao que recebi.
        val query = referenciaLojas.orderByChild("nome").equalTo(nomeDaLoja)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCompletaDeProdutos.clear()
                // O resultado vem como uma lista, então preciso iterar.
                for (lojaSnapshot in snapshot.children) {
                    // Pego a "gaveta" de produtos de dentro da loja que encontrei.
                    val produtosSnapshot = lojaSnapshot.child("produtos")
                    for (produtoSnapshot in produtosSnapshot.children) {
                        val produto = produtoSnapshot.getValue(Produto::class.java)
                        if (produto != null) {
                            listaCompletaDeProdutos.add(produto)
                        }
                    }
                }
                // Depois de carregar os produtos, crio os filtros e exibo a lista.
                criarChipsDeCategoria()
                filtrarLista("")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ProdutosActivity", "Erro ao buscar produtos: ${error.message}")
            }
        })
    }
}

