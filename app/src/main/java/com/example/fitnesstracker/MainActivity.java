package com.example.fitnesstracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;

public class  MainActivity extends AppCompatActivity {

    // 1. Variablen erstellen (Die Fernbedienung für unsere XML-Elemente)
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth; // Das ist unser Firebase-Türsteher

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //verbindet Java-Datei mit XML-Datei daher besonders wichtig

        // 2. Firebase starten
        mAuth = FirebaseAuth.getInstance();

        // 3. Verbindung herstellen (Java sucht die IDs aus der XML)
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // 4. Was passiert beim Klick auf "REGISTRIEREN"?
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Bitte alles ausfüllen!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Hier passiert die Magie: Account erstellen
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Account erstellt!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, TrainingsplanUebersichtActivity.class);
                                    startActivity(intent);
                                    finish(); // Beendet die Login-Seite, damit man nicht mit "Zurück" dahin kommt
                                } else {
                                    Toast.makeText(MainActivity.this, "Fehler: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // 5. Was passiert beim Klick auf "LOGIN"?
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Bitte alles ausfüllen!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Hier passiert die Magie: Einloggen
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, TrainingsplanUebersichtActivity.class);
                                    startActivity(intent);
                                    finish(); // Beendet die Login-Seite, damit man nicht mit "Zurück" dahin kommt
                                } else {
                                    Toast.makeText(MainActivity.this, "Login fehlgeschlagen!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}