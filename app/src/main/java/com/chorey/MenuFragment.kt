package com.chorey

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chorey.adapter.MenuRecyclerViewAdapter
import com.chorey.viewmodel.HomeViewModel
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentMenuBinding
import com.chorey.dialog.AddHomeDialog
import com.chorey.util.HomeUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment(),
    MenuRecyclerViewAdapter.OnHomeSelectedListener {
    private lateinit var mrvAdapter: MenuRecyclerViewAdapter
    private lateinit var binding: FragmentMenuBinding
    lateinit var firestore: FirebaseFirestore
    private var query: Query? = null
    //TODO Change this to an ENUM for more readability
    private var removeHome = false

//    private val signInLauncher = registerForActivityResult(
//        FirebaseAuthUIActivityResultContract()
//    ) { result -> this.onSignInResult(result) }
    private val viewModel: HomeViewModel by activityViewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.all_rooms_recycler)

        // Enable logging
        FirebaseFirestore.setLoggingEnabled(true)
        // FireStore instance
        firestore = Firebase.firestore

        query = firestore.collection("homes")

        query?.let {
            mrvAdapter = object : MenuRecyclerViewAdapter(it, this@MenuFragment) {
                override fun onDataChanged() {
                    // Change UI based on the number of homes present
                    if (itemCount == 0) {
                        binding.allRoomsRecycler.visibility = View.GONE
                    } else {
                        binding.allRoomsRecycler.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: FirebaseFirestoreException) {
                    Snackbar.make(binding.root, "Error: check logs for info",
                        Snackbar.LENGTH_LONG).show()
                }
            }

            binding.allRoomsRecycler.adapter = mrvAdapter
        }

        binding.allRoomsRecycler.layoutManager = LinearLayoutManager(view.context)

        // Begin the dialogues of creating/joining a home
        binding.addHomeButton.setOnClickListener{addHomeHandle()}
        binding.removeHomeButton.setOnClickListener { removeHomeToggle() }
    }

    /**
     * Handles the addition of a new home, checks whether the max number of homes has been reached
     * and then continues through the dialogs
     */
    fun addHomeHandle() {
        // MAX_HOMES has been reached
        val homesRef = firestore.collection("homes")
        if(viewModel.getHomes() != null && viewModel.getHomes()!!.size >= MAX_HOMES) {
            Toast.makeText(activity, "Max number of homes reached!", Toast.LENGTH_SHORT).show()
            return
        }

        homesRef.add(HomeUtil.makeRandomHome(requireContext()))
        AddHomeDialog().show(parentFragmentManager, "AddHome")
    }


//    override fun onCreateHome(dialogFragment: DialogFragment) {
//        val text = requireView().findViewById<TextInputLayout>(R.id.createHomeNameInput)!!
//            .editText?.text
//        Log.d("CreateHome Dialog", " New home name: $text")
//
//        val home = HomeModel()
//        home.homeId = text.toString()
//        viewModel.addHome(home)
//
//        mrvAdapter.notifyItemInserted(viewModel.getSize() - 1 )
//    }


    /**
     * Triggers the visual and logical changes for removing a home from the list
     */
    fun removeHomeToggle() {
        // Already removing or no homes to remove
        if (removeHome || viewModel.getHomes() == null) return

        removeHome = true
        val headText:TextView = binding.menuTitleText
        headText.text = getString(R.string.menu_title_remove)
    }

    /**
     * Reverts the changes done by {@link #removeHomeToggle() removeHomeToggle}
     * @param view: current view
     * @param homeModel: home being removed
     */
    fun removeHomeUntoggle(view: View, homeModel: HomeModel) {
        //TODO: Show confirmation before removal
        viewModel.removeHome(homeModel)
        mrvAdapter.notifyItemRemoved(viewModel.getPos(homeModel))
//        mrvAdapter.homes = viewModel.getHomes()!!

        val headText:TextView = view.findViewById(R.id.menuTitleText)
        headText.text = getString(R.string.menu_title_default)
        removeHome = false
    }
}