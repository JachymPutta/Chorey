package com.chorey.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.R
import com.chorey.data.HomeModel
import com.chorey.databinding.DialogJoinHomeBinding

class JoinHomeDialog : DialogFragment(){

    private var joinHomeListener: JoinHomeListener? = null
    private var _binding: DialogJoinHomeBinding? = null
    private val binding get() = _binding!!

    internal interface JoinHomeListener {
        fun onJoinClicked(homeModel: HomeModel)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DialogJoinHomeBinding.inflate(inflater, container, false)

        //TODO: Hook up the buttons

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (parentFragment is JoinHomeListener) {
            joinHomeListener = parentFragment as JoinHomeListener
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onJoinClicked() {

    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "JoinHomeDialog"
    }
}