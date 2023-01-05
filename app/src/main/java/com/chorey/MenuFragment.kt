package com.chorey

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chorey.adapter.MenuRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.databinding.FragmentMenuBinding
import com.chorey.dialog.CreateNewHomeDialog
import com.chorey.util.AuthInitializer
import com.chorey.util.HomeUtil
import com.chorey.viewmodel.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment(),
    MenuRecyclerAdapter.OnHomeSelectedListener,
    CreateNewHomeDialog.CreateHomeListener {
    enum class HomeOperation {
        ADD, DELETE
    }

    private lateinit var mrvAdapter: MenuRecyclerAdapter
    private lateinit var binding: FragmentMenuBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private var query: Query? = null
    private var curOp = HomeOperation.ADD
    private val viewModel by viewModels<LoginViewModel>()

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

        // Enable logging
        FirebaseFirestore.setLoggingEnabled(true)
        // FireStore instance
        firestore = Firebase.firestore
        launcher = requireActivity()
            .registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
                    result -> this.onSignInResult(result)
            }
        query = firestore.collection("homes")

        query?.let {
            mrvAdapter = object : MenuRecyclerAdapter(it, this@MenuFragment) {
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
        observeAuthState()

        binding.addHomeButton.setOnClickListener{ addHomeHandle() }
        binding.removeHomeButton.setOnClickListener { removeHomeToggle() }
        binding.authButton.setOnClickListener { launchSignInFlow() }

    }
    override fun onStart() {
        super.onStart()

        // Start sign in if necessary
        if (!viewModel.isSigningIn && Firebase.auth.currentUser == null) {
            //TODO: Don't force sign-in, just stay on home screen ?
//            launchSignInFlow()
            makeWelcomeScreen()
            return
        }

        // Start listening for Firestore updates
        mrvAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mrvAdapter.stopListening()
    }

    override fun onHomeSelected(home: DocumentSnapshot) {
        if (curOp == HomeOperation.DELETE) {
            removeHomeUntoggle(home.reference)
        } else {
            val action = MenuFragmentDirections.actionMenuToHome().apply {
                homeId = home.id
            }
            findNavController().navigate(action)
        }

    }


    override fun onCreateHome(homeModel: HomeModel) {
        firestore.collection("homes").add(homeModel)

        Log.d(TAG, "Adding ${homeModel.homeName}")
    }


    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { authState ->
            when (authState) {
                // Changes for a logged in user
                LoginViewModel.AuthState.AUTHED -> { makeMenuScreen() }
                else -> { makeWelcomeScreen() }
                    //TODO: Change UI to welcome screen
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
            // TODO: Add more Sign-in methods
        )

        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()

        viewModel.isSigningIn = true
        launcher.launch(intent)
    }

    private fun onSignInResult(result : FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        viewModel.isSigningIn = false

        if (result.resultCode != Activity.RESULT_OK) {
            if (response == null) {
                requireActivity().finish()
            } else if (response.error != null ) {
                Log.d(TAG, "Error signing in: ${response.error}")
            }
        }

    }

    /**
     * Function to change the UI when there is no user logged in
     */
    private fun makeWelcomeScreen() {
        binding.allRoomsRecycler.visibility = View.GONE
        binding.addHomeButton.visibility = View.GONE
        binding.removeHomeButton.visibility = View.GONE
        binding.menuTitleText.setText(R.string.menu_title_welcome)
        binding.authButton.setText(R.string.auth_button_login)
        binding.authButton.setOnClickListener { launchSignInFlow() }
    }

    /**
     * Function to change the UI when there is a user authenticated
     */
    private fun makeMenuScreen() {
        binding.authButton.setText(R.string.auth_button_logout)
        binding.allRoomsRecycler.visibility = View.VISIBLE
        binding.addHomeButton.visibility = View.VISIBLE
        binding.removeHomeButton.visibility = View.VISIBLE
        binding.authButton.setOnClickListener {
            Firebase.auth.signOut()
            AuthUI.getInstance().signOut(requireContext())
        }
    }

    /**
     * Handles the addition of a new home, checks whether the max number of homes has been reached
     * and then continues through the dialogs
     */
    private fun addHomeHandle() {
        // Adding random homes instead - TESTING
        val homesRef = firestore.collection("homes")
        homesRef.add(HomeUtil.makeRandomHome(requireContext()))

        // Stop removing - on remove cancel
        if (curOp == HomeOperation.DELETE) {
            binding.menuTitleText.setText(R.string.menu_title_default)
            curOp = HomeOperation.ADD
        }

        val numHomes = mrvAdapter.itemCount
        Log.d(TAG, "Total number of homes = $numHomes")

        if (numHomes >= MAX_HOMES) {
            Toast.makeText(activity, "Max number of homes reached!", Toast.LENGTH_SHORT).show()
            return
        }

//        AddHomeDialog().show(parentFragmentManager, AddHomeDialog.TAG)
    }
    /**
     * Triggers the visual and logical changes for removing a home from the list
     */
    private fun removeHomeToggle() {
        // Already removing or no homes to remove
        if (curOp==HomeOperation.DELETE) return

        curOp = HomeOperation.DELETE
        val headText:TextView = binding.menuTitleText
        headText.text = getString(R.string.menu_title_remove)
    }

    /**
     * Reverts the changes done by {@link #removeHomeToggle() removeHomeToggle}
     * @param view: current view
     * @param homeModel: home being removed
     */
    private fun removeHomeUntoggle(home: DocumentReference) {
        //TODO: Show confirmation before removal
        home.delete()
        binding.menuTitleText.setText(R.string.menu_title_default)
        curOp = HomeOperation.ADD
    }


    companion object {
        const val TAG = "MenuFragment"
    }
}