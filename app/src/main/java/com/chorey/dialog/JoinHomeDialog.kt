package com.chorey.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.HOME_COL
import com.chorey.INVITE_COL
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.adapter.JoinHomeRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.data.HomeUserModel
import com.chorey.data.InviteModel
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.DialogJoinHomeBinding
import com.chorey.viewmodel.UserViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class JoinHomeDialog : DialogFragment(),
    JoinHomeRecyclerAdapter.OnJoinSelectedListener{

    private var _binding: DialogJoinHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var query: Query
    private lateinit var firestore: FirebaseFirestore
    private lateinit var joinHomeAdapter: JoinHomeRecyclerAdapter

    private val viewModel by activityViewModels<UserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DialogJoinHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Firebase.firestore

        query = firestore.collection(USER_COL).document(viewModel.user.value!!.UID)
            .collection(INVITE_COL)

        joinHomeAdapter = object : JoinHomeRecyclerAdapter(query, this@JoinHomeDialog) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.joinHomeRecycler.visibility = View.GONE
                    binding.askForInviteText.visibility = View.VISIBLE
                    binding.joinHomeTitle.setText(R.string.join_home_your_invites_empty)
                } else {
                    binding.joinHomeRecycler.visibility = View.VISIBLE
                    binding.askForInviteText.visibility = View.GONE
                    binding.joinHomeTitle.setText(R.string.join_home_your_invites_full)
                }
            }
        }

        binding.joinHomeRecycler.adapter = joinHomeAdapter
        binding.joinHomeRecycler.layoutManager = LinearLayoutManager(view.context)
        binding.joinHomeDismissButton.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        joinHomeAdapter.startListening()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onStop() {
        super.onStop()
        joinHomeAdapter.stopListening()
    }

    override fun onJoinSelected(invite: DocumentSnapshot) {
        val inviteModel = invite.toObject<InviteModel>()
        val user = viewModel.user.value!!

        if (inviteModel == null) {
            Log.e(TAG, "Home/User not found!")
            return
        }

        val db = Firebase.firestore
        val homeRef = db.collection(HOME_COL).document(inviteModel.homeUID)
        val userRef = db.collection(USER_COL).document(user.UID)
        val homeUsersRef = homeRef.collection(USER_COL).document(user.name)
        user.memberOf[inviteModel.homeUID] = inviteModel.homeName

        // Update the database -> $user joins $home
        db.runTransaction {
            val snapshot = it.get(homeRef)
            val homeModel = snapshot.toObject<HomeModel>()
            val homeUserModel = HomeUserModel(name = user.name)

            // Add user to members array
            homeModel!!.users.add(user.name)

            it.set(homeRef, homeModel)

            // Add user to 'users' sub-collection
            it.set(homeUsersRef, homeUserModel)
            it.update(userRef, LoggedUserModel.FIELD_MEMBER_OF, user.memberOf)
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "JoinHomeDialog"
    }
}