package com.example.vitrinefacil

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitrinefacil.databinding.ActivitySacolaBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Esta classe controla a tela da Sacola de Compras.
// Ela implementa a interface CarrinhoListener para ser notificada sobre mudanças no carrinho.
class SacolaActivity : AppCompatActivity(), CarrinhoManager.CarrinhoListener {

    // Acesso aos componentes do layout.
    private lateinit var binding: ActivitySacolaBinding
    // O adaptador para a lista de itens na sacola.
    private lateinit var sacolaAdapter: SacolaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySacolaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Registro esta tela como "ouvinte" do carrinho para receber atualizações.
        CarrinhoManager.addListener(this)

        // Ajusto o layout para não ficar por baixo da barra de status do celular.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Configuro os componentes da tela.
        setupToolbar()
        setupRecyclerView()
        atualizarTela() // Chamo uma vez para mostrar o estado inicial do carrinho.

        // Ação do botão para voltar ao catálogo.
        binding.buttonContinuarComprando.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Ação do botão para finalizar a compra.
        binding.buttonFinalizarPedido.setOnClickListener {
            finalizarPedido()
        }
    }

    // Função chamada pelo CarrinhoManager sempre que um item é adicionado, removido ou alterado.
    override fun onCarrinhoAtualizado() {
        atualizarTela()
    }

    // Removo o "ouvinte" quando a tela é fechada para evitar problemas de memória.
    override fun onDestroy() {
        super.onDestroy()
        CarrinhoManager.removeListener(this)
    }

    // Configuro a barra de ferramentas no topo da tela.
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish() // A seta de voltar simplesmente fecha esta tela.
        }
    }

    // Configuro a lista (RecyclerView) que mostrará os itens do carrinho.
    private fun setupRecyclerView() {
        sacolaAdapter = SacolaAdapter(CarrinhoManager.getItensDoCarrinho().toMutableList())
        binding.recyclerViewSacola.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSacola.adapter = sacolaAdapter
    }

    // Esta função é a principal da tela. Ela decide se mostra a tela de "sacola vazia"
    // ou a lista de produtos com o total.
    private fun atualizarTela() {
        val itens = CarrinhoManager.getItensDoCarrinho()
        if (itens.isEmpty()) {
            // Se o carrinho está vazio, mostro a mensagem e o botão "Continuar Comprando".
            binding.groupSacolaVazia.visibility = View.VISIBLE
            binding.groupSacolaCheia.visibility = View.GONE
        } else {
            // Se tem itens, mostro a lista, o rodapé com o total e o botão "Finalizar Pedido".
            binding.groupSacolaVazia.visibility = View.GONE
            binding.groupSacolaCheia.visibility = View.VISIBLE

            // Recrio o adaptador com a lista mais recente de itens.
            sacolaAdapter = SacolaAdapter(itens.toMutableList())
            binding.recyclerViewSacola.adapter = sacolaAdapter

            // Calculo e exibo o valor total, já formatado.
            binding.textViewTotal.text = CarrinhoManager.getTotalFormatado()
        }
    }

    // Esta é a função que transforma o conteúdo da sacola em um pedido permanente no Firebase.
    private fun finalizarPedido() {
        // Primeiro, pego apenas os itens que o usuário deixou selecionados com o checkbox.
        val itensSelecionados = CarrinhoManager.getItensDoCarrinho().filter { it.isSelected }

        if (itensSelecionados.isEmpty()) {
            Toast.makeText(this, "Selecione ao menos um item para finalizar o pedido.", Toast.LENGTH_SHORT).show()
            return
        }

        // Converto os itens do carrinho para o formato 'ProdutoPedido', que é o que eu salvo no banco.
        val produtosDoPedido = itensSelecionados.map {
            ProdutoPedido(
                nome = it.produto.nome,
                preco = it.produto.preco,
                urlDaImagem = it.produto.urlDaImagem,
                quantidade = it.quantidade
            )
        }

        // Gero um ID único para o pedido.
        val randomId = (1000..9999).random()
        val timestamp = (1000..9999).random()
        val pedidoId = "PEDIDO-${randomId}-${timestamp}"

        // Pego a data e hora atuais.
        val sdf = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault())
        val dataAtual = sdf.format(Date())

        // Crio o objeto 'Pedido' completo com todas as informações.
        val novoPedido = Pedido(
            id = pedidoId,
            data = dataAtual,
            status = "Pendente",
            total = CarrinhoManager.getTotalFormatado(),
            produtos = produtosDoPedido
        )

        // Finalmente, eu me conecto ao Firebase e salvo o novo pedido na "gaveta" de 'pedidos'.
        val database = FirebaseDatabase.getInstance()
        database.getReference("pedidos").child(novoPedido.id).setValue(novoPedido)
            .addOnSuccessListener {
                // Se a operação for um sucesso...
                Toast.makeText(this, "Pedido finalizado com sucesso!", Toast.LENGTH_SHORT).show()
                CarrinhoManager.limparCarrinho() // Limpo o carrinho temporário.
                // E abro a tela de confirmação, enviando o número do novo pedido.
                val intent = Intent(this, PedidoConfirmadoActivity::class.java).apply {
                    putExtra("NUMERO_PEDIDO", novoPedido.id)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                // Se der erro, mostro uma mensagem.
                Toast.makeText(this, "Falha ao salvar pedido: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}

