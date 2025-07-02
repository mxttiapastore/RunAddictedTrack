package com.app.runaddictedtrack.ui.login
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.runaddictedtrack.R
import com.app.runaddictedtrack.data.local.DatabaseHelper
import com.app.runaddictedtrack.ui.home.HomeActivity
import com.app.runaddictedtrack.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvNoAccount = findViewById<TextView>(R.id.tvNoAccount)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Riempi tutti i campi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = dbHelper.validateUser(email, password)
            if (userId != -1) {
                // Apri HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
        } else {
            Toast.makeText(this, "Credenziali non valide", Toast.LENGTH_SHORT).show()
        }
            }

        tvNoAccount.setOnClickListener {
            // Apri RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}