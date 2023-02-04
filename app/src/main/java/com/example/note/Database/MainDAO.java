package com.example.note.Database;

import static androidx.room.OnConflictStrategy.REPLACE;

import android.hardware.lights.LightState;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.note.Models.Notes;

import java.util.List;

@Dao
public interface MainDAO {

    @Insert(onConflict = REPLACE)
    void insert(Notes notes);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Notes> getAll();

    @Query("UPDATE notes SET title = :title,notes = :notes WHERE ID = :id")
    void update(int id,String title , String notes);

    @Delete
    void delete(Notes notes);

}
