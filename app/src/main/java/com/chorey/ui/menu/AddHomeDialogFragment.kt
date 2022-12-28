package com.chorey.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.chorey.R

/**
 * Dialogue for creating a new home or joining an existing one
 * TODO: Change this to appear in the center of the screen instead of the bottom?
 */
class AddHomeDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_home_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a new home - become admin
        view.findViewById<Button>(R.id.createNewHomeButton).setOnClickListener {
            findNavController().navigate(R.id.action_addHomeDialogFragment_to_createNewHomeDialogFragment)
        }

        view.findViewById<Button>(R.id.joinExistingHomeButton).setOnClickListener {
            //TODO: Joining existing homes -> add a unique ID
//            findNavController().navigate(R.id.action_addHomeDialogFragment_to_createNewHomeDialogFragment)
        }
    }

}