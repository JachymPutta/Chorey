package com.chorey.adapter

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

@SuppressLint("NotifyDataSetChanged")
abstract class FirestoreAdapter<VH: RecyclerView.ViewHolder>(private var query: Query) :
    RecyclerView.Adapter<VH>(), EventListener<QuerySnapshot> {

    private var registration: ListenerRegistration? = null
    private val snapshots = ArrayList<DocumentSnapshot>()


    fun startListening() {
        if (registration == null) {
            registration = query.addSnapshotListener(this)
        }
    }

    fun stopListening() {
        registration?.remove()
        registration = null

        snapshots.clear()
        notifyDataSetChanged()
    }

    fun setQuery(query: Query) {
        // Stop listening
        stopListening()

        // Clear existing data
        snapshots.clear()
        notifyDataSetChanged()

        this.query = query
        startListening()
    }

    override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        // Handle errors
        if (e != null) {
            Log.w(TAG, "onEvent:error", e)
            return
        }

        // Dispatch the event
        if (documentSnapshots != null) {
            for (change in documentSnapshots.documentChanges) {
                // snapshot of the changed document
                when (change.type) {
                    DocumentChange.Type.ADDED -> {
                        onDocumentAdded(change)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        onDocumentModified(change)
                    }
                    DocumentChange.Type.REMOVED -> {
                        onDocumentRemoved(change)
                    }
                }
            }
        }
        onDataChanged()
    }

    private fun onDocumentAdded(change: DocumentChange) {
        snapshots.add(change.newIndex, change.document)
        notifyItemInserted(change.newIndex)
    }

    private fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            snapshots[change.oldIndex] = change.document
            notifyItemChanged(change.oldIndex)
        } else {
            // Item changed and changed position
            snapshots.removeAt(change.oldIndex)
            snapshots.add(change.newIndex, change.document)
            notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    private fun onDocumentRemoved(change: DocumentChange) {
        snapshots.removeAt(change.oldIndex)
        notifyItemRemoved(change.oldIndex)
    }

    open fun onError(e : FirebaseFirestoreException) {
        Log.w(TAG, "onError", e)
    }

    open fun onDataChanged(){}

    override fun getItemCount(): Int {
        return snapshots.size
    }

    protected fun getSnapshot(pos: Int) : DocumentSnapshot {
        return snapshots[pos]
    }

    companion object {
        private const val TAG = "FirestoreAdapter"
    }
}