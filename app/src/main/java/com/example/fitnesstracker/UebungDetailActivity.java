package com.example.fitnesstracker;

import android.os.Bundle; // Zum Speichern des Zustands beim Erstellen
import android.view.View; // Basisklasse für alle Oberflächenelemente
import android.widget.Button; // Für die klickbaren Knöpfe
import android.widget.EditText; // Für die Texteingabefelder (Gewicht/Wdh)
import android.widget.TextView; // Zum Anzeigen von Text (Übungsname)
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity; // Die Basisklasse für moderne Android-Activities

public class UebungDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Verbindung zum Layout herstellen
        setContentView(R.layout.activity_uebung_detail);

        // 2. Elemente aus der XML finden
        TextView tvExerciseTitle = findViewById(R.id.tvExerciseTitle);
        EditText etWeight = findViewById(R.id.etWeight);
        EditText etReps = findViewById(R.id.etReps);
        Button btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Geht zurück zur Plan-Übersicht (S4)
            }
        });

        // 3. Den Übungsnamen aus dem Intent-Umschlag holen
        String exerciseName = getIntent().getStringExtra("EXERCISE_NAME");

        // 4. Den Namen oben anzeigen
        if (exerciseName != null) {
            tvExerciseTitle.setText(exerciseName);
        }

        // 5. Speicher-Button Logik
        btnSaveWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weight = etWeight.getText().toString();
                String reps = etReps.getText().toString();

                if (weight.isEmpty() || reps.isEmpty()) {
                    Toast.makeText(UebungDetailActivity.this, "Bitte Gewicht und Wiederholungen eintragen!", Toast.LENGTH_SHORT).show();
                } else {
                    // Feedback an den User
                    Toast.makeText(UebungDetailActivity.this, "Gespeichert: " + weight + "kg x " + reps, Toast.LENGTH_LONG).show();

                    // Zurück zur Übersicht gehen
                    finish();
                }
            }
        });
        // Aktiviert den "Zurück"-Pfeil in der oberen Leiste (ActionBar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Diese Methode wird aufgerufen, wenn der Nutzer auf den Pfeil oben links klickt.
     * Sie sorgt dafür, dass die Activity sauber geschlossen wird.
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Beendet die Seite und geht zurück
        return true;
    }
}