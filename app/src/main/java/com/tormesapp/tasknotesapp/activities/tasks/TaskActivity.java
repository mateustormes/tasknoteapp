package com.tormesapp.tasknotesapp.activities.tasks;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tormesapp.tasknotesapp.R;
import com.tormesapp.tasknotesapp.activities.LoginActivity;
import com.tormesapp.tasknotesapp.models.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class TaskActivity extends AppCompatActivity {

    private OkHttpClient client;
    private static final int CREATE_TASK_REQUEST_CODE = 1;
    private static final int UPDATE_TASK_REQUEST_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        client = new OkHttpClient();

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(TaskActivity.this, CreateTaskActivity.class);
            startActivityForResult(intent, CREATE_TASK_REQUEST_CODE);
        });

        loadTasks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_TASK_REQUEST_CODE || requestCode == UPDATE_TASK_REQUEST_CODE) && resultCode == RESULT_OK) {
            // Recarregar a lista de tarefas
            loadTasks();
        }
    }
    static class TaskDeserializer implements JsonDeserializer<Task> {
        @Override
        public Task deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Task task = new Task();
            task.setId(Integer.parseInt(jsonObject.get("id").getAsString()));
            task.setTitle(jsonObject.get("title").getAsString());
            task.setDescription(jsonObject.get("description").getAsString());
            task.setCompleted(jsonObject.get("is_completed").getAsString().equals("1") ? "true" : "false");
            return task;
        }
    }
    private void loadTasks() {

        int userId = LoginActivity.getUserId(this);
        String url = "https://ethernalpet.com/TaskNotesAppBackend/tasks/read.php?fk_usuario="+String.valueOf(userId); // Atualize com o endpoint correto

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(TaskActivity.this, "Failed to load tasks", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Task.class, new TaskDeserializer())
                            .create();

                    Task[] tasks = gson.fromJson(responseBody, Task[].class);
                    // No método loadTasks()
                    runOnUiThread(() -> {
                        LinearLayout taskListLayout = findViewById(R.id.noteListLayout);
                        taskListLayout.removeAllViews(); // Limpa a lista antes de adicionar novos elementos

                        for (Task task : tasks) {
                            // Cria um LinearLayout para cada tarefa
                            LinearLayout taskLayout = new LinearLayout(TaskActivity.this);
                            taskLayout.setOrientation(LinearLayout.VERTICAL);
                            taskLayout.setBackgroundResource(R.drawable.note_background);
                            taskLayout.setPadding(16, 16, 16, 16);
                            taskLayout.setElevation(4);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(0, 0, 0, 16);
                            taskLayout.setLayoutParams(layoutParams);

                            LinearLayout statusLayout = new LinearLayout(TaskActivity.this);
                            statusLayout.setOrientation(LinearLayout.HORIZONTAL);
                            // Cria um CheckBox para marcar como concluída
                            CheckBox checkBox = new CheckBox(TaskActivity.this);
                            checkBox.setChecked(Boolean.parseBoolean(task.isCompleted()));
                            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                updateTaskStatus(task.getId(), isChecked);
                            });
                            checkBox.setTextColor(getResources().getColor(android.R.color.white));
                            checkBox.setText("Tarefa Realizada?");
                            statusLayout.addView(checkBox);

                            // Adiciona o layout do status checkbox ao layout da tarefa
                            taskLayout.addView(statusLayout);

                            // Cria um LinearLayout horizontal para o título
                            LinearLayout titleLayout = new LinearLayout(TaskActivity.this);
                            titleLayout.setOrientation(LinearLayout.HORIZONTAL);



                            // Cria um TextView para exibir o título da tarefa
                            TextView titleView = new TextView(TaskActivity.this);
                            titleView.setText(task.getTitle());
                            titleView.setTextSize(16);
                            titleView.setTextColor(getResources().getColor(android.R.color.white));
                            titleLayout.addView(titleView);

                            // Adiciona o layout do título ao layout da tarefa
                            taskLayout.addView(titleLayout);

                            // Cria um TextView para exibir a descrição da tarefa
                            TextView descriptionView = new TextView(TaskActivity.this);
                            descriptionView.setText(task.getDescription());
                            descriptionView.setTextSize(14);
                            descriptionView.setTextColor(getResources().getColor(android.R.color.white));
                            taskLayout.addView(descriptionView);

                            // Cria um LinearLayout horizontal para os botões
                            LinearLayout buttonLayout = new LinearLayout(TaskActivity.this);
                            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                            buttonLayout.setPadding(0, 16, 0, 0);

                            // Cria um botão de editar
                            Button editButton = new Button(TaskActivity.this);
                            editButton.setText("Edit");
                            editButton.setTextColor(getResources().getColor(android.R.color.white));
                            editButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.button_color)));
                            editButton.setOnClickListener(view -> {
                                Intent intent = new Intent(TaskActivity.this, UpdateTaskActivity.class);
                                intent.putExtra("task_id", task.getId()); // Passa o ID da tarefa para a atividade de atualização
                                startActivityForResult(intent, UPDATE_TASK_REQUEST_CODE);
                            });
                            buttonLayout.addView(editButton);

                            // Espaçamento entre os botões
                            Space space = new Space(TaskActivity.this);
                            LinearLayout.LayoutParams spaceLayoutParams = new LinearLayout.LayoutParams(16, 0);
                            buttonLayout.addView(space, spaceLayoutParams);

                            // Cria um botão de remover
                            Button removeButton = new Button(TaskActivity.this);
                            removeButton.setText("Remove");
                            removeButton.setTextColor(getResources().getColor(android.R.color.white));
                            removeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.button_color)));
                            removeButton.setOnClickListener(view -> {
                                // Exibir uma popup de confirmação
                                AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
                                builder.setMessage("Deseja realmente remover esta tarefa?")
                                        .setPositiveButton("Sim", (dialogInterface, i) -> {
                                            // Chamamos o método para remover a tarefa
                                            removeTask(task.getId());
                                        })
                                        .setNegativeButton("Não", null) // Nada acontece se clicar em "Não"
                                        .show();
                            });
                            buttonLayout.addView(removeButton);

                            // Adiciona o layout dos botões ao layout da tarefa
                            taskLayout.addView(buttonLayout);

                            // Adiciona o layout da tarefa à lista de tarefas
                            taskListLayout.addView(taskLayout);
                        }
                    });

                } else {
                    runOnUiThread(() -> Toast.makeText(TaskActivity.this, "Failed to load tasks: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateTaskStatus(int taskId, boolean isCompleted) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/tasks/updateStatus.php"; // Atualize com o endpoint correto

        RequestBody requestBody = new FormBody.Builder()
                .add("id", String.valueOf(taskId))
                .add("is_completed", isCompleted ? "1" : "0")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(TaskActivity.this, "Failed to update task status", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(TaskActivity.this, "Task status updated", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(TaskActivity.this, "Failed to update task status: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void removeTask(int taskId) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/tasks/delete.php"; // Atualize com o endpoint correto

        RequestBody requestBody = new FormBody.Builder()
                .add("id", String.valueOf(taskId))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(TaskActivity.this, "Failed to remove task", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(TaskActivity.this, "Task removed successfully", Toast.LENGTH_SHORT).show();
                        loadTasks(); // Recarregar a lista de tarefas após a remoção
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(TaskActivity.this, "Failed to remove task: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
