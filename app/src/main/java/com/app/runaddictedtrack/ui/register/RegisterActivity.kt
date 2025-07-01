package com.app.runaddictedtrack.ui.register
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.runaddictedtrack.R
import com.app.runaddictedtrack.data.local.DatabaseHelper
import com.app.runaddictedtrack.ui.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmailReg = findViewById<EditText>(R.id.etEmailReg)
        val etPasswordReg = findViewById<EditText>(R.id.etPasswordReg)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvAlreadyAccount = findViewById<TextView>(R.id.tvAlreadyAccount)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmailReg.text.toString().trim()
            val password = etPasswordReg.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            when {
                password != confirmPassword -> {
                    Toast.makeText(this, "Le password non corrispondono", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() || password.isEmpty() || name.isEmpty() -> {
                    Toast.makeText(this, "Riempi tutti i campi", Toast.LENGTH_SHORT).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, "Email non in formato corretto.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val userId = dbHelper.addUser(email, password, name)
                    if (userId != -1L) {
                        Toast.makeText(this, "Registrazione avvenuta con successo", Toast.LENGTH_SHORT).show()
                        // Torna a LoginActivity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Errore durante la registrazione", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        tvAlreadyAccount.setOnClickListener {
            // Torna a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}