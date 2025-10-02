package com.example.vitrinefacil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitrinefacil.databinding.ActivityPedidosBinding
import com.google.firebase.database.*

// Esta classe controla a tela que exibe a lista de todos os pedidos do usuário.
class PedidosActivity : AppCompatActivity() {

    // Acesso aos componentes do layout.
    private lateinit var binding: ActivityPedidosBinding
    private lateinit var database: FirebaseDatabase
    // A lista local que vai guardar os pedidos que vêm do Firebase.
    private val listaDePedidos = mutableListOf<Pedido>()
    // O adaptador que conecta a lista de pedidos à interface gráfica.
    private lateinit var pedidoAdapter: PedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajusto o layout para não ficar por baixo da barra de status do celular.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Inicializo a conexão com o Firebase.
        database = FirebaseDatabase.getInstance()

        // Chamo as funções para configurar todos os elementos da tela.
        setupToolbar()
        setupRecyclerView()
        setupBottomNavigation()
        buscarPedidosDoFirebase() // Função principal que carrega os dados.

        // Defino a ação do botão "Começar a Comprar", que só aparece se a lista estiver vazia.
        binding.buttonComecarAComprar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    // Configuro a barra de ferramentas no topo da tela.
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Removo o título padrão.
    }

    // Configuro o RecyclerView, conectando-o ao seu adaptador.
    private fun setupRecyclerView() {
        pedidoAdapter = PedidoAdapter(listaDePedidos)
        binding.recyclerViewPedidos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPedidos.adapter = pedidoAdapter
    }

    // Esta é a função que busca os pedidos salvos no Firebase.
    private fun buscarPedidosDoFirebase() {
        // Aponto para o "nó" ou "gaveta" de 'pedidos' no meu banco de dados.
        val referenciaPedidos = database.getReference("pedidos")

        // Eu uso um 'addValueEventListener' para que a lista se atualize em tempo real
        // se algum pedido for adicionado ou modificado no banco de dados.
        referenciaPedidos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaDePedidos.clear() // Limpo a lista local para não duplicar os dados.
                // Para cada "filho" dentro do nó 'pedidos'...
                for (pedidoSnapshot in snapshot.children) {
                    // ...eu tento converter os dados para o meu objeto 'Pedido'.
                    val pedido = pedidoSnapshot.getValue(Pedido::class.java)
                    if (pedido != null) {
                        listaDePedidos.add(pedido) // E adiciono à minha lista local.
                    }
                }

                listaDePedidos.reverse() // Inverto a lista para mostrar os mais recentes primeiro.

                // Verifico se a lista está vazia para decidir o que mostrar na tela.
                if (listaDePedidos.isEmpty()) {
                    // Se estiver vazia, eu escondo a lista e mostro a mensagem "Nenhum pedido encontrado".
                    binding.recyclerViewPedidos.visibility = View.GONE
                    binding.groupSemPedidos.visibility = View.VISIBLE
                } else {
                    // Se tiver pedidos, eu mostro a lista e escondo a mensagem.
                    binding.recyclerViewPedidos.visibility = View.VISIBLE
                    binding.groupSemPedidos.visibility = View.GONE
                }

                // Finalmente, eu notifico o adaptador que os dados mudaram, para ele redesenhar a lista.
                pedidoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Se der algum erro, eu registro no Logcat para depuração.
                Log.e("PedidosActivity", "Erro ao buscar pedidos: ${error.message}")
            }
        })
    }

    // Configuro a barra de navegação inferior.
    private fun setupBottomNavigation() {
        // Deixo o ícone "Pedidos" selecionado por padrão nesta tela.
        binding.bottomNavigation.selectedItemId = R.id.navigation_pedidos
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_catalogo -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_pedidos -> true // Já estamos aqui.
                R.id.navigation_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}

