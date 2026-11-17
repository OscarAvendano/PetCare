package com.example.petcare

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.petcare.model.Pet
import com.example.petcare.repository.PetRepository
import coil.load
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterPetActivity : AppCompatActivity() {

    private val petRepository = PetRepository()
    private val auth = FirebaseAuth.getInstance()

    private var selectedPhotoUri: Uri? = null
    private var selectedSpecies: String = "" // "Perro", "Gato", "Otra"
    private var selectedSex: String = "" // "Macho", "Hembra", "Sin especificar"
    private var selectedSterilization: String = "" // "Sí", "No", "No especificado"

    private val selectPhotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            findViewById<ImageView>(R.id.imgPetAvatar)?.load(uri) {
                crossfade(true)
            }
            findViewById<View>(R.id.btnRemovePhoto)?.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_pet)

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

        setupPhotoPicker()
        setupToggles()
        setupDatePicker()
        setupSaveButton()
    }

    private fun setupPhotoPicker() {
        val pickPhotoAction = View.OnClickListener {
            selectPhotoLauncher.launch("image/*")
        }

        findViewById<View>(R.id.btnChangePhoto)?.setOnClickListener(pickPhotoAction)
        findViewById<View>(R.id.btnUploadPhotoText)?.setOnClickListener(pickPhotoAction)

        findViewById<View>(R.id.btnRemovePhoto)?.setOnClickListener {
            selectedPhotoUri = null
            val imgAvatar = findViewById<ImageView>(R.id.imgPetAvatar)
            imgAvatar?.setImageResource(R.drawable.ic_petcare)
            imgAvatar?.setColorFilter(getColor(R.color.pc_secondary))
            it.visibility = View.GONE
        }
    }

    private fun setupToggles() {
        // Especie
        findViewById<View>(R.id.btnSpeciesDog)?.setOnClickListener {
            updateSpeciesSelection("Perro")
        }
        findViewById<View>(R.id.btnSpeciesCat)?.setOnClickListener {
            updateSpeciesSelection("Gato")
        }
        findViewById<View>(R.id.btnSpeciesOther)?.setOnClickListener {
            updateSpeciesSelection("Otra")
        }

        // Sexo
        findViewById<View>(R.id.btnSexMale)?.setOnClickListener {
            updateSexSelection("Macho")
        }
        findViewById<View>(R.id.btnSexFemale)?.setOnClickListener {
            updateSexSelection("Hembra")
        }
        findViewById<View>(R.id.btnSexUnspecified)?.setOnClickListener {
            updateSexSelection("Sin especificar")
        }

        // Esterilización
        findViewById<View>(R.id.btnSterilizationYes)?.setOnClickListener {
            updateSterilizationSelection("Sí")
        }
        findViewById<View>(R.id.btnSterilizationNo)?.setOnClickListener {
            updateSterilizationSelection("No")
        }
        findViewById<View>(R.id.btnSterilizationUnspecified)?.setOnClickListener {
            updateSterilizationSelection("No especificado")
        }
    }

    private fun updateSpeciesSelection(species: String) {
        selectedSpecies = species

        val btnDog = findViewById<View>(R.id.btnSpeciesDog)
        val tvDog = findViewById<TextView>(R.id.tvSpeciesDog)
        val imgRadioDog = findViewById<ImageView>(R.id.imgRadioDog)

        val btnCat = findViewById<View>(R.id.btnSpeciesCat)
        val tvCat = findViewById<TextView>(R.id.tvSpeciesCat)
        val imgRadioCat = findViewById<ImageView>(R.id.imgRadioCat)

        val btnOther = findViewById<View>(R.id.btnSpeciesOther)
        val tvOther = findViewById<TextView>(R.id.tvSpeciesOther)
        val imgRadioOther = findViewById<ImageView>(R.id.imgRadioOther)

        btnDog?.setBackgroundResource(if (species == "Perro") R.drawable.bg_species_selected else R.drawable.bg_species_unselected)
        tvDog?.setTextColor(getColor(if (species == "Perro") android.R.color.white else R.color.pc_navy))
        imgRadioDog?.setImageResource(if (species == "Perro") R.drawable.ic_radio_selected else R.drawable.ic_radio_unselected)

        btnCat?.setBackgroundResource(if (species == "Gato") R.drawable.bg_species_selected else R.drawable.bg_species_unselected)
        tvCat?.setTextColor(getColor(if (species == "Gato") android.R.color.white else R.color.pc_navy))
        imgRadioCat?.setImageResource(if (species == "Gato") R.drawable.ic_radio_selected else R.drawable.ic_radio_unselected)

        btnOther?.setBackgroundResource(if (species == "Otra") R.drawable.bg_species_selected else R.drawable.bg_species_unselected)
        tvOther?.setTextColor(getColor(if (species == "Otra") android.R.color.white else R.color.pc_navy))
        imgRadioOther?.setImageResource(if (species == "Otra") R.drawable.ic_radio_selected else R.drawable.ic_radio_unselected)
    }

    private fun updateSexSelection(sex: String) {
        selectedSex = sex

        val btnMale = findViewById<View>(R.id.btnSexMale)
        val tvMale = findViewById<TextView>(R.id.tvSexMale)

        val btnFemale = findViewById<View>(R.id.btnSexFemale)
        val tvFemale = findViewById<TextView>(R.id.tvSexFemale)

        val btnUnspecified = findViewById<View>(R.id.btnSexUnspecified)
        val tvUnspecified = findViewById<TextView>(R.id.tvSexUnspecified)

        btnMale?.setBackgroundResource(if (sex == "Macho") R.drawable.bg_segment_selected else 0)
        tvMale?.setTextColor(getColor(if (sex == "Macho") R.color.pc_navy else R.color.pc_secondary))

        btnFemale?.setBackgroundResource(if (sex == "Hembra") R.drawable.bg_segment_selected else 0)
        tvFemale?.setTextColor(getColor(if (sex == "Hembra") R.color.pc_navy else R.color.pc_secondary))

        btnUnspecified?.setBackgroundResource(if (sex == "Sin especificar") R.drawable.bg_segment_selected else 0)
        tvUnspecified?.setTextColor(getColor(if (sex == "Sin especificar") R.color.pc_navy else R.color.pc_secondary))
    }

    private fun updateSterilizationSelection(sterilization: String) {
        selectedSterilization = sterilization

        val btnYes = findViewById<View>(R.id.btnSterilizationYes)
        val tvYes = findViewById<TextView>(R.id.tvSterilizationYes)

        val btnNo = findViewById<View>(R.id.btnSterilizationNo)
        val tvNo = findViewById<TextView>(R.id.tvSterilizationNo)

        val btnUnspecified = findViewById<View>(R.id.btnSterilizationUnspecified)
        val tvUnspecified = findViewById<TextView>(R.id.tvSterilizationUnspecified)

        btnYes?.setBackgroundResource(if (sterilization == "Sí") R.drawable.bg_segment_selected else 0)
        tvYes?.setTextColor(getColor(if (sterilization == "Sí") R.color.pc_navy else R.color.pc_secondary))

        btnNo?.setBackgroundResource(if (sterilization == "No") R.drawable.bg_segment_selected else 0)
        tvNo?.setTextColor(getColor(if (sterilization == "No") R.color.pc_navy else R.color.pc_secondary))

        btnUnspecified?.setBackgroundResource(if (sterilization == "No especificado") R.drawable.bg_segment_selected else 0)
        tvUnspecified?.setTextColor(getColor(if (sterilization == "No especificado") R.color.pc_navy else R.color.pc_secondary))
    }

    private fun setupDatePicker() {
        val etBirthdate = findViewById<EditText>(R.id.etPetBirthdate)
        etBirthdate?.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                    val spanishLocale = Locale.forLanguageTag("es-ES")
                    val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", spanishLocale)
                    etBirthdate.setText(dateFormat.format(selectedCalendar.time))
                },
                year, month, day
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun setupSaveButton() {
        val btnSave = findViewById<View>(R.id.btnSavePet)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSave)

        btnSave?.setOnClickListener {
            val petName = findViewById<EditText>(R.id.etPetName)?.text?.toString()?.trim() ?: ""
            val breed = findViewById<EditText>(R.id.etPetBreed)?.text?.toString()?.trim() ?: ""
            val birthDate = findViewById<EditText>(R.id.etPetBirthdate)?.text?.toString()?.trim() ?: ""
            val weightStr = findViewById<EditText>(R.id.etPetWeight)?.text?.toString()?.trim() ?: ""
            val color = findViewById<EditText>(R.id.etPetColor)?.text?.toString()?.trim() ?: ""
            val microchip = findViewById<EditText>(R.id.etPetMicrochip)?.text?.toString()?.trim() ?: ""
            val observations = findViewById<EditText>(R.id.etPetObservations)?.text?.toString()?.trim() ?: ""

            // VALIDACIONES
            if (petName.isBlank()) {
                findViewById<EditText>(R.id.etPetName)?.error = "El nombre de la mascota es obligatorio"
                findViewById<EditText>(R.id.etPetName)?.requestFocus()
                return@setOnClickListener
            }

            if (selectedSpecies.isBlank()) {
                Toast.makeText(this, getString(R.string.select_species_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var weightDouble = 0.0
            if (weightStr.isNotEmpty()) {
                try {
                    weightDouble = weightStr.toDouble()
                    if (weightDouble <= 0) {
                        findViewById<EditText>(R.id.etPetWeight)?.error = "El peso debe ser mayor que cero"
                        findViewById<EditText>(R.id.etPetWeight)?.requestFocus()
                        return@setOnClickListener
                    }
                } catch (_: Exception) {
                    findViewById<EditText>(R.id.etPetWeight)?.error = "Ingresa un número decimal válido"
                    return@setOnClickListener
                }
            }

            btnSave.isEnabled = false
            progressBar?.visibility = View.VISIBLE

            val pet = Pet(
                name = petName,
                species = selectedSpecies,
                breed = breed,
                birthDate = birthDate,
                sex = selectedSex,
                weight = weightDouble,
                color = color,
                sterilized = selectedSterilization,
                microchip = microchip,
                observations = observations,
                photoUrl = selectedPhotoUri?.toString() ?: ""
            )

            val doSave = {
                petRepository.savePet(pet) { result ->
                    progressBar?.visibility = View.GONE
                    btnSave.isEnabled = true

                    result.onSuccess { petId ->
                        Toast.makeText(
                            this,
                            "¡Mascota $petName guardada con éxito!",
                            Toast.LENGTH_LONG
                        ).show()

                        val resultIntent = android.content.Intent().apply {
                            putExtra("RESULT_PET_ID", petId)
                            putExtra("RESULT_PET_NAME", petName)
                            putExtra("RESULT_PET_BREED", breed)
                            putExtra("RESULT_IS_DOG", selectedSpecies == "Perro")
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }.onFailure { e ->
                        Toast.makeText(
                            this,
                            "Error al guardar mascota: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            if (auth.currentUser == null) {
                auth.signInAnonymously().addOnCompleteListener {
                    doSave()
                }
            } else {
                doSave()
            }
        }
    }
}
