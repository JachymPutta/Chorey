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
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.adapter.ChoreHistoryAdapter
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
    ChoreHistoryAdapter.OnHistorySelectedListener,
    NoteRecyclerAdapter.OnNoteSelectedListener,
    SummaryRecyclerAdapter.OnSummarySelectedListener {

    private val args: HomeFragmentArgs by navArgs()
    private val viewModel by activityViewModels<LoginViewModel>()
    private lateinit var home : HomeModel

    private lateinit var homeRef: DocumentReference

    private lateinit var hrvAdapter: ChoreRecyclerAdapter
    private lateinit var historyAdapter: ChoreHistoryAdapter
    private lateinit var noteAdapter: NoteRecyclerAdapter
    private lateinit var summaryAdapter: SummaryRecyclerAdapter
    private lateinit var swipeTouchListener: OnSwipeTouchListener
    //TODO: Swiping on individual items
//    private lateinit var recyclerSwipeAdapter: ItemTouchHelper

    private lateinit var choreQuery: Query
    private lateinit var historyQuery: Query
    private lateinit var noteQuery: Query
    private lateinit var summaryQuery: Query


    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding


    enum class CurFrag {
        HOME, BOARD, SUMMARY
    }

    enum class ChoreType {
        ACTIVE, COMPLETED
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

        // DB queries
        firestore = Firebase.firestore
        homeRef = firestore.collection(HOME_COL).document(args.homeModel.UID)

        choreQuery = homeRef.collection(CHORE_COL)
            .whereEqualTo(ChoreModel.FIELD_COMPLETED, 0)
            .orderBy(ChoreModel.FIELD_WHEN_DUE)

        historyQuery = homeRef.collection(CHORE_COL)
            .whereGreaterThanOrEqualTo(ChoreModel.FIELD_COMPLETED, 1)
            .orderBy(ChoreModel.FIELD_COMPLETED)
            .orderBy(ChoreModel.FIELD_WHEN_DUE, Query.Direction.DESCENDING)

        noteQuery = homeRef.collection(NOTE_COL)

        summaryQuery  = homeRef.collection(USER_COL)
            .orderBy(HomeUserModel.FIELD_POINTS, Query.Direction.DESCENDING)

        initAdapters()


        homeRef.get()
            .addOnSuccessListener(requireActivity()) {
                snapshot -> onHomeLoaded(snapshot.toObject<HomeModel>())
            }
            .addOnFailureListener(requireActivity()) {
                e -> Log.d(MenuFragment.TAG, "onCreateHome error: $e")
            }

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

    private fun onHomeLoaded(homeModel: HomeModel?) {
        if (homeModel == null)
            return

        home = homeModel
        binding.homeName.text = homeModel.homeName
        binding.frameLayout.visibility = VISIBLE
        binding.homeLoadingText.visibility = GONE

        changeUI(CurFrag.HOME)
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

    /**
     * Button Click Handlers
     */
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

    private fun addNoteHandle() {
        NoteDetailDialog(homeModel = home, noteModel = null, state = DialogState.CREATE)
            .show(parentFragmentManager, NoteDetailDialog.TAG)
    }

    private fun settingsHandle() {
        HomeDetailDialog(home).show(parentFragmentManager, "HomeDetailDialog")
    }

    /**
     * Clicking recycler items
     */
    override fun onChoreSelected(chore: DocumentSnapshot) {
        val choreModel = chore.toObject<ChoreModel>()
        ChoreDetailDialog(homeModel = home, choreModel = choreModel, DialogState.EDIT)
            .show(parentFragmentManager, ChoreDetailDialog.TAG)
    }

    override fun onHistorySelected(chore: DocumentSnapshot) {
        val choreModel = chore.toObject<ChoreModel>()
        ChoreDetailDialog(homeModel = home, choreModel = choreModel, DialogState.VIEW)
            .show(parentFragmentManager, ChoreDetailDialog.TAG)
    }

    override fun onNoteSelected(note: DocumentSnapshot) {
        val noteModel = note.toObject<NoteModel>()

        NoteDetailDialog(homeModel = home, noteModel = noteModel, state = DialogState.CREATE)
            .show(parentFragmentManager, NoteDetailDialog.TAG)
    }

    override fun onSummarySelected(summary: DocumentSnapshot) {
        // TODO: should this do something?
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
                binding.homeChoreButton.setBackgroundColor(resources.getColor(R.color.ivory, null))
                //TODO: Hook up the history toggle button here

                choreTypeToggle(ChoreType.ACTIVE)
            }
            CurFrag.SUMMARY -> {
                // Visual
                binding.homeRecyclerTitle.setText(R.string.home_summary_title_points)
                binding.noChoresLeftText.text = ""
                binding.addChoreButton.visibility = GONE
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

    private fun choreTypeToggle(choreType : ChoreType) {
        when(choreType){
            ChoreType.ACTIVE -> {
                binding.homeRecyclerTitle.setText(R.string.home_all_chores_text)
                binding.noChoresLeftText.setText(R.string.home_no_chores_left)

                binding.addChoreButton.visibility = VISIBLE
                binding.allChoresRecycler.adapter = hrvAdapter
                binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.addChoreButton.setOnClickListener { addChoreHandle() }
            }
            ChoreType.COMPLETED -> {

                binding.allChoresRecycler.adapter = historyAdapter
                binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())

                binding.addChoreButton.visibility = GONE

            }
        }
    }

    override fun onStart() {
        super.onStart()
        hrvAdapter.startListening()
        noteAdapter.startListening()
        summaryAdapter.startListening()
        historyAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        hrvAdapter.stopListening()
        noteAdapter.stopListening()
        summaryAdapter.stopListening()
        historyAdapter.stopListening()
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

        historyAdapter = ChoreHistoryAdapter(historyQuery,  this@HomeFragment, viewModel.user!!)

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


    companion object {
        const val TAG = "HomeFragment"
        const val NOTE_COLUMN_CNT = 2
    }


}