package com.example.vitrinefacil

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitrinefacil.databinding.ActivityDetalhePedidoBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Esta é a classe da tela que mostra os detalhes de um pedido específico.
class DetalhePedidoActivity : AppCompatActivity() {

    // 'binding' me dá acesso fácil a todos os componentes do layout (TextViews, etc.).
    private lateinit var binding: ActivityDetalhePedidoBinding
    // Criei um adapter específico para a lista de produtos dentro desta tela.
    private lateinit var produtoPedidoAdapter: ProdutoPedidoAdapter
    // A instância do Firebase para eu poder consultar o banco de dados.
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhePedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializo a conexão com o Firebase.
        database = FirebaseDatabase.getInstance()

        // Garante que o layout da tela não fique por baixo da barra de status do celular.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Configuro a barra de ferramentas no topo da tela, com o botão de voltar.
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish() // Ação de fechar a tela atual e voltar para a anterior.
        }

        // Aqui eu pego o ID do pedido que a tela anterior (a lista de pedidos) me enviou.
        val pedidoId = intent.getStringExtra("PEDIDO_ID")
        if (pedidoId != null) {
            // Se o ID foi recebido corretamente, eu chamo a função para buscar os dados no Firebase.
            buscarDetalhesDoPedido(pedidoId)
        }
    }

    // Esta função faz a consulta ao Firebase para buscar um pedido específico.
    private fun buscarDetalhesDoPedido(pedidoId: String) {
        // Eu aponto diretamente para o "nó" do pedido que eu quero, usando o ID.
        val referenciaPedido = database.getReference("pedidos").child(pedidoId)

        // Eu uso um 'addListenerForSingleValueEvent' porque, por enquanto, só preciso carregar os dados uma vez.
        // No futuro, com o app do vendedor, eu poderia trocar para 'addValueEventListener' para atualizações em tempo real.
        referenciaPedido.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // O Firebase me retorna os dados, e eu converto para o meu objeto 'Pedido'.
                val pedido = snapshot.getValue(Pedido::class.java)
                if (pedido != null) {
                    // Se o pedido foi encontrado, eu chamo a função para preencher a tela.
                    preencherDetalhes(pedido)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Se der algum erro na busca, eu registro no Logcat.
                Log.e("DetalhePedidoActivity", "Erro ao buscar detalhes do pedido: ${error.message}")
            }
        })
    }

    // Esta função preenche todos os campos da tela com os dados do pedido.
    private fun preencherDetalhes(pedido: Pedido) {
        binding.textViewPedidoIdDetalhe.text = pedido.id
        binding.textViewDataDetalhe.text = pedido.data

        // Configuro a lista de produtos (RecyclerView) que fica dentro da tela de detalhes.
        produtoPedidoAdapter = ProdutoPedidoAdapter(pedido.produtos)
        binding.recyclerViewProdutosPedido.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProdutosPedido.adapter = produtoPedidoAdapter

    }
}

