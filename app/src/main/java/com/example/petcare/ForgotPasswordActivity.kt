package com.example.petcare

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        supportActionBar?.hide()

        window.statusBarColor = getColor(R.color.pc_background)
        window.navigationBarColor = getColor(R.color.pc_background)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        auth = FirebaseAuth.getInstance()

        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnBackToLogin)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnSendInstructions)?.setOnClickListener {
            sendPasswordResetEmail()
        }

        findViewById<View>(R.id.tvContactSupport)?.setOnClickListener {
            Toast.makeText(
                this,
                "Contactando a soporte de PetCare...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sendPasswordResetEmail() {
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSendInstructions = findViewById<View>(R.id.btnSendInstructions)
        val cardEmailSent = findViewById<View>(R.id.cardEmailSent)

        val email = etEmail?.text?.toString()?.trim() ?: ""

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                this,
                getString(R.string.invalid_email),
                Toast.LENGTH_SHORT
            ).show()
            etEmail?.requestFocus()
            return
        }

        btnSendInstructions?.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                btnSendInstructions?.isEnabled = true
                if (task.isSuccessful) {
                    cardEmailSent?.visibility = View.VISIBLE
                    Toast.makeText(
                        this,
                        getString(R.string.reset_email_sent),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "No se pudo enviar el correo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
