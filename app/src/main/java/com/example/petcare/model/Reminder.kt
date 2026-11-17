package com.example.petcare.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Reminder(
    var reminderId: String = "",
    var ownerId: String = "",
    var petId: String = "",
    var petName: String = "",
    var title: String = "",
    var type: String = "",
    var dateText: String = "",
    var timeText: String = "",
    var location: String = "",
    var doctorOrNotes: String = "",
    var tag: String = "",
    var isHero: Boolean = false,
    var isCompleted: Boolean = false,
    var dateMillis: Long = 0L,
    @ServerTimestamp
    var createdAt: Timestamp? = null
)
