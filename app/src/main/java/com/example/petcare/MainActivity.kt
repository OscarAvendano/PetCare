package com.example.petcare

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.petcare.databinding.ActivityMainBinding
import com.example.petcare.ui.WelcomeScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private var passwordVisible = false
    private var isShowingLogin = false

    private val preferences by lazy {
        getSharedPreferences("PetCarePreferences", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        // Configuración visual de la ventana
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

        // Inicializar Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Recuperar correo guardado
        loadRememberedEmail()

        // Eventos de los botones
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnBack.setOnClickListener {
            showWelcomeScreen()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isShowingLogin) {
                    showWelcomeScreen()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Mostrar pantalla de bienvenida al iniciar
        showWelcomeScreen()
    }

    private fun showWelcomeScreen() {
        isShowingLogin = false
        setContent {
            WelcomeScreen(
                onCreateAccountClick = {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                },
                onLoginClick = {
                    showLoginScreen()
                }
            )
        }
    }

    private fun showLoginScreen() {
        isShowingLogin = true
        setContentView(binding.root)
    }

    // INICIAR SESIÓN

    private fun loginUser() {

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!validateFields(email, password)) return

        binding.btnLogin.isEnabled = false
        showStatus("Verificando credenciales...", false)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                binding.btnLogin.isEnabled = true

                if (task.isSuccessful) {

                    saveRememberedEmail(email)

                    showStatus("", false)

                    Toast.makeText(
                        this,
                        "Bienvenido a PetCare",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {

                    showStatus(
                        task.exception?.localizedMessage
                            ?: "No fue posible iniciar sesión.",
                        true
                    )
                }
            }
    }

    // REGISTRAR USUARIO

    private fun registerUser() {

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!validateFields(email, password)) return

        binding.tvRegister.isEnabled = false
        showStatus("Creando tu cuenta...", false)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                binding.tvRegister.isEnabled = true

                if (task.isSuccessful) {

                    saveRememberedEmail(email)

                    showStatus("", false)

                    Toast.makeText(
                        this,
                        "Cuenta creada correctamente",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {

                    showStatus(
                        task.exception?.localizedMessage
                            ?: "No fue posible crear la cuenta.",
                        true
                    )
                }
            }
    }

    // RECUPERAR CONTRASEÑA

    private fun resetPassword() {

        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty() ||
            !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            showStatus(
                "Ingresa tu correo para recuperar la contraseña.",
                true
            )
            binding.etEmail.requestFocus()
            return
        }

        binding.tvForgotPassword.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->

                binding.tvForgotPassword.isEnabled = true

                if (task.isSuccessful) {

                    showStatus(
                        "Te enviamos un enlace a tu correo.",
                        false
                    )

                } else {

                    showStatus(
                        task.exception?.localizedMessage
                            ?: "No fue posible enviar el enlace.",
                        true
                    )
                }
            }
    }

    // MOSTRAR / OCULTAR CONTRASEÑA

    private fun togglePasswordVisibility() {

        val cursorPosition = binding.etPassword.selectionStart

        passwordVisible = !passwordVisible

        binding.etPassword.transformationMethod =
            if (passwordVisible) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }

        binding.btnTogglePassword.text =
            if (passwordVisible) "Ocultar" else "Mostrar"

        binding.etPassword.setSelection(
            cursorPosition.coerceAtMost(
                binding.etPassword.text.length
            )
        )
    }

    // VALIDAR CAMPOS

    private fun validateFields(
        email: String,
        password: String
    ): Boolean {

        if (email.isEmpty() || password.isEmpty()) {
            showStatus(
                "Completa todos los campos.",
                true
            )
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showStatus(
                "Ingresa un correo electrónico válido.",
                true
            )
            return false
        }

        if (password.length < 6) {
            showStatus(
                "La contraseña debe tener mínimo 6 caracteres.",
                true
            )
            return false
        }

        showStatus("", false)
        return true
    }

    // RECORDAR CORREO

    private fun saveRememberedEmail(email: String) {

        if (binding.cbRememberMe.isChecked) {

            preferences.edit()
                .putString("remembered_email", email)
                .apply()

        } else {

            preferences.edit()
                .remove("remembered_email")
                .apply()
        }
    }

    private fun loadRememberedEmail() {

        val savedEmail = preferences.getString(
            "remembered_email",
            ""
        )

        if (!savedEmail.isNullOrEmpty()) {
            binding.etEmail.setText(savedEmail)
            binding.cbRememberMe.isChecked = true
        }
    }

    // MOSTRAR MENSAJES

    private fun showStatus(
        message: String,
        isError: Boolean
    ) {

        binding.tvStatus.text = message

        binding.tvStatus.visibility =
            if (message.isEmpty()) View.GONE else View.VISIBLE

        binding.tvStatus.setTextColor(
            getColor(
                if (isError) {
                    R.color.pc_error
                } else {
                    R.color.pc_success
                }
            )
        )
    }
}
