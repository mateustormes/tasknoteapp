package com.tormesapp.tasknotesapp.activities.notes;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.tormesapp.tasknotesapp.R;
import com.tormesapp.tasknotesapp.activities.LoginActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class CreateNoteActivity extends AppCompatActivity {

    private OkHttpClient client;
    private EditText noteTitleEditText;
    private EditText noteContentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_note);

        client = new OkHttpClient();
        noteTitleEditText = findViewById(R.id.noteTitle);
        noteContentEditText = findViewById(R.id.noteContent);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            String title = noteTitleEditText.getText().toString().trim();
            String content = noteContentEditText.getText().toString().trim();
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(CreateNoteActivity.this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                createNote(title, content);
            }
        });
    }

    private void createNote(String title, String content) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/notes/create.php"; // Atualize com o endpoint correto


        int userId = LoginActivity.getUserId(this);
        RequestBody requestBody = new FormBody.Builder()
                .add("title", title)
                .add("content", content)
                .add("fk_usuario", String.valueOf(userId))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CreateNoteActivity.this, "Failed to create note", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateNoteActivity.this, "Note created successfully", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK); // Define o resultado como OK
                        finish(); // Fecha a atividade após a criação da nota
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(CreateNoteActivity.this, "Failed to create note: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
