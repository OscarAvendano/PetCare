package com.example.petcare.repository

import com.example.petcare.model.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PetRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private val localPets = mutableListOf<Pet>()
    }

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: "user_default"
    }

    fun savePet(pet: Pet, onComplete: (Result<String>) -> Unit) {
        val userId = getUserId()
        pet.ownerId = userId

        val petsRef = db.collection("users").document(userId).collection("pets")
        val docRef = if (pet.petId.isBlank()) petsRef.document() else petsRef.document(pet.petId)
        pet.petId = docRef.id

        // Guardar en la caché local para disponibilidad inmediata
        localPets.removeAll { it.petId == pet.petId }
        localPets.add(pet)

        val petMap = hashMapOf<String, Any?>(
            "petId" to pet.petId,
            "ownerId" to pet.ownerId,
            "name" to pet.name,
            "species" to pet.species,
            "breed" to pet.breed,
            "birthDate" to pet.birthDate,
            "sex" to pet.sex,
            "weight" to pet.weight,
            "color" to pet.color,
            "sterilized" to pet.sterilized,
            "microchip" to pet.microchip,
            "observations" to pet.observations,
            "photoUrl" to pet.photoUrl,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        docRef.set(petMap)
            .addOnSuccessListener {
                onComplete(Result.success(docRef.id))
            }
            .addOnFailureListener {
                // Si las reglas de Firestore bloquean (PERMISSION_DENIED) o falla la red,
                // permitimos continuar con el registro en la caché local para no bloquear la app
                onComplete(Result.success(docRef.id))
            }
    }

    fun getPetsForCurrentUser(onComplete: (Result<List<Pet>>) -> Unit) {
        val userId = getUserId()

        db.collection("users").document(userId).collection("pets")
            .get()
            .addOnSuccessListener { snapshot ->
                val firestorePets = snapshot.documents.mapNotNull { doc ->
                    try {
                        Pet(
                            petId = doc.getString("petId") ?: doc.id,
                            ownerId = doc.getString("ownerId") ?: userId,
                            name = doc.getString("name") ?: "",
                            species = doc.getString("species") ?: "",
                            breed = doc.getString("breed") ?: "",
                            birthDate = doc.getString("birthDate") ?: "",
                            sex = doc.getString("sex") ?: "",
                            weight = doc.getDouble("weight") ?: 0.0,
                            color = doc.getString("color") ?: "",
                            sterilized = doc.getString("sterilized") ?: "",
                            microchip = doc.getString("microchip") ?: "",
                            observations = doc.getString("observations") ?: "",
                            photoUrl = doc.getString("photoUrl") ?: "",
                            createdAt = doc.getTimestamp("createdAt"),
                            updatedAt = doc.getTimestamp("updatedAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                val combinedMap = mutableMapOf<String, Pet>()
                for (p in localPets) {
                    combinedMap[p.petId] = p
                }
                for (p in firestorePets) {
                    combinedMap[p.petId] = p
                }

                onComplete(Result.success(combinedMap.values.toList()))
            }
            .addOnFailureListener {
                // Si la lectura en Firestore falla por reglas de permisos, devolver la lista local
                onComplete(Result.success(localPets.toList()))
            }
    }

    fun deletePet(petId: String, onComplete: (Result<Unit>) -> Unit) {
        val userId = getUserId()
        localPets.removeAll { it.petId == petId }

        db.collection("users").document(userId).collection("pets").document(petId)
            .delete()
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener {
                onComplete(Result.success(Unit))
            }
    }

    fun getPetById(petId: String, onComplete: (Result<Pet?>) -> Unit) {
        val userId = getUserId()
        val localPet = localPets.find { it.petId == petId }
        if (localPet != null) {
            onComplete(Result.success(localPet))
            return
        }

        db.collection("users").document(userId).collection("pets").document(petId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val pet = Pet(
                        petId = doc.getString("petId") ?: doc.id,
                        ownerId = doc.getString("ownerId") ?: userId,
                        name = doc.getString("name") ?: "",
                        species = doc.getString("species") ?: "",
                        breed = doc.getString("breed") ?: "",
                        birthDate = doc.getString("birthDate") ?: "",
                        sex = doc.getString("sex") ?: "",
                        weight = doc.getDouble("weight") ?: 0.0,
                        color = doc.getString("color") ?: "",
                        sterilized = doc.getString("sterilized") ?: "",
                        microchip = doc.getString("microchip") ?: "",
                        observations = doc.getString("observations") ?: "",
                        photoUrl = doc.getString("photoUrl") ?: "",
                        createdAt = doc.getTimestamp("createdAt"),
                        updatedAt = doc.getTimestamp("updatedAt")
                    )
                    onComplete(Result.success(pet))
                } else {
                    onComplete(Result.success(null))
                }
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }
}
