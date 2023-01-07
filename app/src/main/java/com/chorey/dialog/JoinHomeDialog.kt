package com.chorey.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.R
import com.chorey.adapter.JoinHomeRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogJoinHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class JoinHomeDialog : DialogFragment(), JoinHomeRecyclerAdapter.OnJoinSelectedListener{

    private var joinHomeListener: JoinHomeListener? = null
    private var _binding: DialogJoinHomeBinding? = null
    private val binding get() = _binding!!
    private var query: Query? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var joinHomeAdapter: JoinHomeRecyclerAdapter

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
        query = firestore.collection("homes")

        query?.let {
            joinHomeAdapter = object : JoinHomeRecyclerAdapter(it, this@JoinHomeDialog) {
                override fun onDataChanged() {
                    if (itemCount == 0) {
                        binding.joinHomeRecycler.visibility = View.GONE
                        binding.joinHomeTitle.setText(R.string.join_home_your_invites_empty)
                    } else {
                        binding.joinHomeRecycler.visibility = View.VISIBLE
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
        binding.requestInviteButton.setOnClickListener { requestInviteHandle() }
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

    override fun onJoinSelected(home: DocumentSnapshot) {
        Firebase.firestore.collection("homes").add(home.reference)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onJoinClicked() {
        TODO("Hook up the join home button")

    }

    private fun requestInviteHandle() {
        TODO("Hook up the request invite button")
    }

    private fun onCancelClicked() {
        //TODO: Not sure what this cancel is - probably from the ROW
        dismiss()
    }

    companion object {
        const val TAG = "JoinHomeDialog"
    }
}