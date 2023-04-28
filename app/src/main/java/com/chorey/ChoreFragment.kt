package com.chorey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.adapter.ChoreRecyclerAdapter
import com.chorey.data.ChoreModel
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentChoreBinding
import com.chorey.dialog.ChoreDetailDialog
import com.chorey.dialog.HomeDetailDialog
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class ChoreFragment(
    private val home : HomeModel,
    private val query: Query
) : Fragment(),
    EventListener<DocumentSnapshot>,
    ChoreRecyclerAdapter.OnChoreSelectedListener
{
    private val viewModel by activityViewModels<LoginViewModel>()
    private lateinit var choreAdapter: ChoreRecyclerAdapter
    private lateinit var binding: FragmentChoreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        choreAdapter = object : ChoreRecyclerAdapter(query,
            this@ChoreFragment, viewModel.user!!) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.noChoresLeftText.visibility = View.VISIBLE
                } else {
                    binding.noChoresLeftText.visibility = View.GONE
                }
            }
        }

        binding.homeName.text = home.homeName
        binding.homeToMenuButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMenuFragment())
        }

        binding.homeRecyclerTitle.setText(R.string.home_all_chores_text)
        binding.noChoresLeftText.setText(R.string.home_no_chores_left)

        //Active tab highlight
        binding.activeChoresButton.setBackgroundColor(resources.getColor(R.color.primaryColor, null))
        binding.activeChoresButton.setTextColor(resources.getColor(R.color.black, null))

        binding.addChoreButton.setOnClickListener { addChoreHandle() }
        binding.homeSettingsButton.setOnClickListener {
            HomeDetailDialog(home).show(parentFragmentManager, "HomeDetailDialog")
        }
        // TODO: add listener to change to history


        binding.allChoresRecycler.adapter = choreAdapter
        binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())
        choreAdapter.startListening()
    }

    private fun addChoreHandle() {
        // Check if chore limit reached
        val numChores = choreAdapter.itemCount
        if (numChores >= MAX_CHORES) {
            Toast.makeText(activity, "Max number of homes reached!", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a chore dialog
        ChoreDetailDialog(homeModel = home, null, DialogState.CREATE)
            .show(parentFragmentManager, ChoreDetailDialog.TAG)
    }

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        //TODO: not sure what this should do

    }

    override fun onChoreSelected(chore: DocumentSnapshot) {
        val choreModel = chore.toObject<ChoreModel>()
        ChoreDetailDialog(homeModel = home, choreModel = choreModel, DialogState.EDIT)
            .show(parentFragmentManager, ChoreDetailDialog.TAG)
    }
    companion object {
        const val TAG = "ChoreFragment"
    }
}