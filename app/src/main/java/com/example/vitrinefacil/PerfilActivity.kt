package com.example.vitrinefacil

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitrinefacil.databinding.ActivityPerfilBinding

// Esta classe controla a tela de Perfil do usuário.
class PerfilActivity : AppCompatActivity() {

    // Acesso aos componentes do layout.
    private lateinit var binding: ActivityPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Layout: pego o XML e o transformo em objetos visuais que posso usar.
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        // Defino o conteúdo visual desta tela como sendo o layout.
        setContentView(binding.root)

    }
}

