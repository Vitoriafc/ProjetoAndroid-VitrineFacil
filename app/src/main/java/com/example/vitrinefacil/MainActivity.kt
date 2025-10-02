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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitrinefacil.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

// Esta é a tela principal do meu app. Ela também implementa a interface CarrinhoListener
// para poder ouvir e reagir a mudanças no carrinho de compras.
class MainActivity : AppCompatActivity(), CarrinhoManager.CarrinhoListener {

    // Acesso aos componentes do layout.
    private lateinit var binding: ActivityMainBinding
    // Conexão com o banco de dados Firebase.
    private lateinit var database: FirebaseDatabase
    // Criei duas listas para as lojas: uma para guardar a lista completa que vem do Firebase,
    // e outra para guardar apenas as lojas que devem ser exibidas na tela após a filtragem.
    private val listaCompletaDeLojas = mutableListOf<Loja>()
    private val listaExibidaDeLojas = mutableListOf<Loja>()
    private lateinit var lojaAdapter: LojaAdapter
    // Variável para guardar qual filtro de segmento está selecionado no momento.
    private var filtroSegmentoAtual = "Todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registro esta tela como "ouvinte" para receber atualizações do carrinho.
        CarrinhoManager.addListener(this)

        // Ajusto o layout para não ficar por baixo da barra de status do celular.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Defino a ação de clique para o ícone da sacola.
        binding.sacolaLayout.setOnClickListener {
            startActivity(Intent(this, SacolaActivity::class.java))
        }

        // Inicializo todas as partes principais da tela.
        database = FirebaseDatabase.getInstance()
        setupRecyclerView()
        setupSearchListener()
        setupBottomNavigation()
        buscarDadosDoFirebase()
        atualizarBadge()
    }

    // Esta função é chamada automaticamente pelo CarrinhoManager sempre que um item é adicionado.
    override fun onCarrinhoAtualizado() {
        atualizarBadge()
    }

    // Garanto que o contador da sacola seja atualizado sempre que o usuário voltar para esta tela.
    override fun onResume() {
        super.onResume()
        atualizarBadge()
    }

    // Quando a tela é destruída (fechada), eu a removo da lista de ouvintes para evitar problemas de memória.
    override fun onDestroy() {
        super.onDestroy()
        CarrinhoManager.removeListener(this)
    }

    // Esta função controla a visibilidade e o número do contador (badge) da sacola.
    private fun atualizarBadge() {
        val numeroDeItens = CarrinhoManager.getNumeroTotalDeItens()
        if (numeroDeItens > 0) {
            binding.sacolaBadge.visibility = View.VISIBLE
            binding.sacolaBadge.text = numeroDeItens.toString()
        } else {
            binding.sacolaBadge.visibility = View.GONE
        }
    }

    // Configuração inicial da lista de lojas.
    private fun setupRecyclerView() {
        lojaAdapter = LojaAdapter(listaExibidaDeLojas)
        binding.recyclerViewLojas.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewLojas.adapter = lojaAdapter
    }

    // Configuro um "ouvinte" para a barra de pesquisa que reage a cada letra digitada.
    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            // Sempre que o texto muda, eu chamo a função para filtrar as lojas.
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLojas(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Função principal para buscar os dados das lojas no Firebase.
    private fun buscarDadosDoFirebase() {
        val referenciaLojas = database.getReference("lojas")
        referenciaLojas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaCompletaDeLojas.clear()
                for (lojaSnapshot in snapshot.children) {
                    val loja = lojaSnapshot.getValue(Loja::class.java)
                    if (loja != null) {
                        listaCompletaDeLojas.add(loja)
                    }
                }
                // Depois de buscar os dados, eu chamo as funções para criar os filtros
                // e para exibir a lista inicial de lojas.
                criarChipsDeSegmento()
                filtrarLojas("")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "Falha ao ler dados.", error.toException())
            }
        })
    }

    // Esta função cria os filtros de forma dinâmica.
    private fun criarChipsDeSegmento() {
        binding.chipGroup.removeAllViews()

        // Eu pego a lista de lojas, extraio apenas os segmentos, e uso 'distinct()'
        // para pegar apenas os valores únicos (evitando filtros repetidos).
        val segmentos = listaCompletaDeLojas.map { it.segmento }.distinct()

        // Crio o chip "Todas" manualmente, que sempre existirá.
        val chipTodos = Chip(this).apply {
            text = "Todas"
            isCheckable = true
            isChecked = true
            id = View.generateViewId()
        }
        binding.chipGroup.addView(chipTodos)

        // Crio um chip para cada segmento único que encontrei no Firebase.
        segmentos.forEach { segmento ->
            val chip = Chip(this).apply {
                text = segmento
                isCheckable = true
                id = View.generateViewId()
            }
            binding.chipGroup.addView(chip)
        }

        // Configuro o que acontece quando um chip é clicado.
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                chipTodos.isChecked = true
                return@setOnCheckedStateChangeListener
            }
            val chip = group.findViewById<Chip>(checkedIds[0])
            filtroSegmentoAtual = chip.text.toString()
            filtrarLojas(binding.searchEditText.text.toString())
        }
    }

    // Esta é a função central que combina a busca por texto e o filtro por segmento.
    private fun filtrarLojas(textoBusca: String) {
        listaExibidaDeLojas.clear()
        val textoBuscaMinusculo = textoBusca.lowercase(Locale.getDefault())

        // Primeiro, eu filtro a lista completa de lojas com base no segmento selecionado.
        val lojasFiltradasPorSegmento = if (filtroSegmentoAtual.equals("Todas", ignoreCase = true)) {
            listaCompletaDeLojas
        } else {
            listaCompletaDeLojas.filter {
                it.segmento.equals(filtroSegmentoAtual, ignoreCase = true)
            }
        }

        // Depois, sobre essa lista já filtrada, eu aplico o filtro da barra de pesquisa.
        if (textoBuscaMinusculo.isEmpty()) {
            listaExibidaDeLojas.addAll(lojasFiltradasPorSegmento)
        } else {
            for (loja in lojasFiltradasPorSegmento) {
                if (loja.nome.lowercase(Locale.getDefault()).contains(textoBuscaMinusculo)) {
                    listaExibidaDeLojas.add(loja)
                }
            }
        }
        // No final, eu notifico o adapter que a lista de exibição mudou.
        lojaAdapter.notifyDataSetChanged()
    }

    // Configura as ações da barra de navegação inferior.
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_catalogo -> true
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
}
