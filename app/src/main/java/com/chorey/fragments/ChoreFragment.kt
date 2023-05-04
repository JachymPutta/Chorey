package com.chorey.fragments

import android.app.Application
import android.content.res.Configuration
import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.Chorey
import com.chorey.MAX_CHORES
import com.chorey.R
import com.chorey.adapter.ChoreHistoryAdapter
import com.chorey.adapter.ChoreRecyclerAdapter
import com.chorey.data.ChoreModel
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentChoreBinding
import com.chorey.dialog.ChoreDetailDialog
import com.chorey.dialog.HistoryDetailDialog
import com.chorey.dialog.HomeDetailDialog
import com.chorey.viewmodel.UserViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class ChoreFragment(
    private val home : HomeModel,
    private val choreQuery: Query,
    private val historyQuery: Query
) : Fragment(),
    ChoreRecyclerAdapter.OnChoreSelectedListener,
    ChoreHistoryAdapter.OnHistorySelectedListener
{
    private val viewModel by activityViewModels<UserViewModel>()
    private lateinit var choreAdapter: ChoreRecyclerAdapter
    private lateinit var historyAdapter: ChoreHistoryAdapter

    private lateinit var binding: FragmentChoreBinding

    private var curChores = ChoreType.COMPLETED

    enum class ChoreType {
        ACTIVE, COMPLETED
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.noChoresLeftText.visibility = GONE

        choreAdapter = object : ChoreRecyclerAdapter(choreQuery,
            this@ChoreFragment, viewModel.user.value!!) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.noChoresLeftText.visibility = VISIBLE
                } else {
                    binding.noChoresLeftText.visibility = GONE
                }
            }
        }

        historyAdapter = ChoreHistoryAdapter(historyQuery,this@ChoreFragment)

        binding.activeChoresButton.setOnClickListener { choreTypeToggle(ChoreType.ACTIVE) }
        binding.historyChoresButton.setOnClickListener { choreTypeToggle(ChoreType.COMPLETED) }


        binding.addChoreButton.setOnClickListener { addChoreHandle() }

        curChores = ChoreType.COMPLETED
        choreTypeToggle(ChoreType.ACTIVE)
    }

    override fun onStart() {
        super.onStart()
        choreAdapter.startListening()
        historyAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        choreAdapter.stopListening()
        historyAdapter.stopListening()
    }
    private fun choreTypeToggle(choreType : ChoreType) {
        if(curChores == choreType) return
        var textColor = 0

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                textColor = resources.getColor(R.color.black, null)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                textColor = resources.getColor(R.color.ivory, null)
            }
        }

        when(curChores) {
            ChoreType.ACTIVE -> {
                binding.activeChoresButton.paintFlags = binding.activeChoresButton.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
            ChoreType.COMPLETED -> {
                binding.historyChoresButton.paintFlags = binding.historyChoresButton.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }

        when(choreType){
            ChoreType.ACTIVE -> {
                binding.noChoresLeftText.setText(R.string.home_no_chores_left)

                //Active tab highlight
                binding.activeChoresButton.paintFlags = binding.activeChoresButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                binding.addChoreButton.visibility = VISIBLE
                binding.addChoreButton.setOnClickListener { addChoreHandle() }


                binding.allChoresRecycler.adapter = choreAdapter
                binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())
            }
            ChoreType.COMPLETED -> {
                binding.noChoresLeftText.text = ""

                binding.addChoreButton.visibility = GONE

                //Active tab highlight
                binding.historyChoresButton.paintFlags = binding.historyChoresButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                binding.allChoresRecycler.adapter = historyAdapter
                binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        curChores = choreType
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

    override fun onChoreSelected(chore: DocumentSnapshot) {
        val choreModel = chore.toObject<ChoreModel>()
        ChoreDetailDialog(homeModel = home, choreModel = choreModel, DialogState.EDIT)
            .show(parentFragmentManager, ChoreDetailDialog.TAG)
    }

    override fun onHistorySelected(chore: DocumentSnapshot) {
        val choreModel = chore.toObject<ChoreModel>()
        HistoryDetailDialog(choreModel!!).show(parentFragmentManager, HistoryDetailDialog.TAG)
    }
    companion object {
        const val TAG = "ChoreFragment"
    }
}