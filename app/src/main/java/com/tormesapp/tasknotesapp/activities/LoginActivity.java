package com.tormesapp.tasknotesapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tormesapp.tasknotesapp.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextSenha;
    private TextView txtErro;
    private static final String TAG = "LoginActivity";

    private static final String PREF_NAME = "user_pref";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "KEY_EMAIL";
    private static final String KEY_SENHA = "KEY_SENHA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextSenha = findViewById(R.id.editTextPassword);
        txtErro = findViewById(R.id.txtErro); // Inicializa o TextView

        // Recuperar usuário e senha salvos, se disponíveis
        String savedEmail = getSavedEmail();
        String savedSenha = getSavedSenha();
        if (!savedEmail.isEmpty() && !savedSenha.isEmpty()) {
            editTextEmail.setText(savedEmail);
            editTextSenha.setText(savedSenha);
        }


        Button btnLogin = findViewById(R.id.btnVoltar);
        btnLogin.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            String senha = editTextSenha.getText().toString();

            Log.d(TAG, "Attempting to login with email: " + email);
            login(email, senha);
        });

        Button btnUsuarios = findViewById(R.id.btnCadastrarUser);
        btnUsuarios.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, UsuariosActivity.class);
            startActivity(intent);
        });
    }

    private String getSavedEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    private String getSavedSenha() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SENHA, "");
    }

    private void saveCredentials(String email, String senha) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_SENHA, senha);
        editor.apply();
    }

    private void login(String email, String senha) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("email", email)
                .add("senha", senha);
        Request request = new Request.Builder()
                .url("https://ethernalpet.com/TaskNotesAppBackend/usuarios/readFindByEmailAndSenha.php") // Substitua pela URL correta
                .post(formBuilder.build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Response received: " + responseData);

                    // Analisar a resposta JSON
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        if (jsonResponse.has("message") && jsonResponse.getString("message").equals("Usuário não encontrado")) {
                            showError("Usuário não encontrado");
                        } else {
                            int userId = jsonResponse.getInt("id");
                            saveUserId(userId);
                            saveCredentials(email, senha);
                            runOnUiThread(() -> {
                                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                startActivity(intent);
                            });
                        }
                    } catch (JSONException e) {
                        showError("Erro ao processar resposta do servidor");
                        Log.e(TAG, "JSON exception", e);
                    }
                } else {
                    showError("Erro ao conectar ao servidor: " + response.message());
                    Log.e(TAG, "Response was not successful: " + response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                showError("Erro ao conectar ao servidor");
                Log.e(TAG, "Network call failed", e);
            }
        });
    }
    // Método para salvar o ID do usuário nas preferências compartilhadas
    private void saveUserId(int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    // Método para recuperar o ID do usuário das preferências compartilhadas
    public static int getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, -1); // Retorna -1 se o ID do usuário não estiver disponível
    }

    private void showError(final String message) {
        runOnUiThread(() -> {
            txtErro.setText(message);
            txtErro.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing error message: " + message); // Log para verificar se o método showError está sendo chamado
        });
    }
}
