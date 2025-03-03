package com.example.notes.dashboard

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.example.notes.databinding.DialogNoteBinding
import com.example.notes.model.Note

class NoteDialog(
    context: Context,
    private val note: Note? = null,
    private val onSave: (String, String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-fill if editing
        note?.let {
            binding.etTitle.setText(it.title)
            binding.etContent.setText(it.content)
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val content = binding.etContent.text.toString()
            
            if (title.isNotEmpty() && content.isNotEmpty()) {
                onSave(title, content)
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
} 