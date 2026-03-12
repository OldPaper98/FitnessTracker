package com.example.fitnesstracker;

import android.os.Bundle; // Klasse: Speichert den Zustand der App (z.B. bei Drehung des Bildschirms)
import android.text.TextUtils; // Ein praktisches Tool, um Texte schnell zu prüfen (z.B. "Ist das Feld leer?")
import android.view.View; // Die Basisklasse für alles, was man sieht (Buttons, Texte, etc.)
import android.widget.Button; // Speziell für klickbare Buttons
import android.widget.EditText; // Speziell für Texteingabefelder
import android.widget.Toast; // Für die kleinen Pop-up-Meldungen am unteren Rand

import androidx.annotation.NonNull; // Ein Hinweis für das System: "Dieser Wert darf niemals 'null' sein"
import androidx.appcompat.app.AppCompatActivity; // Das Grundgerüst, damit die App auf modernen Handys läuft

import com.google.android.gms.tasks.OnCompleteListener; // Wartet darauf, dass Firebase eine Aufgabe fertigstellt
import com.google.android.gms.tasks.Task; // Repräsentiert die Aufgabe selbst (z.B. "Logge den User ein")
import com.google.firebase.auth.AuthResult; // Enthält das Ergebnis des Logins (Erfolg oder Fehler)
import com.google.firebase.auth.FirebaseAuth; // Das Haupt-Tool für die Benutzerverwaltung
import com.google.firebase.auth.FirebaseUser; // Repräsentiert den aktuell eingeloggten Nutzer
import android.content.Intent; // Der "Fahrschein", um von einem Screen zum nächsten zu wechseln

public class  MainActivity extends AppCompatActivity {

    // 1. Variablen erstellen (Die Fernbedienung für unsere XML-Elemente)
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private FirebaseAuth mAuth; // Firebase-Türsteher

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //aktueller Stand, AppCombatActivity
        setContentView(R.layout.activity_main); //verbindet Java-Datei mit XML-Datei daher besonders wichtig

        // 2. Firebase starten
        mAuth = FirebaseAuth.getInstance(); //Einrichten von Kommunikation zu Firebase

        // 3. Verbindung herstellen (Java sucht die IDs aus der XML) - greift auf R zu.
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

                // Einloggen
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