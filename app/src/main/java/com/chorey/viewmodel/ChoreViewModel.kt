package com.chorey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chorey.CHORE_COL
import com.chorey.HOME_COL
import com.chorey.data.ChoreModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChoreViewModel : ViewModel() {
    private val _chore = MutableLiveData<ChoreModel>()
    val chore: LiveData<ChoreModel> = _chore

    fun updateChore(newUser : ChoreModel) {
        _chore.value = newUser
    }

    fun resetChore() {
        _chore.value = ChoreModel()
    }

    fun addChoreToHome() {
        Firebase.firestore.collection(HOME_COL).document(chore.value!!.homeId)
            .collection(CHORE_COL).document(chore.value!!.choreUID).set(chore.value!!)
    }
}