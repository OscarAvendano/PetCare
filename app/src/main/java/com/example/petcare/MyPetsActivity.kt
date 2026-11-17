package com.example.petcare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcare.adapter.PetListAdapter
import com.example.petcare.model.Pet
import com.example.petcare.repository.PetRepository

class MyPetsActivity : AppCompatActivity() {

    private val petRepository = PetRepository()
    private lateinit var petAdapter: PetListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_pets)

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

        setupRecyclerView()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadPetsFromFirestore()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvMyPetsList)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        petAdapter = PetListAdapter(
            onItemClick = { pet ->
                openPetProfileScreen(pet)
            },
            onEditClick = { pet ->
                openEditPetScreen(pet)
            },
            onDeleteClick = { pet ->
                showDeleteConfirmationDialog(pet)
            }
        )

        recyclerView?.adapter = petAdapter
    }

    private fun loadPetsFromFirestore() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarMyPets)
        progressBar?.visibility = View.VISIBLE

        petRepository.getPetsForCurrentUser { result ->
            progressBar?.visibility = View.GONE
            result.onSuccess { pets ->
                updateUIState(pets)
            }.onFailure { e ->
                Toast.makeText(this, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                updateUIState(emptyList())
            }
        }
    }

    private fun updateUIState(pets: List<Pet>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rvMyPetsList)
        val layoutEmpty = findViewById<View>(R.id.layoutEmptyMyPets)

        if (pets.isEmpty()) {
            recyclerView?.visibility = View.GONE
            layoutEmpty?.visibility = View.VISIBLE
            petAdapter.submitList(emptyList())
        } else {
            recyclerView?.visibility = View.VISIBLE
            layoutEmpty?.visibility = View.GONE
            petAdapter.submitList(pets)
        }
    }

    private fun openRegisterPetScreen() {
        val intent = Intent(this, RegisterPetActivity::class.java)
        startActivity(intent)
    }

    private fun openPetProfileScreen(pet: Pet) {
        val intent = Intent(this, PetProfileActivity::class.java).apply {
            putExtra("EXTRA_PET_ID", pet.petId)
        }
        startActivity(intent)
    }

    private fun openEditPetScreen(pet: Pet) {
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

    private fun showDeleteConfirmationDialog(pet: Pet) {
        val title = getString(R.string.confirm_delete_pet, pet.name)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(R.string.confirm_delete_pet_msg)
            .setPositiveButton(R.string.delete_action) { _, _ ->
                deletePetFromFirestore(pet)
            }
            .setNegativeButton(R.string.cancel_action, null)
            .show()
    }

    private fun deletePetFromFirestore(pet: Pet) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarMyPets)
        progressBar?.visibility = View.VISIBLE

        petRepository.deletePet(pet.petId) { result ->
            progressBar?.visibility = View.GONE
            result.onSuccess {
                Toast.makeText(this, "Mascota ${pet.name} eliminada correctamente", Toast.LENGTH_SHORT).show()
                loadPetsFromFirestore()
            }.onFailure { e ->
                Toast.makeText(this, "Error al eliminar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        val openRegisterAction = View.OnClickListener { openRegisterPetScreen() }

        findViewById<View>(R.id.btnHeaderAddPet)?.setOnClickListener(openRegisterAction)
        findViewById<View>(R.id.btnAddPetCard)?.setOnClickListener(openRegisterAction)
        findViewById<View>(R.id.btnRegisterFirstPet)?.setOnClickListener(openRegisterAction)

        findViewById<View>(R.id.navHome)?.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.navPets)?.setOnClickListener {
            // Ya estamos en la pantalla Mis Mascotas
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
