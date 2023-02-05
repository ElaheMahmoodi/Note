package com.example.note;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.room.Database;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.note.Adapters.NoteListAdapter;
import com.example.note.Database.RoomDB;
import com.example.note.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{
    RecyclerView recyclerView;
    NoteListAdapter noteListAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add;
    SearchView searchView_home;
    Notes selectedNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);

        searchView_home = findViewById(R.id.searchView_home);

        database = RoomDB.getInstance(this);
        notes = database.mainDAO().getAll();

        updateRecycler(notes);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this , NotesTakerActivity.class);
                startActivityForResult(intent,101);


            }
        });

        searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

    }

    private void filter(String newText) {
        List<Notes> filteredList = new ArrayList<>();
        for (Notes singleNotes : notes){
            if (singleNotes.getTitle().toLowerCase().contains(newText.toLowerCase())
            || singleNotes.getNotes().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(singleNotes);

            }
        }
        noteListAdapter.filterList(filteredList);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101){
            if (resultCode == Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                noteListAdapter.notifyDataSetChanged();
            }
        }
        else if (requestCode == 102){
            if (resultCode == Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                database.mainDAO().update(new_notes.getID(),new_notes.getTitle(),new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                noteListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        noteListAdapter = new NoteListAdapter(MainActivity.this,notes,notesClickListener);
        recyclerView.setAdapter(noteListAdapter);

    }
    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this,NotesTakerActivity.class);
            intent.putExtra("old_note",notes);
            startActivityForResult(intent,102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNotes = new Notes();
            selectedNotes = notes;
            showPopup(cardView);
        }
    };

    private void showPopup(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.pin:
                if (selectedNotes.getPinned()){
                    database.mainDAO().pin(selectedNotes.getID(),false);
                    Toast.makeText(MainActivity.this, "Unpinned 📌", Toast.LENGTH_SHORT).show();
                }else{
                    database.mainDAO().pin(selectedNotes.getID(),true);
                    Toast.makeText(MainActivity.this, "Pinned 📌", Toast.LENGTH_SHORT).show();
                }

                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                noteListAdapter.notifyDataSetChanged();
                return true;

            case R.id.delete:
                database.mainDAO().delete(selectedNotes);
                notes.remove(selectedNotes);
                noteListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Note deleted 🧺", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return false;

        }
    }
}