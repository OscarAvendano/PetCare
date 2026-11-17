package com.example.petcare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import coil.load
import com.example.petcare.model.Pet
import com.example.petcare.repository.PetRepository
import com.example.petcare.repository.ReminderRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PetProfileActivity : AppCompatActivity() {

    private val petRepository = PetRepository()
    private val reminderRepository = ReminderRepository()

    private var currentPet: Pet? = null
    private var petIdExtra: String = ""

    private val selectPhotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && currentPet != null) {
            currentPet?.photoUrl = uri.toString()
            findViewById<ImageView>(R.id.imgPetProfileAvatar)?.load(uri) {
                crossfade(true)
            }
            // Guardar inmediatamente en Firestore
            currentPet?.let { pet ->
                petRepository.savePet(pet) { result ->
                    result.onSuccess {
                        Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profile)

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

        petIdExtra = intent.getStringExtra("EXTRA_PET_ID") ?: ""

        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadPetProfile()
    }

    private fun loadPetProfile() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarProfile)
        progressBar?.visibility = View.VISIBLE

        if (petIdExtra.isNotBlank()) {
            fetchPetDetails(petIdExtra)
        } else {
            petRepository.getPetsForCurrentUser { result ->
                result.onSuccess { pets ->
                    if (pets.isNotEmpty()) {
                        val firstPet = pets.first()
                        petIdExtra = firstPet.petId
                        fetchPetDetails(firstPet.petId)
                    } else {
                        progressBar?.visibility = View.GONE
                        Toast.makeText(this, getString(R.string.pet_not_found), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.onFailure { e ->
                    progressBar?.visibility = View.GONE
                    Toast.makeText(this, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun fetchPetDetails(petId: String) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarProfile)
        petRepository.getPetById(petId) { result ->
            progressBar?.visibility = View.GONE
            result.onSuccess { pet ->
                if (pet != null) {
                    currentPet = pet
                    bindPetToUI(pet)
                    fetchHealthRecords(pet)
                } else {
                    Toast.makeText(this, getString(R.string.pet_not_found), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.onFailure { e ->
                Toast.makeText(this, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun bindPetToUI(pet: Pet) {
        findViewById<TextView>(R.id.tvPetProfileName)?.text = pet.name.ifBlank { "Mascota" }

        val sexText = when {
            pet.sex.equals("Macho", true) -> "♂ Macho"
            pet.sex.equals("Hembra", true) -> "♀ Hembra"
            else -> "Sin especificar"
        }
        findViewById<TextView>(R.id.tvPetProfileSexBadge)?.text = sexText

        val breedStr = pet.breed.ifBlank { pet.species.ifBlank { "Mascota" } }
        val speciesStr = pet.species.ifBlank { "Mascota" }
        findViewById<TextView>(R.id.tvPetProfileSpeciesBreed)?.text = "🐾 $speciesStr · $breedStr"

        findViewById<TextView>(R.id.tvPetProfileBreedVal)?.text = breedStr
        findViewById<TextView>(R.id.tvPetProfileAgeVal)?.text = calculateAge(pet.birthDate)
        findViewById<TextView>(R.id.tvPetProfileWeightVal)?.text = if (pet.weight > 0) "${pet.weight} kg" else "Sin registrar"
        findViewById<TextView>(R.id.tvPetProfileMicrochipVal)?.text = pet.microchip.ifBlank { "Sin registrar" }

        // Dieta y observaciones
        findViewById<TextView>(R.id.tvDailyGuidelinesVal)?.text = pet.observations.ifBlank { "Sin observaciones registradas" }

        // Foto avatar
        val imgAvatar = findViewById<ImageView>(R.id.imgPetProfileAvatar)
        if (pet.photoUrl.isNotBlank()) {
            imgAvatar?.load(pet.photoUrl) {
                crossfade(true)
                error(if (pet.species.equals("Gato", true)) R.drawable.ic_cat_avatar else R.drawable.ic_dog_avatar)
            }
        } else {
            imgAvatar?.setImageResource(if (pet.species.equals("Gato", true)) R.drawable.ic_cat_avatar else R.drawable.ic_dog_avatar)
        }
    }

    private fun fetchHealthRecords(pet: Pet) {
        reminderRepository.getRemindersForCurrentUser { result ->
            result.onSuccess { reminders ->
                val petReminders = reminders.filter { it.petId == pet.petId }
                val layoutEmptyHealth = findViewById<View>(R.id.layoutEmptyHealthState)
                if (petReminders.isEmpty()) {
                    layoutEmptyHealth?.visibility = View.VISIBLE
                } else {
                    layoutEmptyHealth?.visibility = View.GONE
                }
            }
        }
    }

    private fun calculateAge(birthDateStr: String): String {
        if (birthDateStr.isBlank()) return "Sin registrar"
        return try {
            val localeEs = Locale.forLanguageTag("es-ES")
            val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", localeEs)
            val birthDate = try {
                sdf.parse(birthDateStr)
            } catch (_: Exception) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(birthDateStr)
            } ?: return birthDateStr

            val birthCal = Calendar.getInstance().apply { time = birthDate }
            val nowCal = Calendar.getInstance()

            var years = nowCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            var months = nowCal.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)

            if (months < 0) {
                years--
                months += 12
            }

            when {
                years > 0 && months > 0 -> "${years}.${months} años"
                years > 0 -> "$years ${if (years == 1) "año" else "años"}"
                months > 0 -> "$months ${if (months == 1) "mes" else "meses"}"
                else -> "Recién nacido"
            }
        } catch (_: Exception) {
            birthDateStr
        }
    }

    private fun openEditPetScreen() {
        val pet = currentPet ?: return
        val intent = Intent(this, RegisterPetActivity::class.java).apply {
            putExtra("EXTRA_PET_NAME", pet.name)
            putExtra("EXTRA_PET_BREED", pet.breed)
            putExtra("EXTRA_PET_BIRTHDATE", pet.birthDate)
            putExtra("EXTRA_PET_WEIGHT", if (pet.weight > 0) pet.weight.toString() else "")
            putExtra("EXTRA_IS_DOG", pet.species.equals("Perro", true))
            putExtra("EXTRA_IS_MALE", pet.sex.equals("Macho", true))
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog() {
        val pet = currentPet ?: return
        val title = getString(R.string.confirm_delete_pet, pet.name)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(R.string.confirm_delete_pet_msg)
            .setPositiveButton(R.string.delete_action) { _, _ ->
                petRepository.deletePet(pet.petId) { result ->
                    result.onSuccess {
                        Toast.makeText(this, "Mascota eliminada correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton(R.string.cancel_action, null)
            .show()
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.btnBack)?.setOnClickListener { finish() }

        val changePhotoAction = View.OnClickListener {
            selectPhotoLauncher.launch("image/*")
        }
        findViewById<View>(R.id.btnChangePhotoProfile)?.setOnClickListener(changePhotoAction)

        findViewById<View>(R.id.btnOverflowMenu)?.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("Editar información")
            popup.menu.add("Eliminar mascota")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Editar información" -> openEditPetScreen()
                    "Eliminar mascota" -> showDeleteConfirmationDialog()
                }
                true
            }
            popup.show()
        }

        findViewById<View>(R.id.btnEditPetInfo)?.setOnClickListener { openEditPetScreen() }

        findViewById<View>(R.id.btnRegisterCare)?.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java).apply {
                putExtra("EXTRA_OPEN_CREATE_DIALOG", true)
                currentPet?.let { pet ->
                    putExtra("EXTRA_PET_ID", pet.petId)
                    putExtra("EXTRA_PET_NAME", pet.name)
                }
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.btnConsultHistory)?.setOnClickListener {
            val petName = currentPet?.name ?: "Mascota"
            Toast.makeText(this, "Consultando historial médico de $petName", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnSeeAllHealth)?.setOnClickListener {
            Toast.makeText(this, "Ver toda la información de salud", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.navHome)?.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.navPets)?.setOnClickListener {
            val intent = Intent(this, MyPetsActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.navHistory)?.setOnClickListener {
            Toast.makeText(this, "Historial", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.navSchedule)?.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }
    }
}
