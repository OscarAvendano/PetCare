package com.example.petcare

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnRegisterBack)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.tvRegisterLoginLink)?.setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnToggleRegisterPassword)?.setOnClickListener {
            togglePasswordVisibility()
        }

        findViewById<Button>(R.id.btnRegisterSubmit)?.setOnClickListener {
            registerNewUser()
        }
    }

    private fun togglePasswordVisibility() {
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val btnToggle = findViewById<ImageView>(R.id.btnToggleRegisterPassword)

        isPasswordVisible = !isPasswordVisible

        val transformation = if (isPasswordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }

        etPassword?.transformationMethod = transformation
        etConfirmPassword?.transformationMethod = transformation
        etPassword?.setSelection(etPassword.text?.length ?: 0)
        btnToggle?.alpha = if (isPasswordVisible) 1.0f else 0.6f
    }

    private fun registerNewUser() {
        val etName = findViewById<EditText>(R.id.etRegisterName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val cbTerms = findViewById<CheckBox>(R.id.cbRegisterTerms)
        val tvStatus = findViewById<TextView>(R.id.tvRegisterStatus)
        val btnSubmit = findViewById<Button>(R.id.btnRegisterSubmit)

        val name = etName?.text?.toString()?.trim() ?: ""
        val email = etEmail?.text?.toString()?.trim() ?: ""
        val password = etPassword?.text?.toString() ?: ""
        val confirmPassword = etConfirmPassword?.text?.toString() ?: ""

        tvStatus?.visibility = View.GONE

        if (name.isEmpty()) {
            showError("Ingresa tu nombre completo.")
            etName?.requestFocus()
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.invalid_email))
            etEmail?.requestFocus()
            return
        }

        if (password.length < 6) {
            showError(getString(R.string.short_password))
            etPassword?.requestFocus()
            return
        }

        if (password != confirmPassword) {
            showError("Las contraseñas no coinciden.")
            etConfirmPassword?.requestFocus()
            return
        }

        if (cbTerms?.isChecked == false) {
            showError("Debes aceptar los Términos y Condiciones.")
            return
        }

        btnSubmit?.isEnabled = false
        btnSubmit?.text = "Creando cuenta..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnSubmit?.isEnabled = true
                btnSubmit?.text = "Crear cuenta     ➜"

                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    firebaseUser?.updateProfile(profileUpdates)

                    if (firebaseUser != null) {
                        val userMap = hashMapOf<String, Any?>(
                            "uid" to firebaseUser.uid,
                            "name" to name,
                            "email" to email,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                        db.collection("users").document(firebaseUser.uid)
                            .set(userMap)
                    }

                    Toast.makeText(
                        this,
                        getString(R.string.register_success),
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "No fue posible crear la cuenta."
                    showError(errorMsg)
                }
            }
    }

    private fun showError(message: String) {
        val tvStatus = findViewById<TextView>(R.id.tvRegisterStatus)
        tvStatus?.text = message
        tvStatus?.visibility = View.VISIBLE
    }
}
