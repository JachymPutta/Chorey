package com.chorey

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chorey.data.HomeModel
import com.chorey.adapter.ChoreRecyclerAdapter
import com.chorey.adapter.NoteRecyclerAdapter
import com.chorey.adapter.SummaryRecyclerAdapter
import com.chorey.data.ChoreModel
import com.chorey.data.DialogState
import com.chorey.data.HomeUserModel
import com.chorey.data.NoteModel
import com.chorey.databinding.FragmentHomeBinding
import com.chorey.dialog.ChoreDetailDialog
import com.chorey.dialog.HomeDetailDialog
import com.chorey.dialog.NoteDetailDialog
import com.chorey.util.OnSwipeTouchListener
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


/**
 * Fragment handling individual homes, it diesplays the chores and members associated with the
 * particular home.
 */
class HomeFragment : Fragment(),
    EventListener<DocumentSnapshot>,
    ChoreRecyclerAdapter.OnChoreSelectedListener,
    NoteRecyclerAdapter.OnNoteSelectedListener,
    SummaryRecyclerAdapter.OnSummarySelectedListener {

    private val args: HomeFragmentArgs by navArgs()
    private val viewModel by activityViewModels<LoginViewModel>()
    private lateinit var home : HomeModel

    private lateinit var homeRef: DocumentReference

    private lateinit var hrvAdapter: ChoreRecyclerAdapter
    private lateinit var noteAdapter: NoteRecyclerAdapter
    private lateinit var summaryAdapter: SummaryRecyclerAdapter
    private lateinit var swipeTouchListener: OnSwipeTouchListener
    //TODO: Swiping on individual items
//    private lateinit var recyclerSwipeAdapter: ItemTouchHelper

    private lateinit var choreQuery: Query
    private lateinit var noteQuery: Query
    private lateinit var summaryQuery: Query


    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding


    enum class CurFrag {
        HOME, BOARD, SUMMARY
    }
    private var curFrag = CurFrag.SUMMARY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Firebase.firestore

        homeRef = firestore.collection(HOME_COL).document(args.homeModel.UID)
        homeRef.get()
            .addOnSuccessListener(requireActivity()) {
                snapshot -> onHomeLoaded(snapshot.toObject<HomeModel>())
            }
            .addOnFailureListener(requireActivity()) {
                e -> Log.d(MenuFragment.TAG, "onCreateHome error: $e")
            }

        choreQuery = homeRef.collection(CHORE_COL)
            .orderBy(ChoreModel.FIELD_COMPLETED)
            .orderBy(ChoreModel.FIELD_WHEN_DUE)
        noteQuery = homeRef.collection(NOTE_COL)
        summaryQuery  = homeRef.collection(USER_COL)
            .orderBy(HomeUserModel.FIELD_POINTS, Query.Direction.DESCENDING)


        initAdapters()
        binding.frameLayout.visibility = GONE

        // Hooking up buttons
        binding.homeSettingsButton.setOnClickListener { settingsHandle() }
        binding.homeToMenuButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMenuFragment())
        }

        // Bottom navigation
        binding.homeSummaryButton.setOnClickListener { changeUI(CurFrag.SUMMARY) }
        binding.noticeBoardButton.setOnClickListener { changeUI(CurFrag.BOARD) }
        binding.homeChoreButton.setOnClickListener { changeUI(CurFrag.HOME) }

        //Swipe navigation
        binding.fragHomeLayout.setOnTouchListener(swipeTouchListener)
        binding.allChoresRecycler.setOnTouchListener(swipeTouchListener)
        //TODO: Swiping on individual items?
