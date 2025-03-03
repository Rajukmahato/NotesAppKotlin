package com.example.notes.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.auth.SignInActivity
import com.example.notes.databinding.ActivityDashboardBinding
import com.example.notes.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var notesAdapter: NotesAdapter
    private val notes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("notes")

        setupRecyclerView()
        loadNotes()

        binding.fabAddNote.setOnClickListener {
            showNoteDialog()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(notes) { note ->
            showNoteDialog(note)
        }
        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = notesAdapter
        }

        // Swipe to delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = notes[viewHolder.adapterPosition]
                deleteNote(note)
            }
        }).attachToRecyclerView(binding.rvNotes)
    }

    private fun loadNotes() {
        val userId = auth.currentUser?.uid ?: return
        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notes.clear()
                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(Note::class.java)
                        note?.let { notes.add(it) }
                    }
                    notes.sortByDescending { it.timestamp }
                    notesAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DashboardActivity, "Failed to load notes", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showNoteDialog(note: Note? = null) {
        NoteDialog(this, note) { title, content ->
            if (note == null) {
                createNote(title, content)
            } else {
                updateNote(note.id, title, content)
            }
        }.show()
    }

    private fun createNote(title: String, content: String) {
        val userId = auth.currentUser?.uid ?: return
        val noteId = database.push().key ?: return
        val note = Note(noteId, title, content, System.currentTimeMillis(), userId)
        
        database.child(noteId).setValue(note)
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create note", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateNote(noteId: String, title: String, content: String) {
        database.child(noteId).updateChildren(
            mapOf(
                "title" to title,
                "content" to content,
                "timestamp" to System.currentTimeMillis()
            )
        ).addOnFailureListener {
            Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNote(note: Note) {
        database.child(note.id).removeValue()
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
            }
    }
} 