package com.tormesapp.tasknotesapp.activities.tasks;

import android.app.Activity;
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
import com.tormesapp.tasknotesapp.activities.LoginActivity;
import com.tormesapp.tasknotesapp.activities.notes.CreateNoteActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateTaskActivity extends AppCompatActivity {
    private OkHttpClient client;
    private EditText taskTitleEditText;
    private EditText taskDescriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_tasks);

        client = new OkHttpClient();
        taskTitleEditText = findViewById(R.id.taskTitle);
        taskDescriptionEditText = findViewById(R.id.taskDescription);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> {
            String title = taskTitleEditText.getText().toString().trim();
            String description = taskDescriptionEditText.getText().toString().trim();
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(CreateTaskActivity.this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                createTask(title, description);
            }
        });
    }

    private void createTask(String title, String description) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/tasks/create.php"; // Atualize com o endpoint correto

        int userId = LoginActivity.getUserId(this);
        RequestBody requestBody = new FormBody.Builder()
                .add("title", title)
                .add("description", description)
                .add("is_completed ", "0")
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
                runOnUiThread(() -> Toast.makeText(CreateTaskActivity.this, "Failed to create task", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateTaskActivity.this, "Task created successfully", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK); // Define o resultado como OK
                        finish(); // Fecha a atividade após a criação da nota
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(CreateTaskActivity.this, "Failed to create task: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}