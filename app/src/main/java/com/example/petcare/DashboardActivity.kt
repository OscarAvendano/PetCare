package com.example.petcare

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.petcare.model.Pet
import com.example.petcare.model.Reminder
import com.example.petcare.repository.PetRepository
import com.example.petcare.repository.ReminderRepository
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private val petRepository = PetRepository()
    private val reminderRepository = ReminderRepository()
    private val auth = FirebaseAuth.getInstance()

    private var selectedPet: Pet? = null
    private var cachedPets: List<Pet> = emptyList()
    private var cachedReminders: List<Reminder> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

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

        setupUserGreeting()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadDashboardData()
        }
    }

    private fun setupUserGreeting() {
        val currentUser = auth.currentUser
        val greetingTv = findViewById<TextView>(R.id.tvGreeting)
        if (currentUser != null && !currentUser.displayName.isNullOrBlank()) {
            greetingTv?.text = "¡Hola, ${currentUser.displayName}! 👋"
        } else if (currentUser != null && !currentUser.email.isNullOrBlank()) {
            val nameFromEmail = currentUser.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Usuario"
            greetingTv?.text = "¡Hola, $nameFromEmail! 👋"
        }
    }

    private fun loadDashboardData() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarDashboard)
        progressBar?.visibility = View.VISIBLE

        petRepository.getPetsForCurrentUser { petResult ->
            petResult.onSuccess { pets ->
                cachedPets = pets
                if (selectedPet == null || !pets.any { it.petId == selectedPet?.petId }) {
                    selectedPet = pets.firstOrNull()
                }

                reminderRepository.getRemindersForCurrentUser { reminderResult ->
                    progressBar?.visibility = View.GONE
                    reminderResult.onSuccess { reminders ->
                        cachedReminders = reminders
                        updateDashboardUI(pets, reminders)
                    }.onFailure {
                        cachedReminders = emptyList()
                        updateDashboardUI(pets, emptyList())
                    }
                }
            }.onFailure { e ->
                progressBar?.visibility = View.GONE
                Toast.makeText(this, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                updateDashboardUI(emptyList(), emptyList())
            }
        }
    }

    private fun updateDashboardUI(pets: List<Pet>, reminders: List<Reminder>) {
        val tvPetsStatusSub = findViewById<TextView>(R.id.tvPetsStatusSub)
        val tvRegisteredPetsCount = findViewById<TextView>(R.id.tvRegisteredPetsCount)
        val layoutEmptyPets = findViewById<View>(R.id.layoutEmptyPets)
        val scrollPetsList = findViewById<View>(R.id.scrollPetsList)
        val containerPetsList = findViewById<LinearLayout>(R.id.containerPetsList)

        val cardHeroReminder = findViewById<View>(R.id.cardHeroReminder)
        val tvHeroPetTitle = findViewById<TextView>(R.id.tvHeroPetTitle)

        val layoutEmptyReminders = findViewById<View>(R.id.layoutEmptyReminders)
        val layoutRemindersList = findViewById<View>(R.id.layoutRemindersList)
        val badgeNotificationDot = findViewById<View>(R.id.badgeNotificationDot)

        // 1. ESTADO DE MASCOTAS
        if (pets.isEmpty()) {
            tvPetsStatusSub?.text = getString(R.string.pets_status_empty)
            tvRegisteredPetsCount?.text = getString(R.string.zero_registered_pets)
            layoutEmptyPets?.visibility = View.VISIBLE
            scrollPetsList?.visibility = View.GONE
            cardHeroReminder?.visibility = View.GONE
            layoutEmptyReminders?.visibility = View.VISIBLE
            layoutRemindersList?.visibility = View.GONE
            badgeNotificationDot?.visibility = View.GONE
            return
        }

        tvPetsStatusSub?.text = getString(R.string.pets_status_sub)
        tvRegisteredPetsCount?.text = "${pets.size} ${if (pets.size == 1) "registrada" else "registradas"}"
        layoutEmptyPets?.visibility = View.GONE
        scrollPetsList?.visibility = View.VISIBLE

        populatePetCards(containerPetsList, pets)

        // 2. HERO CARD (SÓLO SI EXISTE UN RECORDATORIO REAL PARA LA MASCOTA SELECCIONADA)
        val currentPet = selectedPet ?: pets.first()
        val petReminders = reminders.filter { it.petId == currentPet.petId || it.petId.isBlank() }
        val heroReminder = petReminders.firstOrNull { it.isHero } ?: petReminders.firstOrNull()

        if (heroReminder != null) {
            cardHeroReminder?.visibility = View.VISIBLE
            val breedOrSpecies = currentPet.breed.ifBlank { currentPet.species }
            tvHeroPetTitle?.text = "${currentPet.name} ($breedOrSpecies)"

            val tvHeroBadge = findViewById<TextView>(R.id.tvHeroBadge)
            val tvHeroTime = findViewById<TextView>(R.id.tvHeroTime)
            val layoutHeroLocation = findViewById<View>(R.id.layoutHeroLocation)
            val tvHeroLocation = findViewById<TextView>(R.id.tvHeroLocation)
            val layoutHeroDoctor = findViewById<View>(R.id.layoutHeroDoctor)
            val tvHeroDoctor = findViewById<TextView>(R.id.tvHeroDoctor)

            tvHeroBadge?.text = "• ${heroReminder.type.ifBlank { heroReminder.title }}"
            tvHeroTime?.text = heroReminder.dateText.ifBlank { "Próximamente" }

            if (heroReminder.location.isNotBlank()) {
                layoutHeroLocation?.visibility = View.VISIBLE
                tvHeroLocation?.text = heroReminder.location
            } else {
                layoutHeroLocation?.visibility = View.GONE
            }

            if (heroReminder.doctorOrNotes.isNotBlank()) {
                layoutHeroDoctor?.visibility = View.VISIBLE
                tvHeroDoctor?.text = heroReminder.doctorOrNotes
            } else {
                layoutHeroDoctor?.visibility = View.GONE
            }
        } else {
            // NO MOSTRAR DATOS FICTICIOS SI NO HAY RECORDATORIOS REALES
            cardHeroReminder?.visibility = View.GONE
        }

        // 3. ESTADO DE RECORDATORIOS
        if (reminders.isEmpty()) {
            layoutEmptyReminders?.visibility = View.VISIBLE
            layoutRemindersList?.visibility = View.GONE
            badgeNotificationDot?.visibility = View.GONE
        } else {
            layoutEmptyReminders?.visibility = View.GONE
            layoutRemindersList?.visibility = View.VISIBLE
            badgeNotificationDot?.visibility = View.VISIBLE
            populateRemindersList(reminders)
        }
    }

    private fun populatePetCards(container: LinearLayout?, pets: List<Pet>) {
        if (container == null) return
        container.removeAllViews()

        val density = resources.displayMetrics.density

        for (pet in pets) {
            val isSelected = (pet.petId == selectedPet?.petId)
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((6 * density).toInt(), (6 * density).toInt(), (14 * density).toInt(), (6 * density).toInt())
                setBackgroundResource(if (isSelected) R.drawable.bg_pet_item_selected else R.drawable.bg_pet_item)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, (12 * density).toInt(), 0)
                layoutParams = params
                isClickable = true
                isFocusable = true
            }

            val avatarImageView = ImageView(this).apply {
                val size = (38 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size)
                if (pet.photoUrl.isNotBlank()) {
                    load(pet.photoUrl) {
                        crossfade(true)
                        error(if (pet.species.equals("Gato", true)) R.drawable.ic_cat_avatar else R.drawable.ic_dog_avatar)
                    }
                } else {
                    setImageResource(if (pet.species.equals("Gato", true)) R.drawable.ic_cat_avatar else R.drawable.ic_dog_avatar)
                }
            }

            val textLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins((8 * density).toInt(), 0, 0, 0)
                layoutParams = params
            }

            val nameTextView = TextView(this).apply {
                text = pet.name
                setTextColor(getColor(R.color.pc_navy))
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
            }

            val breedTextView = TextView(this).apply {
                text = pet.breed.ifBlank { pet.species }
                setTextColor(getColor(R.color.pc_secondary))
                textSize = 11f
            }

            textLayout.addView(nameTextView)
            textLayout.addView(breedTextView)

            itemLayout.addView(avatarImageView)
            itemLayout.addView(textLayout)

            if (isSelected) {
                val dotView = View(this).apply {
                    val size = (6 * density).toInt()
                    val params = LinearLayout.LayoutParams(size, size)
                    params.setMargins((8 * density).toInt(), 0, 0, 0)
                    layoutParams = params
                    setBackgroundResource(R.drawable.bg_tag_coral)
                }
                itemLayout.addView(dotView)
            }

            itemLayout.setOnClickListener {
                selectedPet = pet
                updateDashboardUI(cachedPets, cachedReminders)
            }

            container.addView(itemLayout)
        }

        val addPetBtn = FrameLayout(this).apply {
            val size = (44 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size)
            setBackgroundResource(R.drawable.bg_add_pet)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                openRegisterPetScreen()
            }
        }

        val plusIcon = ImageView(this).apply {
            val size = (20 * density).toInt()
            val params = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
            layoutParams = params
            setImageResource(R.drawable.ic_plus)
            setColorFilter(getColor(R.color.pc_secondary))
        }

        addPetBtn.addView(plusIcon)
        container.addView(addPetBtn)
    }

    private fun populateRemindersList(reminders: List<Reminder>) {
        val layoutRemindersList = findViewById<LinearLayout>(R.id.layoutRemindersList) ?: return
        layoutRemindersList.removeAllViews()

        val density = resources.displayMetrics.density

        for (reminder in reminders) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((14 * density).toInt(), (14 * density).toInt(), (14 * density).toInt(), (14 * density).toInt())
                setBackgroundResource(R.drawable.bg_reminder_item)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, (10 * density).toInt())
                layoutParams = params
            }

            val iconFrame = FrameLayout(this).apply {
                val size = (40 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size)
                setBackgroundResource(R.drawable.bg_quick_action_icon)
            }

            val iconImg = ImageView(this).apply {
                val size = (20 * density).toInt()
                val params = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
                layoutParams = params
                setImageResource(R.drawable.ic_pill_bottle)
                setColorFilter(getColor(R.color.pc_navy))
            }
            iconFrame.addView(iconImg)

            val textLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                params.setMargins((12 * density).toInt(), 0, 0, 0)
                layoutParams = params
            }

            val titleTv = TextView(this).apply {
                text = reminder.title
                setTextColor(getColor(R.color.pc_navy))
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
            }

            val subTv = TextView(this).apply {
                text = "${reminder.petName} • ${reminder.type.ifBlank { "Recordatorio" }}"
                setTextColor(getColor(R.color.pc_secondary))
                textSize = 11f
            }

            textLayout.addView(titleTv)
            textLayout.addView(subTv)

            itemLayout.addView(iconFrame)
            itemLayout.addView(textLayout)

            if (reminder.tag.isNotBlank()) {
                val tagTv = TextView(this).apply {
                    text = reminder.tag
                    setTextColor(0xFFE59A72.toInt())
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    setBackgroundResource(R.drawable.bg_tag_coral)
                    setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
                }
                itemLayout.addView(tagTv)
            }

            layoutRemindersList.addView(itemLayout)
        }
    }

    private fun openRegisterPetScreen() {
        val intent = Intent(this, RegisterPetActivity::class.java)
        startActivityForResult(intent, 1001)
    }

    private fun setupClickListeners() {
        val createReminderAction = View.OnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java).apply {
                putExtra("EXTRA_OPEN_CREATE_DIALOG", true)
                selectedPet?.let { pet ->
                    putExtra("EXTRA_PET_ID", pet.petId)
                    putExtra("EXTRA_PET_NAME", pet.name)
                }
            }
            startActivity(intent)
        }

        findViewById<View>(R.id.btnRegisterFirstPet)?.setOnClickListener { openRegisterPetScreen() }
        findViewById<View>(R.id.btnAddPet)?.setOnClickListener { openRegisterPetScreen() }
        findViewById<View>(R.id.btnQuickRegisterPet)?.setOnClickListener { openRegisterPetScreen() }

        findViewById<View>(R.id.btnCreateReminderEmpty)?.setOnClickListener(createReminderAction)
        findViewById<View>(R.id.btnQuickAddEvent)?.setOnClickListener(createReminderAction)

        findViewById<View>(R.id.btnNotifications)?.setOnClickListener {
            Toast.makeText(this, "Notificaciones de PetCare", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnHeroDetails)?.setOnClickListener {
            val petName = selectedPet?.name ?: "Mascota"
            Toast.makeText(this, "Detalles de vacuna/cita de $petName", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnQuickViewHistory)?.setOnClickListener {
            Toast.makeText(this, "Ver historial de mascotas", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.tvSeeAllReminders)?.setOnClickListener {
            Toast.makeText(this, "Ver todos los recordatorios", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.navHome)?.setOnClickListener {
            Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.navPets)?.setOnClickListener {
            val intent = Intent(this, MyPetsActivity::class.java)
            startActivity(intent)
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
