package com.tormesapp.tasknotesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.tormesapp.tasknotesapp.R;
import com.tormesapp.tasknotesapp.activities.notes.NoteActivity;
import com.tormesapp.tasknotesapp.activities.tasks.TaskActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnSair = findViewById(R.id.btnSair);
        Button noteButton = findViewById(R.id.btnCadastrarUser);
        Button btnTask = findViewById(R.id.btnTask);

        btnSair.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, LoginActivity.class));
        });

        btnTask.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, TaskActivity.class));
        });

        noteButton.setOnClickListener(view -> {
            startActivity(new Intent(MenuActivity.this, NoteActivity.class));
        });
    }
}