package com.example.fitnesstracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanEditorActivity extends AppCompatActivity {

    private EditText etPlanName;
    private CheckBox cbBenchPress, cbSquats, cbDeadlift, cbPullUps;
    private Button btnSavePlan;

    // Neue Variablen für den Bearbeitungs-Modus
    private String planId;
    private boolean isEditMode = false;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_editor);

        db = FirebaseFirestore.getInstance();

        // UI Elemente
        etPlanName = findViewById(R.id.etPlanName);
        cbBenchPress = findViewById(R.id.cbBenchPress);
        cbSquats = findViewById(R.id.cbSquats);
        cbDeadlift = findViewById(R.id.cbDeadlift);
        cbPullUps = findViewById(R.id.cbPullUps);
        btnSavePlan = findViewById(R.id.btnSavePlan);
        Button btnBack = findViewById(R.id.btnBack);

        // Prüfen, ob wir im Bearbeitungs-Modus sind
        planId = getIntent().getStringExtra("PLAN_ID");
        isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);

        if (isEditMode && planId != null) {
            loadExistingPlanData();
            btnSavePlan.setText("Änderungen speichern");
        }

        btnBack.setOnClickListener(v -> finish());

        btnSavePlan.setOnClickListener(v -> savePlan());
    }

    private void loadExistingPlanData() {
        db.collection("trainingsplaene").document(planId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etPlanName.setText(doc.getString("planName"));
                        List<String> selectedUebungen = (List<String>) doc.get("uebungen");

                        if (selectedUebungen != null) {
                            if (selectedUebungen.contains("Bankdrücken")) cbBenchPress.setChecked(true);
                            if (selectedUebungen.contains("Kniebeugen")) cbSquats.setChecked(true);
                            if (selectedUebungen.contains("Kreuzheben")) cbDeadlift.setChecked(true);
                            if (selectedUebungen.contains("Klimmzüge")) cbPullUps.setChecked(true);
                        }
                    }
                });
    }

    private void savePlan() {
        String planName = etPlanName.getText().toString().trim();
        List<String> selectedExercises = new ArrayList<>();

        if (planName.isEmpty()) {
            Toast.makeText(this, "Bitte gib dem Plan einen Namen!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cbBenchPress.isChecked()) selectedExercises.add("Bankdrücken");
        if (cbSquats.isChecked()) selectedExercises.add("Kniebeugen");
        if (cbDeadlift.isChecked()) selectedExercises.add("Kreuzheben");
        if (cbPullUps.isChecked()) selectedExercises.add("Klimmzüge");

        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "Wähle mindestens eine Übung aus!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> planData = new HashMap<>();
        planData.put("planName", planName);
        planData.put("uebungen", selectedExercises);
        planData.put("userId", FirebaseAuth.getInstance().getUid());
        planData.put("timestamp", Timestamp.now());

        if (isEditMode) {
            // ÜBUNG ENTFERNEN LOGIK: Da wir hier die Liste komplett neu senden,
            // werden abgewählte Checkboxen einfach nicht mehr mitgespeichert!
            db.collection("trainingsplaene").document(planId)
                    .set(planData) // Überschreibt das alte Dokument mit den neuen Daten
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Plan aktualisiert!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            db.collection("trainingsplaene")
                    .add(planData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Plan erstellt!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}