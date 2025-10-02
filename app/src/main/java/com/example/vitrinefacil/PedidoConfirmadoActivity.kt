package com.example.vitrinefacil

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitrinefacil.databinding.ActivityPedidoConfirmadoBinding

// Esta classe controla a tela que aparece logo após o usuário finalizar uma compra.
class PedidoConfirmadoActivity : AppCompatActivity() {

    // Acesso aos componentes do layout (Views).
    private lateinit var binding: ActivityPedidoConfirmadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidoConfirmadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Faço o ajuste para que o layout não fique por baixo da barra de status do celular.
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Configuro a barra de ferramentas no topo da tela.
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Habilito a seta de "voltar".
        // Defino que a seta de voltar deve levar o usuário para a tela inicial.
        binding.toolbar.setNavigationOnClickListener {
            irParaTelaInicial()
        }

        // Aqui, eu pego o número do pedido que foi enviado pela tela da Sacola.
        // O '.getStringExtra("NUMERO_PEDIDO")' busca o "dado extra" que foi anexado à Intent.
        val numeroPedido = intent.getStringExtra("NUMERO_PEDIDO")
        // E então, eu exibo esse número no TextView correspondente.
        binding.textViewNumeroPedido.text = numeroPedido

        // Defino a ação do botão "Ver Meus Pedidos".
        binding.buttonVerPedidos.setOnClickListener {
            val intent = Intent(this, PedidosActivity::class.java)
            // Uso essas flags para limpar o histórico de telas, garantindo uma navegação limpa.
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Fecho a tela de confirmação.
        }

        // Defino a ação do botão "Continuar Comprando".
        binding.buttonContinuarComprando.setOnClickListener {
            irParaTelaInicial()
        }
    }

    // Criei esta função para evitar repetição de código.
    // Ela é responsável por levar o usuário de volta para a tela principal do app.
    private fun irParaTelaInicial() {
        val intent = Intent(this, MainActivity::class.java)
        // Essas flags garantem que, ao ir para a tela inicial, todas as telas anteriores
        // (como a sacola e a confirmação) sejam fechadas, criando uma nova experiência de compra.
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Fecho a tela de confirmação.
    }
}
