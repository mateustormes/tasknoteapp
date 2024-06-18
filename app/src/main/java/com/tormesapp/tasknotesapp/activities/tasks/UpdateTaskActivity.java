package com.tormesapp.tasknotesapp.activities.tasks;

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
import com.tormesapp.tasknotesapp.activities.notes.UpdateNoteActivity;
import com.tormesapp.tasknotesapp.models.Note;
import com.tormesapp.tasknotesapp.models.Task;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateTaskActivity extends AppCompatActivity {
    private OkHttpClient client;
    private EditText taskTitleEditText;
    private EditText taskContentEditText;
    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        client = new OkHttpClient();
        taskTitleEditText = findViewById(R.id.updateTaskTitle);
        taskContentEditText = findViewById(R.id.updateTaskContent);

        // Pega o ID da nota passada pela Intent
        Intent intent = getIntent();
        taskId = intent.getIntExtra("task_id", -1);
        if (taskId == -1) {
            Toast.makeText(this, "Task ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Carrega os detalhes da nota para preencher os campos
        loadTasksDetails(taskId);

        Button saveButton = findViewById(R.id.updateSaveButton);
        saveButton.setOnClickListener(view -> {
            String title = taskTitleEditText.getText().toString().trim();
            String description = taskContentEditText.getText().toString().trim();
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(UpdateTaskActivity.this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                updateNote(taskId, title, description);
            }
        });
    }

    private void loadTasksDetails(int noteId) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/tasks/read_single.php?id=" + noteId; // Atualize com o endpoint correto

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UpdateTaskActivity.this, "Failed to load task details", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Gson gson = new Gson();
                    Task task = gson.fromJson(responseBody, Task.class);

                    runOnUiThread(() -> {
                        taskTitleEditText.setText(task.getTitle());
                        taskContentEditText.setText(task.getDescription());
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(UpdateTaskActivity.this, "Failed to load task details: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateNote(int taskId, String title, String description) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/tasks/update.php"; // Atualize com o endpoint correto

        RequestBody requestBody = new FormBody.Builder()
                .add("id", String.valueOf(taskId))
                .add("title", title)
                .add("description", description)
                .add("is_completed ", "0")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UpdateTaskActivity.this, "Failed to update task", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(UpdateTaskActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK); // Define o resultado como OK
                        finish(); // Fecha a atividade após a atualização da nota
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(UpdateTaskActivity.this, "Failed to update task: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}