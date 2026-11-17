package com.example.petcare.repository

import com.example.petcare.model.Reminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ReminderRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private val localReminders = mutableListOf<Reminder>()
    }

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: "user_default"
    }

    fun saveReminder(reminder: Reminder, onComplete: (Result<String>) -> Unit) {
        val userId = getUserId()
        reminder.ownerId = userId

        val remindersRef = db.collection("users").document(userId).collection("reminders")
        val docRef = if (reminder.reminderId.isBlank()) remindersRef.document() else remindersRef.document(reminder.reminderId)
        reminder.reminderId = docRef.id

        localReminders.removeAll { it.reminderId == reminder.reminderId }
        localReminders.add(reminder)

        val reminderMap = hashMapOf<String, Any?>(
            "reminderId" to reminder.reminderId,
            "ownerId" to reminder.ownerId,
            "petId" to reminder.petId,
            "petName" to reminder.petName,
            "title" to reminder.title,
            "type" to reminder.type,
            "dateText" to reminder.dateText,
            "timeText" to reminder.timeText,
            "location" to reminder.location,
            "doctorOrNotes" to reminder.doctorOrNotes,
            "tag" to reminder.tag,
            "isHero" to reminder.isHero,
            "isCompleted" to reminder.isCompleted,
            "dateMillis" to reminder.dateMillis,
            "createdAt" to FieldValue.serverTimestamp()
        )

        docRef.set(reminderMap)
            .addOnSuccessListener {
                onComplete(Result.success(docRef.id))
            }
            .addOnFailureListener {
                onComplete(Result.success(docRef.id))
            }
    }

    fun deleteReminder(reminderId: String, onComplete: (Result<Unit>) -> Unit) {
        val userId = getUserId()
        localReminders.removeAll { it.reminderId == reminderId }

        db.collection("users").document(userId).collection("reminders").document(reminderId)
            .delete()
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener {
                onComplete(Result.success(Unit))
            }
    }

    fun getRemindersForCurrentUser(onComplete: (Result<List<Reminder>>) -> Unit) {
        val userId = getUserId()

        db.collection("users").document(userId).collection("reminders")
            .get()
            .addOnSuccessListener { snapshot ->
                val firestoreReminders = snapshot.documents.mapNotNull { doc ->
                    try {
                        Reminder(
                            reminderId = doc.getString("reminderId") ?: doc.id,
                            ownerId = doc.getString("ownerId") ?: userId,
                            petId = doc.getString("petId") ?: "",
                            petName = doc.getString("petName") ?: "",
                            title = doc.getString("title") ?: "",
                            type = doc.getString("type") ?: "",
                            dateText = doc.getString("dateText") ?: "",
                            timeText = doc.getString("timeText") ?: "",
                            location = doc.getString("location") ?: "",
                            doctorOrNotes = doc.getString("doctorOrNotes") ?: "",
                            tag = doc.getString("tag") ?: "",
                            isHero = doc.getBoolean("isHero") ?: false,
                            isCompleted = doc.getBoolean("isCompleted") ?: false,
                            dateMillis = doc.getLong("dateMillis") ?: 0L,
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }

                val combinedMap = mutableMapOf<String, Reminder>()
                for (r in localReminders) {
                    combinedMap[r.reminderId] = r
                }
                for (r in firestoreReminders) {
                    combinedMap[r.reminderId] = r
                }

                onComplete(Result.success(combinedMap.values.toList()))
            }
            .addOnFailureListener {
                onComplete(Result.success(localReminders.toList()))
            }
    }
}
