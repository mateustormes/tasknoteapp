package com.tormesapp.tasknotesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.tormesapp.tasknotesapp.R;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class UsuariosActivity extends AppCompatActivity {

    private OkHttpClient client;
    private EditText editTextNome, editTextEmail, editTextSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuarios);

        client = new OkHttpClient();

        editTextNome = findViewById(R.id.editTextSenha);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextPassword);

        Button btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(view -> {
            Intent intent = new Intent(UsuariosActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        Button btnCadastrarUser = findViewById(R.id.btnCadastrarUser);
        btnCadastrarUser.setOnClickListener(view -> {
            cadastrarUsuario();
        });
    }

    private void cadastrarUsuario() {
        String nome = editTextNome.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String senha = editTextSenha.getText().toString().trim();

        // Verifica se todos os campos foram preenchidos
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construa o corpo da solicitação com os parâmetros do usuário
        RequestBody requestBody = new FormBody.Builder()
                .add("nome", nome)
                .add("email", email)
                .add("senha", senha)
                .build();

        // Construa a solicitação POST com o corpo da solicitação
        Request request = new Request.Builder()
                .url("https://ethernalpet.com/TaskNotesAppBackend/usuarios/create.php") // Endereço do endpoint de criação de usuário
                .post(requestBody)
                .build();

        // Faça a chamada assíncrona para o endpoint
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UsuariosActivity.this, "Erro ao cadastrar usuário", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(UsuariosActivity.this, "Usuário cadastrado com sucesso", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(UsuariosActivity.this, "Erro ao cadastrar usuário: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
