package com.chorey

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.adapter.MenuRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.data.UserModel
import com.chorey.databinding.FragmentMenuBinding
import com.chorey.dialog.AddHomeDialog
import com.chorey.dialog.ConfirmRemoveDialog
import com.chorey.viewmodel.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment(),
    MenuRecyclerAdapter.OnHomeSelectedListener {
    enum class HomeOperation {
        ADD, DELETE
    }

    private lateinit var mrvAdapter: MenuRecyclerAdapter
    private lateinit var binding: FragmentMenuBinding
    private lateinit var firestore: FirebaseFirestore

    private lateinit var confirmRemoveDialog: ConfirmRemoveDialog
    private lateinit var addHomeDialog: AddHomeDialog

    private var query: Query? = null
    private var curOp = HomeOperation.ADD

    private val viewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Re-enable logging - disabled while working on other stuff
        // Enable logging
//        FirebaseFirestore.setLoggingEnabled(true)

        // FireStore instance
        firestore = Firebase.firestore
        query = firestore.collection("homes")

        query?.let {
            mrvAdapter = object : MenuRecyclerAdapter(it, this@MenuFragment) {
                override fun onDataChanged() {
                    // Change UI based on the number of homes present
                    if (itemCount == 0) {
                        binding.allRoomsRecycler.visibility = View.GONE
                        binding.menuEmptyRecyclerText.visibility = View.VISIBLE
                    } else {
                        binding.allRoomsRecycler.visibility = View.VISIBLE
                        binding.menuEmptyRecyclerText.visibility = View.GONE
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

        confirmRemoveDialog = ConfirmRemoveDialog()
        addHomeDialog = AddHomeDialog()

        binding.addHomeButton.setOnClickListener{ addHomeHandle() }
        binding.removeHomeButton.setOnClickListener { removeHomeToggle() }
        binding.authButton.setOnClickListener {launchSignInFlow() }
    }
    override fun onStart() {
        super.onStart()

        // Start sign in if necessary
        if (needSignIn()) {
            makeWelcomeScreen()
            return
        }

        checkUserName()

        // Start listening for Firestore updates
        mrvAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mrvAdapter.stopListening()
    }

    override fun onHomeSelected(home: DocumentSnapshot) {
        home.reference.get()
            .addOnSuccessListener { doc ->
                val homeVal = doc.toObject<HomeModel>()

                if (curOp == HomeOperation.DELETE) {
                    confirmRemoveDialog.snapshot = home
                    confirmRemoveDialog.name = homeVal!!.homeName
                    confirmRemoveDialog.show(childFragmentManager, ConfirmRemoveDialog.TAG)
                    removeHomeToggle()
                } else {
                    val action = MenuFragmentDirections.actionMenuToHome(homeVal!!)
                    findNavController().navigate(action)
                }
            }.addOnFailureListener{
                e -> Log.d(TAG, "Error fetching home from snap: $e!")
            }

    }

    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { authState ->
            when (authState) {
                // Changes for a logged in user
                LoginViewModel.AuthState.AUTHED -> { makeMenuScreen() }
                else -> { makeWelcomeScreen() }
            }
        }
    }

    private fun needSignIn() = Firebase.auth.currentUser == null

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
            // TODO: Add more Sign-in methods
        )

        // TODO Customize this!
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()

        viewModel.launcher.launch(intent)
    }

    /**
     * Function to change the UI when there is no user logged in
     */
    private fun makeWelcomeScreen() {
        binding.allRoomsRecycler.visibility = View.GONE
        binding.addHomeButton.visibility = View.GONE
        binding.removeHomeButton.visibility = View.GONE
        binding.menuEmptyRecyclerText.visibility = View.GONE
        binding.menuTitleText.setText(R.string.menu_title_welcome)
        binding.authButton.setText(R.string.auth_button_login)
        binding.authButton.setOnClickListener { launchSignInFlow() }
    }

    /**
     * Function to change the UI when there is a user authenticated
     */
    private fun makeMenuScreen() {
        // UI changes
        binding.authButton.setText(R.string.auth_button_logout)
        binding.allRoomsRecycler.visibility = View.VISIBLE
        binding.addHomeButton.visibility = View.VISIBLE
        binding.removeHomeButton.visibility = View.VISIBLE
        binding.menuEmptyRecyclerText.visibility = View.VISIBLE
        binding.authButton.setOnClickListener {
            Firebase.auth.signOut()
            AuthUI.getInstance().signOut(requireContext())
        }
    }

    private fun checkUserName() {
        val user = Firebase.auth.currentUser
        if(user == null) {
            Log.e(TAG, "checkUserName got a null user")
            return
        }

        firestore.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { ds ->
                if (!ds.exists() || user.displayName == null) {
                    getUserNameDialog()
                } else {
                    viewModel.user = ds.toObject<UserModel>()
                }
            }
            .addOnFailureListener {
                e -> Log.e(TAG, "Error fetching user $e")
            }

    }
    private fun getUserNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val nameInput = EditText(requireContext())

        nameInput.inputType = InputType.TYPE_CLASS_TEXT
        builder.setTitle("Your name:")
            .setView(nameInput)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ ->
               if (nameInput.text.toString().isBlank()) {
                   Toast.makeText(requireContext(), "Please input a name.", Toast.LENGTH_SHORT).show()
               } else {
                   val userModel = UserModel(
                       UID = Firebase.auth.currentUser!!.uid,
                       name = nameInput.text.toString()
                   )
                   viewModel.user = userModel
                   firestore.collection("users").document(userModel.UID).set(userModel)
                   dialog.dismiss()
               }
            }

        builder.show()
    }

    /**
     * Handles the addition of a new home, checks whether the max number of homes has been reached
     * and then continues through the dialogs
     */
    private fun addHomeHandle() {
        // Adding random homes instead - TESTING
//        val homesRef = firestore.collection("homes")
//        homesRef.add(HomeUtil.makeRandomHome(requireContext()))

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

        addHomeDialog.show(parentFragmentManager, AddHomeDialog.TAG)
    }

    /**
     * Triggers the visual and logical changes for removing a home from the list
     */
    private fun removeHomeToggle() {
        // Already removing or no homes to remove
        if (curOp == HomeOperation.DELETE) {
            curOp = HomeOperation.ADD
            binding.menuTitleText.setText(R.string.menu_title_default)
        } else {
            curOp = HomeOperation.DELETE
            binding.menuTitleText.setText(R.string.menu_title_remove)
        }
    }

    companion object {
        const val TAG = "MenuFragment"
    }
}