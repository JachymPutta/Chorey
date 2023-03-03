package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.databinding.DialogAddHomeBinding

class AddHomeDialog : DialogFragment() {
        private var _binding: DialogAddHomeBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = DialogAddHomeBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            binding.createNewHomeButton.setOnClickListener {
                CreateHomeDialog(requireParentFragment() as CreateHomeDialog.CreateHomeListener)
                    .show(parentFragmentManager, "CreateNewHome")
                this.dismiss()
            }
            binding.joinExistingButton.setOnClickListener {
                JoinHomeDialog().show(parentFragmentManager, "JoinExistingHome")
                this.dismiss()
            }
        }

        override fun onStart() {
            super.onStart()
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
}