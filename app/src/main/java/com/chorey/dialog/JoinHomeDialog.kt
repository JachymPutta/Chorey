package com.chorey.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.HOME_COL
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.adapter.JoinHomeRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.data.InviteModel
import com.chorey.databinding.DialogJoinHomeBinding
import com.chorey.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class JoinHomeDialog : DialogFragment(), JoinHomeRecyclerAdapter.OnJoinSelectedListener{

    private var joinHomeListener: JoinHomeListener? = null
    private var _binding: DialogJoinHomeBinding? = null
    private val binding get() = _binding!!
    private var query: Query? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var joinHomeAdapter: JoinHomeRecyclerAdapter

    private val viewModel by activityViewModels<LoginViewModel>()

    internal interface JoinHomeListener {
        fun onJoinClicked(homeModel: HomeModel)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DialogJoinHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Firebase.firestore

        query = firestore.collection(USER_COL).document(viewModel.user!!.UID)
            .collection("invites")

        Toast.makeText(requireContext(), viewModel.user!!.UID, Toast.LENGTH_LONG).show()

        query?.let {
            joinHomeAdapter = object : JoinHomeRecyclerAdapter(it, this@JoinHomeDialog) {
                override fun onDataChanged() {
                    if (itemCount == 0) {
//                        binding.joinHomeRecycler.visibility = View.GONE
                        binding.joinHomeTitle.setText(R.string.join_home_your_invites_empty)
                    } else {
//                        binding.joinHomeRecycler.visibility = View.VISIBLE
                        binding.joinHomeTitle.setText(R.string.join_home_your_invites_full)
                    }
                }

                override fun onError(e: FirebaseFirestoreException) {
                    Snackbar.make(binding.root, "Error: check logs for info",
                        Snackbar.LENGTH_LONG).show()
                }
            }

            binding.joinHomeRecycler.adapter = joinHomeAdapter
        }
        binding.joinHomeRecycler.layoutManager = LinearLayoutManager(view.context)


        binding.joinHomeDismissButton.setOnClickListener { dismiss() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (parentFragment is JoinHomeListener) {
            joinHomeListener = parentFragment as JoinHomeListener
        }
    }

    override fun onStart() {
        super.onStart()
        joinHomeAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        joinHomeAdapter.stopListening()
    }

    override fun onJoinSelected(invite: DocumentSnapshot) {
        val inviteModel = invite.toObject<InviteModel>()

        if (inviteModel == null) {
            Log.e(TAG, "Home/User not found!")
            return
        }

        val homeRef = firestore.collection(HOME_COL).document(inviteModel.homeUID)

        firestore.runTransaction {
            val snap = it.get(homeRef)
            @Suppress("UNCHECKED_CAST")
            val members = snap["users"] as Map<String, Int> + (viewModel.user!!.name to 0)

            it.update(homeRef, USER_COL, members)
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