package com.example.petcare

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.petcare.model.Pet
import com.example.petcare.model.Reminder
import com.example.petcare.repository.PetRepository
import com.example.petcare.repository.ReminderRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleActivity : AppCompatActivity() {

    private val petRepository = PetRepository()
    private val reminderRepository = ReminderRepository()
    private val auth = FirebaseAuth.getInstance()

    private var currentCalendar: Calendar = Calendar.getInstance()
    private var selectedDayCalendar: Calendar = Calendar.getInstance()
    private var isWeekView = true

    private var userPets: List<Pet> = emptyList()
    private var allReminders: List<Reminder> = emptyList()

    private var autoOpenCreateDialog = false
    private var preselectedPetIdExtra: String? = null
    private var preselectedPetNameExtra: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

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

        autoOpenCreateDialog = intent.getBooleanExtra("EXTRA_OPEN_CREATE_DIALOG", false)
        preselectedPetIdExtra = intent.getStringExtra("EXTRA_PET_ID")
        preselectedPetNameExtra = intent.getStringExtra("EXTRA_PET_NAME")

        setupUserGreeting()
        setupCalendarViewControls()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadDataFromFirestore()
    }

    private fun setupUserGreeting() {
        val currentUser = auth.currentUser
        val imgProfile = findViewById<ImageView>(R.id.imgProfileSchedule)
        if (currentUser != null) {
            imgProfile?.setImageResource(R.drawable.ic_user_avatar)
        }
    }

    private fun setupCalendarViewControls() {
        updateMonthButtonText()

        findViewById<View>(R.id.btnSelectMonth)?.setOnClickListener {
            val year = currentCalendar.get(Calendar.YEAR)
            val month = currentCalendar.get(Calendar.MONTH)
            val day = currentCalendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                { _, y, m, d ->
                    currentCalendar.set(y, m, d)
                    selectedDayCalendar.set(y, m, d)
                    updateMonthButtonText()
                    renderCalendarStrip()
                    filterAndRenderLists()
                },
                year, month, day
            ).show()
        }

        val btnWeek = findViewById<TextView>(R.id.btnViewWeek)
        val btnMonth = findViewById<TextView>(R.id.btnViewMonth)

        btnWeek?.setOnClickListener {
            isWeekView = true
            btnWeek.setBackgroundResource(R.drawable.bg_segment_selected)
            btnWeek.setTextColor(getColor(R.color.pc_navy))
            btnWeek.setTypeface(null, Typeface.BOLD)

            btnMonth?.background = null
            btnMonth?.setTextColor(getColor(R.color.pc_secondary))
            btnMonth?.setTypeface(null, Typeface.NORMAL)

            renderCalendarStrip()
        }

        btnMonth?.setOnClickListener {
            isWeekView = false
            btnMonth.setBackgroundResource(R.drawable.bg_segment_selected)
            btnMonth.setTextColor(getColor(R.color.pc_navy))
            btnMonth.setTypeface(null, Typeface.BOLD)

            btnWeek?.background = null
            btnWeek?.setTextColor(getColor(R.color.pc_secondary))
            btnWeek?.setTypeface(null, Typeface.NORMAL)

            renderCalendarStrip()
        }
    }

    private fun updateMonthButtonText() {
        val localeEs = Locale.forLanguageTag("es-ES")
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", localeEs)
        val monthYearStr = monthYearFormat.format(currentCalendar.time).replaceFirstChar { it.uppercase() }
        findViewById<TextView>(R.id.btnSelectMonth)?.text = "📅 $monthYearStr ▾"
    }

    private fun loadDataFromFirestore() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSchedule)
        progressBar?.visibility = View.VISIBLE

        petRepository.getPetsForCurrentUser { petResult ->
            petResult.onSuccess { pets ->
                userPets = pets
                reminderRepository.getRemindersForCurrentUser { reminderResult ->
                    progressBar?.visibility = View.GONE
                    reminderResult.onSuccess { reminders ->
                        allReminders = reminders
                        renderCalendarStrip()
                        filterAndRenderLists()

                        if (autoOpenCreateDialog) {
                            autoOpenCreateDialog = false
                            showCreateReminderDialog(preselectedPetIdExtra, preselectedPetNameExtra)
                        }
                    }.onFailure {
                        allReminders = emptyList()
                        renderCalendarStrip()
                        filterAndRenderLists()

                        if (autoOpenCreateDialog) {
                            autoOpenCreateDialog = false
                            showCreateReminderDialog(preselectedPetIdExtra, preselectedPetNameExtra)
                        }
                    }
                }
            }.onFailure {
                progressBar?.visibility = View.GONE
                userPets = emptyList()
                allReminders = emptyList()
                renderCalendarStrip()
                filterAndRenderLists()
            }
        }
    }

    private fun renderCalendarStrip() {
        val container = findViewById<LinearLayout>(R.id.containerCalendarDays) ?: return
        container.removeAllViews()

        val density = resources.displayMetrics.density
        val daysToDisplay = if (isWeekView) 7 else 14

        val tempCal = selectedDayCalendar.clone() as Calendar
        tempCal.add(Calendar.DAY_OF_MONTH, -3)

        val dayLetters = arrayOf("D", "L", "M", "M", "J", "V", "S")

        for (i in 0 until daysToDisplay) {
            val dayOfMonth = tempCal.get(Calendar.DAY_OF_MONTH)
            val dayOfWeekIndex = tempCal.get(Calendar.DAY_OF_WEEK) - 1
            val dayLetter = dayLetters[dayOfWeekIndex]

            val isSelectedDay = (tempCal.get(Calendar.YEAR) == selectedDayCalendar.get(Calendar.YEAR) &&
                    tempCal.get(Calendar.DAY_OF_YEAR) == selectedDayCalendar.get(Calendar.DAY_OF_YEAR))

            val hasRealEvents = allReminders.any { reminder ->
                val remCal = Calendar.getInstance().apply { timeInMillis = reminder.dateMillis }
                remCal.get(Calendar.YEAR) == tempCal.get(Calendar.YEAR) &&
                        remCal.get(Calendar.DAY_OF_YEAR) == tempCal.get(Calendar.DAY_OF_YEAR)
            }

            val dayItemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding((6 * density).toInt(), (8 * density).toInt(), (6 * density).toInt(), (8 * density).toInt())
                setBackgroundResource(if (isSelectedDay) R.drawable.bg_button_primary else 0)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                params.setMargins((2 * density).toInt(), 0, (2 * density).toInt(), 0)
                layoutParams = params
                isClickable = true
                isFocusable = true
            }

            val letterTv = TextView(this).apply {
                text = dayLetter
                setTextColor(getColor(if (isSelectedDay) android.R.color.white else R.color.pc_secondary))
                textSize = 11f
                gravity = Gravity.CENTER
            }

            val numberTv = TextView(this).apply {
                text = "$dayOfMonth"
                setTextColor(getColor(if (isSelectedDay) android.R.color.white else R.color.pc_navy))
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }

            dayItemLayout.addView(letterTv)
            dayItemLayout.addView(numberTv)

            if (hasRealEvents) {
                val dotView = View(this).apply {
                    val size = (5 * density).toInt()
                    val params = LinearLayout.LayoutParams(size, size)
                    params.setMargins(0, (4 * density).toInt(), 0, 0)
                    layoutParams = params
                    setBackgroundResource(if (isSelectedDay) R.drawable.bg_tag_coral else R.drawable.bg_circle_navy)
                }
                dayItemLayout.addView(dotView)
            }

            val targetTime = tempCal.timeInMillis
            dayItemLayout.setOnClickListener {
                selectedDayCalendar.timeInMillis = targetTime
                renderCalendarStrip()
                filterAndRenderLists()
            }

            container.addView(dayItemLayout)
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun filterAndRenderLists() {
        val tvAppointmentsCount = findViewById<TextView>(R.id.tvAppointmentsCount)
        val tvCareRemindersCount = findViewById<TextView>(R.id.tvCareRemindersCount)

        val layoutEmptyAppointments = findViewById<View>(R.id.layoutEmptyAppointments)
        val layoutAppointmentsList = findViewById<LinearLayout>(R.id.layoutAppointmentsList)

        val layoutEmptyCareReminders = findViewById<View>(R.id.layoutEmptyCareReminders)
        val layoutCareRemindersList = findViewById<LinearLayout>(R.id.layoutCareRemindersList)

        val layoutRecentHistoryList = findViewById<LinearLayout>(R.id.layoutRecentHistoryList)

        val pendingAppointments = allReminders.filter { !it.isCompleted && (it.type.equals("Cita", true) || it.type.equals("Vacuna", true) || it.isHero) }
        val activeCareReminders = allReminders.filter { !it.isCompleted && !it.type.equals("Cita", true) && !it.type.equals("Vacuna", true) && !it.isHero }
        val completedHistory = allReminders.filter { it.isCompleted }

        // 1. CITAS VETERINARIAS
        tvAppointmentsCount?.text = "${pendingAppointments.size} ${if (pendingAppointments.size == 1) "pendiente" else "pendientes"}"
        if (pendingAppointments.isEmpty()) {
            layoutEmptyAppointments?.visibility = View.VISIBLE
            layoutAppointmentsList?.visibility = View.GONE
        } else {
            layoutEmptyAppointments?.visibility = View.GONE
            layoutAppointmentsList?.visibility = View.VISIBLE
            renderAppointmentsCards(layoutAppointmentsList, pendingAppointments)
        }

        // 2. RECORDATORIOS DE CUIDADOS
        tvCareRemindersCount?.text = "${activeCareReminders.size} ${if (activeCareReminders.size == 1) "activo" else "activos"}"
        if (activeCareReminders.isEmpty()) {
            layoutEmptyCareReminders?.visibility = View.VISIBLE
            layoutCareRemindersList?.visibility = View.GONE
        } else {
            layoutEmptyCareReminders?.visibility = View.GONE
            layoutCareRemindersList?.visibility = View.VISIBLE
            renderCareRemindersCards(layoutCareRemindersList, activeCareReminders)
        }

        // 3. HISTORIAL RECIENTE
        renderHistoryCards(layoutRecentHistoryList, completedHistory)
    }

    private fun renderAppointmentsCards(container: LinearLayout?, appointments: List<Reminder>) {
        if (container == null) return
        container.removeAllViews()

        val density = resources.displayMetrics.density

        for (appointment in appointments) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding((16 * density).toInt(), (14 * density).toInt(), (16 * density).toInt(), (14 * density).toInt())
                setBackgroundResource(R.drawable.bg_dashboard_hero)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, (12 * density).toInt())
                layoutParams = params
            }

            val topRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val statusTv = TextView(this).apply {
                text = if (appointment.tag.isNotBlank()) "• ${appointment.tag}" else "• Confirmada"
                setTextColor(0xFFE59A72.toInt())
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setBackgroundResource(R.drawable.bg_tag_coral)
                setPadding((8 * density).toInt(), (3 * density).toInt(), (8 * density).toInt(), (3 * density).toInt())
            }

            val timeTv = TextView(this).apply {
                text = " | ${appointment.dateText.ifBlank { "Próximamente" }}"
                setTextColor(getColor(R.color.pc_secondary))
                textSize = 12f
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                params.setMargins((6 * density).toInt(), 0, 0, 0)
                layoutParams = params
            }

            val deleteBtn = ImageView(this).apply {
                val size = (20 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size)
                setImageResource(R.drawable.ic_plus)
                setColorFilter(getColor(R.color.pc_secondary))
                setOnClickListener { showDeleteReminderDialog(appointment) }
            }

            topRow.addView(statusTv)
            topRow.addView(timeTv)
            topRow.addView(deleteBtn)

            val titleTv = TextView(this).apply {
                text = "Cita: ${appointment.title}"
                setTextColor(getColor(R.color.pc_navy))
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, (10 * density).toInt(), 0, 0)
                layoutParams = params
            }

            val petRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, (8 * density).toInt(), 0, 0)
                layoutParams = params
            }

            val petNameTv = TextView(this).apply {
                text = "Mascota: ${appointment.petName.ifBlank { "Mi Mascota" }}"
                setTextColor(getColor(R.color.pc_navy))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
            }

            petRow.addView(petNameTv)

            if (appointment.location.isNotBlank()) {
                val locRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, (6 * density).toInt(), 0, 0)
                    layoutParams = params
                }

                val locIcon = ImageView(this).apply {
                    val size = (16 * density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size)
                    setImageResource(R.drawable.ic_location)
                    setColorFilter(getColor(R.color.pc_navy))
                }

                val locTv = TextView(this).apply {
                    text = appointment.location
                    setTextColor(getColor(R.color.pc_navy))
                    textSize = 12f
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins((6 * density).toInt(), 0, 0, 0)
                    layoutParams = params
                }

                locRow.addView(locIcon)
                locRow.addView(locTv)
                itemLayout.addView(locRow)
            }

            if (appointment.doctorOrNotes.isNotBlank()) {
                val docRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, (6 * density).toInt(), 0, 0)
                    layoutParams = params
                }

                val docIcon = ImageView(this).apply {
                    val size = (16 * density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size)
                    setImageResource(R.drawable.ic_doctor)
                    setColorFilter(getColor(R.color.pc_secondary))
                }

                val docTv = TextView(this).apply {
                    text = appointment.doctorOrNotes
                    setTextColor(getColor(R.color.pc_secondary))
                    textSize = 12f
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins((6 * density).toInt(), 0, 0, 0)
                    layoutParams = params
                }

                docRow.addView(docIcon)
                docRow.addView(docTv)
                itemLayout.addView(docRow)
            }

            itemLayout.addView(topRow, 0)
            itemLayout.addView(titleTv, 1)
            itemLayout.addView(petRow, 2)

            container.addView(itemLayout)
        }
    }

    private fun renderCareRemindersCards(container: LinearLayout?, reminders: List<Reminder>) {
        if (container == null) return
        container.removeAllViews()

        val density = resources.displayMetrics.density

        for (reminder in reminders) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding((14 * density).toInt(), (14 * density).toInt(), (14 * density).toInt(), (14 * density).toInt())
                setBackgroundResource(R.drawable.bg_reminder_item)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, (10 * density).toInt())
                layoutParams = params
            }

            val topRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val iconFrame = FrameLayout(this).apply {
                val size = (38 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size)
                setBackgroundResource(R.drawable.bg_quick_action_icon)
            }

            val iconImg = ImageView(this).apply {
                val size = (18 * density).toInt()
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

            val timeTv = TextView(this).apply {
                text = reminder.dateText.ifBlank { "Pendiente" }
                setTextColor(getColor(R.color.pc_secondary))
                textSize = 11f
            }

            textLayout.addView(titleTv)
            textLayout.addView(timeTv)

            topRow.addView(iconFrame)
            topRow.addView(textLayout)

            val actionRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, (10 * density).toInt(), 0, 0)
                layoutParams = params
            }

            val petNameTv = TextView(this).apply {
                text = "Mascota: ${reminder.petName.ifBlank { "Mi Mascota" }}"
                setTextColor(getColor(R.color.pc_secondary))
                textSize = 11f
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = params
            }

            val completeBtn = TextView(this).apply {
                text = getString(R.string.mark_as_completed)
                setTextColor(getColor(R.color.pc_navy))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    markReminderAsCompleted(reminder)
                }
            }

            actionRow.addView(petNameTv)
            actionRow.addView(completeBtn)

            itemLayout.addView(topRow)
            itemLayout.addView(actionRow)

            container.addView(itemLayout)
        }
    }

    private fun renderHistoryCards(container: LinearLayout?, completedList: List<Reminder>) {
        if (container == null) return
        container.removeAllViews()

        if (completedList.isEmpty()) return

        val density = resources.displayMetrics.density

        for (reminder in completedList) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((14 * density).toInt(), (12 * density).toInt(), (14 * density).toInt(), (12 * density).toInt())
                setBackgroundResource(R.drawable.bg_reminder_item)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, (8 * density).toInt())
                layoutParams = params
            }

            val iconImg = ImageView(this).apply {
                val size = (20 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size)
                setImageResource(R.drawable.ic_check_circle)
                setColorFilter(getColor(R.color.pc_secondary))
            }

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
                text = "${reminder.petName} • ${reminder.dateText}"
                setTextColor(getColor(R.color.pc_secondary))
                textSize = 11f
            }

            textLayout.addView(titleTv)
            textLayout.addView(subTv)

            val tagTv = TextView(this).apply {
                text = getString(R.string.completed)
                setTextColor(getColor(R.color.pc_secondary))
                textSize = 11f
                setBackgroundResource(R.drawable.bg_tag_gray)
                setPadding((8 * density).toInt(), (3 * density).toInt(), (8 * density).toInt(), (3 * density).toInt())
            }

            itemLayout.addView(iconImg)
            itemLayout.addView(textLayout)
            itemLayout.addView(tagTv)

            container.addView(itemLayout)
        }
    }

    private fun markReminderAsCompleted(reminder: Reminder) {
        reminder.isCompleted = true
        reminder.tag = "Completado"
        reminderRepository.saveReminder(reminder) {
            Toast.makeText(this, "Marcado como completado", Toast.LENGTH_SHORT).show()
            loadDataFromFirestore()
        }
    }

    private fun showDeleteReminderDialog(reminder: Reminder) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_reminder))
            .setPositiveButton(R.string.delete_action) { _, _ ->
                reminderRepository.deleteReminder(reminder.reminderId) {
                    Toast.makeText(this, "Recordatorio eliminado", Toast.LENGTH_SHORT).show()
                    loadDataFromFirestore()
                }
            }
            .setNegativeButton(R.string.cancel_action, null)
            .show()
    }

    private fun showCreateReminderDialog(preselectedPetId: String? = null, preselectedPetName: String? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_reminder, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etDialogTitle)
        val spType = dialogView.findViewById<Spinner>(R.id.spDialogType)
        val etDate = dialogView.findViewById<EditText>(R.id.etDialogDate)
        val etTime = dialogView.findViewById<EditText>(R.id.etDialogTime)
        val spPet = dialogView.findViewById<Spinner>(R.id.spDialogPet)
        val etLocation = dialogView.findViewById<EditText>(R.id.etDialogLocation)
        val etDoctor = dialogView.findViewById<EditText>(R.id.etDialogDoctor)
        val btnSave = dialogView.findViewById<View>(R.id.btnSaveDialog)

        val types = arrayOf("Cita", "Vacuna", "Desparasitación", "Medicamento", "Higiene", "Otro")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        spType?.adapter = typeAdapter

        val petNames = if (userPets.isNotEmpty()) userPets.map { it.name } else listOf("Mi Mascota")
        val petAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, petNames)
        spPet?.adapter = petAdapter

        // Preseleccionar mascota si viene especificada
        if (!preselectedPetName.isNullOrBlank()) {
            val petPos = petNames.indexOfFirst { it.equals(preselectedPetName, true) }
            if (petPos >= 0) {
                spPet?.setSelection(petPos)
            }
        }

        val selectedCal = Calendar.getInstance()

        etDate?.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    selectedCal.set(y, m, d)
                    val localeEs = Locale.forLanguageTag("es-ES")
                    val df = SimpleDateFormat("dd 'de' MMMM", localeEs)
                    etDate.setText(df.format(selectedCal.time))
                },
                selectedCal.get(Calendar.YEAR),
                selectedCal.get(Calendar.MONTH),
                selectedCal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etTime?.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedCal.set(Calendar.MINUTE, minute)
                    val amPm = if (hourOfDay >= 12) "PM" else "AM"
                    val hour12 = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
                    etTime.setText(timeFormatted)
                },
                10, 30, false
            ).show()
        }

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave?.setOnClickListener {
            val title = etTitle?.text?.toString()?.trim() ?: ""
            val dateText = etDate?.text?.toString()?.trim() ?: ""
            val timeText = etTime?.text?.toString()?.trim() ?: ""
            val location = etLocation?.text?.toString()?.trim() ?: ""
            val doctor = etDoctor?.text?.toString()?.trim() ?: ""

            if (title.isBlank()) {
                etTitle?.error = "Ingresa el título o actividad"
                return@setOnClickListener
            }

            val selectedType = spType?.selectedItem?.toString() ?: "Cita"
            val selectedPetName = if (userPets.isNotEmpty() && spPet != null) {
                userPets.getOrNull(spPet.selectedItemPosition)?.name ?: "Mi Mascota"
            } else {
                "Mi Mascota"
            }
            val selectedPetId = if (userPets.isNotEmpty() && spPet != null) {
                userPets.getOrNull(spPet.selectedItemPosition)?.petId ?: ""
            } else ""

            val reminder = Reminder(
                petId = selectedPetId,
                petName = selectedPetName,
                title = title,
                type = selectedType,
                dateText = if (dateText.isNotBlank()) "$dateText, $timeText" else "Pendiente",
                timeText = timeText,
                location = location,
                doctorOrNotes = doctor,
                tag = if (selectedType == "Cita") "Confirmada" else "Pendiente",
                isHero = (selectedType == "Cita" || selectedType == "Vacuna"),
                dateMillis = selectedCal.timeInMillis
            )

            reminderRepository.saveReminder(reminder) {
                Toast.makeText(this, "Recordatorio guardado correctamente", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                loadDataFromFirestore()
            }
        }

        alertDialog.show()
    }

    private fun setupClickListeners() {
        val createReminderAction = View.OnClickListener {
            showCreateReminderDialog()
        }

        findViewById<View>(R.id.btnCreateReminder)?.setOnClickListener(createReminderAction)

        findViewById<View>(R.id.layoutEmptyAppointments)?.setOnClickListener(createReminderAction)
        findViewById<View>(R.id.layoutEmptyCareReminders)?.setOnClickListener(createReminderAction)

        findViewById<View>(R.id.btnNotifications)?.setOnClickListener {
            Toast.makeText(this, "Notificaciones de Agenda", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnSeeAllHistory)?.setOnClickListener {
            Toast.makeText(this, "Ver todo el historial", Toast.LENGTH_SHORT).show()
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
            // Ya estamos en Agenda
        }
    }
}
