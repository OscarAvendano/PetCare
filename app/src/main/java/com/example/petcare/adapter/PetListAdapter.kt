package com.example.petcare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.petcare.R
import com.example.petcare.model.Pet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PetListAdapter(
    private val onItemClick: (Pet) -> Unit,
    private val onEditClick: (Pet) -> Unit,
    private val onDeleteClick: (Pet) -> Unit
) : ListAdapter<Pet, PetListAdapter.PetViewHolder>(PetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet_card, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgPetCardAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvPetCardName)
        private val tvSpeciesBadge: TextView = itemView.findViewById(R.id.tvPetCardSpeciesBadge)
        private val tvBreed: TextView = itemView.findViewById(R.id.tvPetCardBreed)
        private val tvBoxBreed: TextView = itemView.findViewById(R.id.tvPetCardBoxBreed)
        private val tvBoxAge: TextView = itemView.findViewById(R.id.tvPetCardBoxAge)
        private val tvBoxWeight: TextView = itemView.findViewById(R.id.tvPetCardBoxWeight)
        private val tvHealthText: TextView = itemView.findViewById(R.id.tvPetCardHealthText)
        private val btnMenu: View = itemView.findViewById(R.id.btnPetCardMenu)
        private val btnFullProfile: View = itemView.findViewById(R.id.btnPetCardFullProfile)

        fun bind(pet: Pet) {
            tvName.text = pet.name.ifBlank { "Mascota" }
            tvSpeciesBadge.text = pet.species.ifBlank { "Mascota" }
            tvBreed.text = pet.breed.ifBlank { "Raza no registrada" }
            tvBoxBreed.text = pet.breed.ifBlank { "No reg." }

            // Cálculo dinámico de edad
            tvBoxAge.text = calculateAge(pet.birthDate)

            // Peso
            tvBoxWeight.text = if (pet.weight > 0) "${pet.weight} kg" else "No reg."

            // Estado de salud
            tvHealthText.text = "• Perfil de ${pet.name} al día"

            // Carga de imagen con Coil
            if (pet.photoUrl.isNotBlank()) {
                imgAvatar.load(pet.photoUrl) {
                    crossfade(true)
                    error(if (pet.species.equals("Gato", true)) R.drawable.ic_cat_avatar else R.drawable.ic_dog_avatar)
                }
            } else {
                imgAvatar.setImageResource(if (pet.species.equals("Gato", true)) R.drawable.ic_cat_avatar else R.drawable.ic_dog_avatar)
            }

            val clickAction = View.OnClickListener { onItemClick(pet) }
            itemView.setOnClickListener(clickAction)
            btnFullProfile.setOnClickListener(clickAction)

            btnMenu.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Editar")
                popup.menu.add("Eliminar")
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Editar" -> onEditClick(pet)
                        "Eliminar" -> onDeleteClick(pet)
                    }
                    true
                }
                popup.show()
            }
        }

        private fun calculateAge(birthDateStr: String): String {
            if (birthDateStr.isBlank()) return "No reg."
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
                    years > 0 && months > 0 -> "${years}a ${months}m"
                    years > 0 -> "$years ${if (years == 1) "año" else "años"}"
                    months > 0 -> "$months ${if (months == 1) "mes" else "meses"}"
                    else -> "Recién nacido"
                }
            } catch (_: Exception) {
                birthDateStr
            }
        }
    }

    class PetDiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem.petId == newItem.petId
        }

        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem == newItem
        }
    }
}
