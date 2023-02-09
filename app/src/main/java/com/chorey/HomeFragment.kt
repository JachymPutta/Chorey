package com.chorey

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
import com.chorey.data.HomeModel
import com.chorey.adapter.ChoreRecyclerAdapter
import com.chorey.adapter.NoteRecyclerAdapter
import com.chorey.adapter.SummaryRecyclerAdapter
import com.chorey.data.ChoreModel
import com.chorey.data.NoteModel
import com.chorey.databinding.FragmentHomeBinding
import com.chorey.viewmodel.LoginViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.local.QueryEngine
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


    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding


    enum class CurFrag {
        HOME, BOARD, SUMMARY
    }
    private var curFrag = CurFrag.HOME

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

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

        val choreQuery: Query = homeRef.collection(CHORE_COL).orderBy("whenDue")
        val noteQuery: Query = homeRef.collection(NOTE_COL)
        val summaryQuery : Query = homeRef.collection(USER_COL)
            .orderBy("points", Query.Direction.DESCENDING)

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

        noteAdapter = NoteRecyclerAdapter(noteQuery, this@HomeFragment)
        summaryAdapter = SummaryRecyclerAdapter(summaryQuery, this@HomeFragment)

        binding.frameLayout.visibility = GONE

        binding.allChoresRecycler.adapter = hrvAdapter
        binding.allChoresRecycler.layoutManager = LinearLayoutManager(view.context)

        // Hooking up buttons
        binding.addChoreButton.setOnClickListener { addChoreHandle() }
        binding.addMemberButton.setOnClickListener { addMemberHandle() }
        binding.homeToMenuButton.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMenuFragment())
        }

        // Bottom navigation
        binding.homeSummaryButton.setOnClickListener { changeUI(CurFrag.SUMMARY) }
        binding.noticeBoardButton.setOnClickListener { changeUI(CurFrag.BOARD) }
        binding.homeChoreButton.setOnClickListener { changeUI(CurFrag.HOME) }

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
        val action = HomeFragmentDirections.actionHomeFragmentToCreateChoreDialog(home)
        findNavController().navigate(action)
    }

    override fun onChoreSelected(chore: DocumentSnapshot) {
        val action = HomeFragmentDirections.actionHomeFragmentToCreateChoreDialog(home).apply {
            choreModel = chore.toObject<ChoreModel>()
        }
        findNavController().navigate(action)
    }

    private fun addNoteHandle() {
        val action = HomeFragmentDirections.actionHomeFragmentToNoteDetailDialog(home)
        findNavController().navigate(action)
    }
    override fun onNoteSelected(note: DocumentSnapshot) {
        val action = HomeFragmentDirections.actionHomeFragmentToNoteDetailDialog(home).apply {
            noteModel = note.toObject<NoteModel>()
        }
        findNavController().navigate(action)
    }

    override fun onSummarySelected(summary: DocumentSnapshot) {
        // TODO: should this do something?
    }

    private fun addMemberHandle() {
        val action = HomeFragmentDirections.actionHomeFragmentToAddMemberDialog(home)
        findNavController().navigate(action)
    }

    /**
     * Toggles the visibility of the UI elements based on the button clicked
     */
    private fun changeUI(nextFrag: CurFrag) {
       if (curFrag == nextFrag) {
           return
       }

        when (nextFrag) {
            CurFrag.HOME -> {
                binding.homeRecyclerTitle.setText(R.string.home_all_chores_text)
                binding.allChoresRecycler.adapter = hrvAdapter
                binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())

                binding.addChoreButton.visibility = VISIBLE
                binding.addChoreButton.setText(R.string.add_chore_button)
                binding.addChoreButton.setOnClickListener { addChoreHandle() }
            }
            CurFrag.SUMMARY -> {
                binding.homeRecyclerTitle.setText(R.string.home_summary_title)
                binding.allChoresRecycler.adapter = summaryAdapter
                binding.allChoresRecycler.layoutManager = LinearLayoutManager(requireContext())

                binding.addChoreButton.visibility = GONE
            }
            CurFrag.BOARD -> {
                binding.homeRecyclerTitle.setText(R.string.home_notes_title)

                binding.allChoresRecycler.adapter = noteAdapter
                binding.allChoresRecycler.layoutManager = GridLayoutManager(requireView().context, NOTE_COLUMN_CNT)

                binding.addChoreButton.visibility = VISIBLE
                binding.addChoreButton.setText(R.string.add_note_button)
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

    private fun onHomeLoaded(homeModel: HomeModel?) {
        if (homeModel == null)
            return

        home = homeModel
        binding.homeName.text = homeModel.homeName
        binding.frameLayout.visibility = VISIBLE
        binding.homeLoadingText.visibility = GONE

    }

    private fun toggleRecycler(state : Int) {
        binding.allChoresRecycler.visibility = state
        binding.noChoresLeftText.visibility = state
        binding.addChoreButton.visibility = state
    }

    companion object {
        const val TAG = "HomeFragment"
        const val NOTE_COLUMN_CNT = 2
    }

}