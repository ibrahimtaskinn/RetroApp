package com.example.retroapp.data

import android.net.Uri
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import android.util.Log
import com.example.retroapp.data.model.Notes
import com.example.retroapp.data.model.Retro
import android.content.Context
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : StorageRepository {

    private val notesCollection: CollectionReference = firebaseFirestore.collection("notes")
    private val retroRef: CollectionReference = firebaseFirestore.collection("retro")
    override fun user() = auth.currentUser
    override fun hasUser(): Boolean = auth.currentUser != null
    override fun getUserId(): String = auth.currentUser?.uid.orEmpty()

    override fun getNotes(): Flow<Resource<List<Notes>>> = callbackFlow {
        val listenerRegistration: ListenerRegistration =
            notesCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notesList: List<Notes> = snapshot.toObjects(Notes::class.java)
                    trySend(Resource.Success(notesList))
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun getNoteById(
        noteId: String,
        onError: (Throwable?) -> Unit,
        onSuccess: (Notes?) -> Unit
    ) {
        notesCollection
            .document(noteId)
            .get()
            .addOnSuccessListener {
                onSuccess.invoke(it?.toObject(Notes::class.java))
            }
            .addOnFailureListener { result ->
                onError.invoke(result.cause)
            }
    }

    override suspend fun addNote(
        userId: String,
        username: String,
        title: String,
        description: String,
        images: List<Uri>,
        timestamp: Timestamp,
        type: String,
        onComplete: (Boolean) -> Unit
    ) {
        val id = notesCollection.document().id
        val list = mutableListOf<String>()

        val deferreds = images.map { uri ->
            CoroutineScope(Dispatchers.IO).async {
                val uid = uri.toString()
                val taskSnapshot = firebaseStorage.reference.child(uid).putFile(uri).await()
                val url = taskSnapshot.metadata?.reference?.downloadUrl?.await()
                url?.let { list.add(it.toString()) }
            }
        }
        deferreds.awaitAll()
        val note = Notes(
            id,
            userId,
            list,
            username,
            title,
            description,
            timestamp,
            type
        )
        notesCollection
            .document(id)
            .set(note)
            .addOnCompleteListener { result ->
                onComplete.invoke(result.isSuccessful)
            }
    }

    override suspend fun deleteNote(noteId: String, onComplete: (Boolean) -> Unit) {
        notesCollection.document(noteId)
            .delete()
            .addOnCompleteListener {
                onComplete.invoke(it.isSuccessful)
            }
    }

    override fun getFilteredNotes(
        searchText: String,
        filterType: String
    ): Flow<Resource<List<Notes>>> = callbackFlow {
        var snapshotStateListener: ListenerRegistration? = null
        try {
            var query = notesCollection.orderBy("timestamp")
            if (searchText.isNotEmpty()) {
                query = query.whereGreaterThanOrEqualTo("title", searchText)
            }
            if (filterType.isNotEmpty()) {
                query = query.whereEqualTo("type", filterType)
            }

            snapshotStateListener = query.addSnapshotListener { snapshot, e ->
                val response = if (snapshot != null) {
                    val notes = snapshot.toObjects(Notes::class.java)
                    Resource.Success(result = notes)
                } else {
                    e?.let { Resource.Failure(exception = it) }
                }
                response?.let { trySend(it) }
            }
        } catch (e: Exception) {
            trySend(Resource.Failure(e))
            e.printStackTrace()
        }
        awaitClose {
            snapshotStateListener?.remove()
        }
    }

    override suspend fun updateNote(
        title: String,
        note: String,
        noteId: String,
        images: List<Uri>,
        type: String,
        userId: String,
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        val list = mutableListOf<String>()
        val updateData = hashMapOf<String, Any>(
            "userId" to userId,
            "username" to username,
            "timestamp" to Timestamp.now(),
            "description" to note,
            "title" to title,
            "type" to type
        )
        if (images.isNotEmpty()) {
            val deferreds = images.map { uri ->
                CoroutineScope(Dispatchers.IO).async {
                    val uid = uri.toString()
                    if (uid.startsWith("https://firebasestorage.googleapis.com/")) {
                        list.add(uid)
                    } else {
                        val taskSnapshot = firebaseStorage.reference.child(uid).putFile(uri).await()
                        val url = taskSnapshot.metadata?.reference?.downloadUrl?.await()
                        url?.let { list.add(it.toString()) }
                    }
                }
            }
            deferreds.awaitAll()
            updateData["images"] = list
        } else {
            val currentNote = notesCollection.document(noteId).get().await()
            val currentImages = currentNote["images"] as? List<String>
            if (!currentImages.isNullOrEmpty()) {
                updateData["images"] = currentImages
            }
        }
        notesCollection.document(noteId)
            .update(updateData)
            .addOnCompleteListener {
                onResult(it.isSuccessful)
            }
    }

    override suspend fun getRetro(
        retroId: String,
        onError: (Throwable?) -> Unit,
        onSuccess: (Retro?) -> Unit
    ) {
        retroRef.document(retroId).get().addOnSuccessListener { retro ->
            if (retro != null) {
                onSuccess.invoke(retro.toObject(Retro::class.java))
            }
        }.addOnFailureListener {
            onError.invoke(it.cause)
        }
    }

    override suspend fun createRetro(
        admin: String,
        notes: List<Notes>,
        isActive: Boolean,
        title: String,
        time: Int,
        onComplete: (Boolean) -> Unit
    ) {
        val id = retroRef.document().id
        val endTimeSeconds = Timestamp.now().seconds + time * 60
        val endTime = Timestamp(endTimeSeconds, 0)
        val retro = Retro(id, admin, notes, isActive, title, time, endTime)
        retroRef.document(id)
            .set(retro)
            .addOnCompleteListener {
                onComplete.invoke(it.isSuccessful)
            }

        val workData = workDataOf("retroId" to id)
        val endRetroRequest = OneTimeWorkRequestBuilder<EndRetroWorker>()
            .setInitialDelay(time.toLong(), TimeUnit.MINUTES)
            .setInputData(workData)
            .addTag(id)
            .build()

        WorkManager.getInstance(context).enqueue(endRetroRequest)
    }

    override suspend fun isActive(): Flow<Boolean> = callbackFlow {
        val listenerRegistration = retroRef.whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(false)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val isActive = snapshot.documents.isNotEmpty()
                    trySend(isActive)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun getActiveRetroId(): Flow<String> = callbackFlow {
        val listenerRegistration = retroRef.whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.toObjects(Retro::class.java)
                    val id = list.get(0).id
                    trySend(id)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun addConfirmedNotes(retroId: String) {
        getRetro(retroId, onError = {}) { retro ->
            retro?.notes?.forEach {
                notesCollection
                    .document(it.id)
                    .set(it)
                    .addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Notes of retro succesfully saved",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }

    override suspend fun addNotesToRetro(retroId: String, notes: Notes) {
        notes.id = retroRef.document().id
        retroRef.document(retroId).update("notes", FieldValue.arrayUnion(notes))
            .addOnSuccessListener {
                Log.d("eklendi", "eklendi")
            }
            .addOnFailureListener {
                Log.d("fail", "fail")
            }
    }

    override suspend fun deleteNotesFromRetro(retroId: String, notes: Notes) {
        retroRef.document(retroId).update("notes", FieldValue.arrayRemove(notes))
            .addOnSuccessListener {
                Log.d("silindi", "silindi")
            }
            .addOnFailureListener {
                Log.d("fail", "fail")
            }
    }

    fun signOut() = auth.signOut()

    override suspend fun getUserNameById(userId: String): String? {
        return try {
            val userDocument = firebaseFirestore.collection("users").document(userId).get().await()
            userDocument.getString("username")
        } catch (e: Exception) {
            Log.d("getUserNameById", e.toString())
            null
        }
    }

    override suspend fun updateRetroTime(
        retroId: String,
        newTime: Int,
        onComplete: (Boolean) -> Unit
    ) {
        val endTimeSeconds = Timestamp.now().seconds + newTime * 60
        val endTime = Timestamp(endTimeSeconds, 0)
        retroRef.document(retroId)
            .update(mapOf("time" to newTime, "endTime" to endTime))
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    WorkManager.getInstance(context).cancelAllWorkByTag(retroId)

                    val workData = workDataOf("retroId" to retroId)
                    val endRetroRequest = OneTimeWorkRequestBuilder<EndRetroWorker>()
                        .setInitialDelay(newTime.toLong(), TimeUnit.MINUTES)
                        .setInputData(workData)
                        .addTag(retroId)
                        .build()

                    WorkManager.getInstance(context).enqueue(endRetroRequest)
                }

                onComplete.invoke(result.isSuccessful)
            }
    }

    override suspend fun deleteImage(
        noteId: String,
        imageUri: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (noteId.isBlank()) {
            Log.e("DeleteImage", "Note ID is blank")
            Toast.makeText(context, "Note ID is blank. Please provide a valid Note ID.", Toast.LENGTH_LONG).show()
            onComplete.invoke(false)
            return
        }

        val noteRef = notesCollection.document(noteId)
        val noteSnapshot = noteRef.get().await()
        val currentImages = noteSnapshot["images"] as? List<String>
        if (currentImages != null && currentImages.contains(imageUri)) {
            val storageRef = firebaseStorage.getReferenceFromUrl(imageUri)
            storageRef.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    noteRef.update("images", FieldValue.arrayRemove(imageUri))
                        .addOnCompleteListener { task2 ->
                            onComplete.invoke(task2.isSuccessful)
                        }
                } else {
                    Toast.makeText(context, "Failed to delete image from Firebase Storage.", Toast.LENGTH_LONG).show()
                    onComplete.invoke(false)
                }
            }.addOnFailureListener { exception ->
                Log.e("DeleteImage", "Failed to delete image: ", exception)
                Toast.makeText(context, "Failed to delete image: ${exception.message}", Toast.LENGTH_LONG).show()
                onComplete.invoke(false)
            }
        } else {
            Log.e("DeleteImage", "Image not found in Firebase")
            Toast.makeText(context, "Image not found in Firebase.", Toast.LENGTH_LONG).show()
            onComplete.invoke(false)
        }
    }
}