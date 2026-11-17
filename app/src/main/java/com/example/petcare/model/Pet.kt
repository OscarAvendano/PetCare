package com.example.petcare.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Pet(
    var petId: String = "",
    var ownerId: String = "",
    var name: String = "",
    var species: String = "",
    var breed: String = "",
    var birthDate: String = "",
    var sex: String = "",
    var weight: Double = 0.0,
    var color: String = "",
    var sterilized: String = "",
    var microchip: String = "",
    var observations: String = "",
    var photoUrl: String = "",
    @ServerTimestamp
    var createdAt: Timestamp? = null,
    @ServerTimestamp
    var updatedAt: Timestamp? = null
)
