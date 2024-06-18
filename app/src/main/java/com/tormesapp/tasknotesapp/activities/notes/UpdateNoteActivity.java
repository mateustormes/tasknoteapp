package com.tormesapp.tasknotesapp.activities.notes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tormesapp.tasknotesapp.R;
import com.tormesapp.tasknotesapp.models.Note;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class UpdateNoteActivity extends AppCompatActivity {

    private OkHttpClient client;
    private EditText noteTitleEditText;
    private EditText noteContentEditText;
    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        client = new OkHttpClient();
        noteTitleEditText = findViewById(R.id.updateNoteTitle);
        noteContentEditText = findViewById(R.id.updateNoteContent);

        // Pega o ID da nota passada pela Intent
        Intent intent = getIntent();
        noteId = intent.getIntExtra("note_id", -1);
        if (noteId == -1) {
            Toast.makeText(this, "Note ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Carrega os detalhes da nota para preencher os campos
        loadNoteDetails(noteId);

        Button saveButton = findViewById(R.id.updateSaveButton);
        saveButton.setOnClickListener(view -> {
            String title = noteTitleEditText.getText().toString().trim();
            String content = noteContentEditText.getText().toString().trim();
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(UpdateNoteActivity.this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                updateNote(noteId, title, content);
            }
        });
    }

    private void loadNoteDetails(int noteId) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/notes/read_single.php?id=" + noteId; // Atualize com o endpoint correto

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UpdateNoteActivity.this, "Failed to load note details", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Gson gson = new Gson();
                    Note note = gson.fromJson(responseBody, Note.class);

                    runOnUiThread(() -> {
                        noteTitleEditText.setText(note.getTitle());
                        noteContentEditText.setText(note.getContent());
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(UpdateNoteActivity.this, "Failed to load note details: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateNote(int noteId, String title, String content) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/notes/update.php"; // Atualize com o endpoint correto

        RequestBody requestBody = new FormBody.Builder()
                .add("id", String.valueOf(noteId))
                .add("title", title)
                .add("content", content)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UpdateNoteActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(UpdateNoteActivity.this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK); // Define o resultado como OK
                        finish(); // Fecha a atividade após a atualização da nota
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(UpdateNoteActivity.this, "Failed to update note: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