//        recyclerSwipeAdapter.attachToRecyclerView(binding.allChoresRecycler)
    }

    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (error != null) {
            Log.w(TAG, "home:onEvent ", error)
            return
        }

        value?.let {
            val homeModel = value.toObject<HomeModel>()
            if (homeModel != null) {
                onHomeLoaded(homeModel)
            }
        }
    }

    private fun addChoreHandle() {
        // Check if chore limit reached
        val numChores = hrvAdapter.itemCount
        if (numChores >= MAX_CHORES) {
            Toast.makeText(activity, "Max number of homes reached!", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a chore dialog
        ChoreDetailDialog(homeModel = home, null, DialogState.CREATE)
            .show(parentFragmentManager, ChoreDetailDialog.TAG)
    }

    /**
     * Selecting a currently active chore, returns the edit state
     */
    override fun onChoreSelected(chore: DocumentSnapshot) {
        val choreModel = chore.toObject<ChoreModel>()
        ChoreDetailDialog(homeModel = home, choreModel = choreModel, DialogState.EDIT)
            .show(parentFragmentManager, ChoreDetailDialog.TAG)
    }

    private fun addNoteHandle() {
        NoteDetailDialog(homeModel = home, noteModel = null, state = DialogState.CREATE)
            .show(parentFragmentManager, NoteDetailDialog.TAG)
    }
    override fun onNoteSelected(note: DocumentSnapshot) {
        val noteModel = note.toObject<NoteModel>()

        NoteDetailDialog(homeModel = home, noteModel = noteModel, state = DialogState.CREATE)
            .show(parentFragmentManager, NoteDetailDialog.TAG)
    }

    override fun onSummarySelected(summary: DocumentSnapshot) {
        // TODO: should this do something?
    }


    private fun settingsHandle() {
        HomeDetailDialog(home).show(parentFragmentManager, "HomeDetailDialog")
    }

    /**
     * Toggles the visibility of the UI elements based on the button clicked
     */
    private fun changeUI(nextFrag: CurFrag) {
        if (curFrag == nextFrag) {
            return
        }

        // Removes the highlight from the last tab
        when (curFrag) {
            CurFrag.HOME -> binding.homeChoreButton.setBackgroundColor(resources.getColor(android.R.color.transparent,null))
            CurFrag.BOARD -> binding.noticeBoardButton.setBackgroundColor(resources.getColor(android.R.color.transparent,null))
            CurFrag.SUMMARY -> binding.homeSummaryButton.setBackgroundColor(resources.getColor(android.R.color.transparent,null))
        }

        when (nextFrag) {
            CurFrag.HOME -> {
                // Visual
                binding.homeRecyclerTitle.setText(R.string.home_all_chores_text)
                binding.addChoreButton.visibility = VISIBLE
                binding.noChoresLeftText.setText(R.string.home_no_chores_left)
                binding.homeChoreButton.setBackgroundColor(resources.getColor(R.color.ivory, null))

                // Logic
                binding.allChoresRecycler.adapter = hrvAdapter
                binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.addChoreButton.setOnClickListener { addChoreHandle() }
            }
            CurFrag.SUMMARY -> {
                // Visual
                binding.homeRecyclerTitle.setText(R.string.home_summary_title_points)
                binding.noChoresLeftText.text = ""
                binding.addChoreButton.visibility = GONE
                binding.allChoresRecycler.visibility = VISIBLE
                binding.homeSummaryButton.setBackgroundColor(resources.getColor(R.color.ivory, null))

                // Logic
                binding.allChoresRecycler.adapter = summaryAdapter
                binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())

            }
            CurFrag.BOARD -> {
                // Visual
                binding.homeRecyclerTitle.setText(R.string.home_notes_title)
                binding.noChoresLeftText.setText(R.string.home_no_notes_left)
                binding.addChoreButton.visibility = VISIBLE
                binding.noticeBoardButton.setBackgroundColor(resources.getColor(R.color.ivory, null))

                // Logic
                binding.allChoresRecycler.adapter = noteAdapter
                binding.allChoresRecycler.layoutManager = GridLayoutManager(requireView().context, NOTE_COLUMN_CNT)
                binding.addChoreButton.setOnClickListener { addNoteHandle() }
            }
        }

        curFrag = nextFrag
    }

    override fun onStart() {
        super.onStart()
        hrvAdapter.startListening()
        noteAdapter.startListening()
        summaryAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        hrvAdapter.stopListening()
        noteAdapter.stopListening()
        summaryAdapter.stopListening()
    }

    private fun initAdapters() {
        hrvAdapter = object : ChoreRecyclerAdapter(choreQuery,
            this@HomeFragment, viewModel.user!!) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.noChoresLeftText.visibility = VISIBLE
                } else {
                    binding.noChoresLeftText.visibility = GONE
                }
            }
        }

        noteAdapter = object : NoteRecyclerAdapter(noteQuery, this@HomeFragment) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    binding.noChoresLeftText.visibility = VISIBLE
                } else {
                    binding.noChoresLeftText.visibility = GONE
                }
            }
        }

        summaryAdapter = SummaryRecyclerAdapter(summaryQuery, this@HomeFragment)

        swipeTouchListener = object : OnSwipeTouchListener(requireContext()){
            override fun onSwipeLeft() {
                when(curFrag){
                    CurFrag.HOME -> changeUI(CurFrag.SUMMARY)
                    CurFrag.BOARD -> changeUI(CurFrag.HOME)
                    CurFrag.SUMMARY -> {}
                }
            }

            override fun onSwipeRight() {
                when(curFrag){
                    CurFrag.HOME -> changeUI(CurFrag.BOARD)
                    CurFrag.BOARD -> {}
                    CurFrag.SUMMARY -> changeUI(CurFrag.HOME)
                }
            }
        }

        //TODO: Swiping on individual items
//        recyclerSwipeAdapter = ItemTouchHelper(
//            object : ItemTouchHelper.SimpleCallback(0,(LEFT or RIGHT)) {
//                override fun onMove(
//                    recyclerView: RecyclerView,
//                    viewHolder: RecyclerView.ViewHolder,
//                    target: RecyclerView.ViewHolder
//                ): Boolean {
//                    return false
//                }
//
//                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    when (direction) {
//                        LEFT -> Toast.makeText(requireContext(), "LEFT SWIPE", Toast.LENGTH_SHORT).show()
//                        RIGHT -> Toast.makeText(requireContext(), "RIGHT SWIPE", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        })
    }

    private fun onHomeLoaded(homeModel: HomeModel?) {
        if (homeModel == null)
            return

        home = homeModel
        binding.homeName.text = homeModel.homeName
        binding.frameLayout.visibility = VISIBLE
        binding.homeLoadingText.visibility = GONE

        changeUI(CurFrag.HOME)
    }

    companion object {
        const val TAG = "HomeFragment"
        const val NOTE_COLUMN_CNT = 2
    }

}