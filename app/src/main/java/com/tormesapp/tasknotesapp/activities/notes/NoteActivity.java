package com.tormesapp.tasknotesapp.activities.notes;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tormesapp.tasknotesapp.R;
import com.tormesapp.tasknotesapp.activities.LoginActivity;
import com.tormesapp.tasknotesapp.activities.tasks.CreateTaskActivity;
import com.tormesapp.tasknotesapp.activities.tasks.TaskActivity;
import com.tormesapp.tasknotesapp.models.Note;
import com.google.gson.Gson;
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

public class NoteActivity extends AppCompatActivity {

    private OkHttpClient client;
    private static final int CREATE_NOTE_REQUEST_CODE = 1;
    private static final int UPDATE_NOTE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        client = new OkHttpClient();

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(NoteActivity.this, CreateNoteActivity.class);
            startActivityForResult(intent, CREATE_NOTE_REQUEST_CODE);
        });

        loadNotes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_NOTE_REQUEST_CODE || requestCode == UPDATE_NOTE_REQUEST_CODE) && resultCode == RESULT_OK) {
            // Recarregar a lista de tarefas
            loadNotes();
        }
    }

    // Método para remover a nota
    private void removeNote(int noteId) {
        String url = "https://ethernalpet.com/TaskNotesAppBackend/notes/delete.php"; // Atualize com o endpoint correto

        // Crie o corpo da requisição POST com o ID da nota a ser removida
        RequestBody requestBody = new FormBody.Builder()
                .add("id", String.valueOf(noteId))
                .build();

        // Crie a requisição
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // Envie a requisição
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(NoteActivity.this, "Failed to remove note", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    // Imprima a resposta do servidor para depuração
                    System.out.println("Server response: " + responseBody);
                    runOnUiThread(() -> {
                        Toast.makeText(NoteActivity.this, "Note removed successfully", Toast.LENGTH_SHORT).show();
                        // Recarregue a lista de notas após a remoção
                        loadNotes();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(NoteActivity.this, "Failed to remove note: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void loadNotes() {

        int userId = LoginActivity.getUserId(this);
        String url = "http://www.ethernalpet.com/TaskNotesAppBackend/notes/read.php?fk_usuario="+String.valueOf(userId); // Atualize com o endpoint correto

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(NoteActivity.this, "Failed to load notes", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Note>>() {}.getType();
                    List<Note> notes = gson.fromJson(responseBody, listType);

                    runOnUiThread(() -> {
                        LinearLayout noteListLayout = findViewById(R.id.noteListLayout);
                        noteListLayout.removeAllViews(); // Limpa a lista antes de adicionar novos elementos

                        for (Note note : notes) {
                            // Cria um LinearLayout para cada nota com um estilo mais bonito
                            LinearLayout noteLayout = new LinearLayout(NoteActivity.this);
                            noteLayout.setOrientation(LinearLayout.VERTICAL);
                            noteLayout.setBackgroundResource(R.drawable.note_background);
                            noteLayout.setPadding(16, 16, 16, 16);
                            noteLayout.setElevation(4);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(0, 0, 0, 16);
                            noteLayout.setLayoutParams(layoutParams);

                            // Cria um TextView para exibir os detalhes da nota
                            TextView textView = new TextView(NoteActivity.this);
                            textView.setText(note.getTitle() + ": " + note.getContent());
                            textView.setTextSize(16);
                            textView.setTextColor(getResources().getColor(android.R.color.white));
                            noteLayout.addView(textView);

                            // Cria um LinearLayout horizontal para os botões
                            LinearLayout buttonLayout = new LinearLayout(NoteActivity.this);
                            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                            buttonLayout.setPadding(0, 16, 0, 0);

                            // Cria um botão de editar
                            Button editButton = new Button(NoteActivity.this);
                            editButton.setText("Edit");
                            editButton.setTextColor(Color.WHITE);
                            editButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.button_color)));
                            editButton.setOnClickListener(view -> {
                                Intent intent = new Intent(NoteActivity.this, UpdateNoteActivity.class);
                                intent.putExtra("note_id", note.getId()); // Passa o ID da nota para a atividade de atualização
                                startActivityForResult(intent, UPDATE_NOTE_REQUEST_CODE);
                            });
                            buttonLayout.addView(editButton);

                            // Espaçamento entre os botões
                            Space space = new Space(NoteActivity.this);
                            LinearLayout.LayoutParams spaceLayoutParams = new LinearLayout.LayoutParams(16, 0);
                            buttonLayout.addView(space, spaceLayoutParams);

                            // Cria um botão de remover
                            Button removeButton = new Button(NoteActivity.this);
                            removeButton.setText("Remove");
                            removeButton.setTextColor(Color.WHITE);
                            removeButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.button_color)));
                            removeButton.setOnClickListener(view -> {
                                // Exibir uma popup de confirmação
                                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                                builder.setMessage("Deseja realmente remover esta nota?")
                                        .setPositiveButton("Sim", (dialogInterface, i) -> {
                                            // Chamamos o método para remover a nota
                                            removeNote(note.getId());
                                        })
                                        .setNegativeButton("Não", null) // Nada acontece se clicar em "Não"
                                        .show();
                            });
                            buttonLayout.addView(removeButton);

                            // Adiciona o layout dos botões ao layout da nota
                            noteLayout.addView(buttonLayout);

                            // Adiciona o layout da nota à lista de notas
                            noteListLayout.addView(noteLayout);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(NoteActivity.this, "Failed to load notes: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
