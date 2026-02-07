package com.example.fitnesstracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Firebase Importe (Sorgen für die Cloud-Anbindung A5 & A6)
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

// Java Hilfsmittel (Für Listen und Datenstrukturen)
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanEditorActivity extends AppCompatActivity {

    // UI Elemente definieren
    private EditText etPlanName;
    private CheckBox cbBenchPress, cbSquats, cbDeadlift, cbPullUps;
    private Button btnSavePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_editor); // Verbindung zum Screen S3 [cite: 7, 13]

        // Elemente initialisieren
        etPlanName = findViewById(R.id.etPlanName);
        cbBenchPress = findViewById(R.id.cbBenchPress);
        cbSquats = findViewById(R.id.cbSquats);
        cbDeadlift = findViewById(R.id.cbDeadlift);
        cbPullUps = findViewById(R.id.cbPullUps);
        btnSavePlan = findViewById(R.id.btnSavePlan);

        // Zurück-Button (Navigation S3 -> S2) [cite: 32]
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Beendet Seite und geht zurück zum Dashboard [cite: 32]
            }
        });

        // Speichern-Button
        btnSavePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlan();
            }
        });
    }

    private void savePlan() {
        // 1. Daten auslesen
        String planName = etPlanName.getText().toString().trim();
        List<String> selectedExercises = new ArrayList<>();

        // Validierung
        if (planName.isEmpty()) {
            Toast.makeText(this, "Bitte gib dem Plan einen Namen!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Übungen aus statischer Liste sammeln (Kernfunktionalität 2 / A2)
        if (cbBenchPress.isChecked()) selectedExercises.add("Bankdrücken");
        if (cbSquats.isChecked()) selectedExercises.add("Kniebeugen");
        if (cbDeadlift.isChecked()) selectedExercises.add("Kreuzheben");
        if (cbPullUps.isChecked()) selectedExercises.add("Klimmzüge");

        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "Wähle mindestens eine Übung aus!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Daten für Firestore vorbereiten (A5 & A6) [cite: 6, 24]
        Map<String, Object> planData = new HashMap<>();
        planData.put("planName", planName);
        planData.put("uebungen", selectedExercises);
        planData.put("userId", FirebaseAuth.getInstance().getUid()); // Login-Schutz A6
        planData.put("timestamp", Timestamp.now());

        // 4. In Cloud speichern (A5: Persistente Datenbank) [cite: 6, 24]
        FirebaseFirestore.getInstance()
                .collection("trainingsplaene")
                .add(planData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PlanEditorActivity.this, "Plan '" + planName + "' gespeichert!", Toast.LENGTH_SHORT).show();
                    finish(); // Zurück-Navigation nach Erfolg [cite: 32]
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PlanEditorActivity.this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}