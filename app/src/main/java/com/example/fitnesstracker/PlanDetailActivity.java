package com.example.fitnesstracker;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);

        TextView tvPlanName = findViewById(R.id.tvDetailPlanName);
        LinearLayout exerciseContainer = findViewById(R.id.exerciseContainer);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnDelete = findViewById(R.id.btnDeletePlan);
        Button btnEdit = findViewById(R.id.btnEditPlan);

        String planName = getIntent().getStringExtra("PLAN_NAME");
        String planId = getIntent().getStringExtra("PLAN_ID");

        tvPlanName.setText(planName);

        if (planId != null) {
            FirebaseFirestore.getInstance()
                    .collection("trainingsplaene")
                    .document(planId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> uebungen = (List<String>) documentSnapshot.get("uebungen");

                            if (uebungen != null) {
                                exerciseContainer.removeAllViews();
                                for (String uebung : uebungen) {
                                    // 1. Container für die Übungsgruppe
                                    LinearLayout row = new LinearLayout(this);
                                    row.setOrientation(LinearLayout.VERTICAL);
                                    row.setPadding(0, 20, 0, 40);

                                    // 2. Name der Übung
                                    TextView tvUebung = new TextView(this);
                                    tvUebung.setText(uebung);
                                    tvUebung.setTextSize(22);
                                    tvUebung.setTypeface(null, Typeface.BOLD);
                                    row.addView(tvUebung);

                                    // 3. Container für die Historie (wird unter loadExerciseHistory befüllt)
                                    LinearLayout historyLayout = new LinearLayout(this);
                                    historyLayout.setOrientation(LinearLayout.VERTICAL);
                                    historyLayout.setPadding(10, 5, 0, 10);

                                    // Initiales Laden der letzten Sätze
                                    loadExerciseHistory(planId, uebung, historyLayout);

                                    // 4. Eingabe-Zeile (Horizontal)
                                    LinearLayout inputLayout = new LinearLayout(this);
                                    inputLayout.setOrientation(LinearLayout.HORIZONTAL);
                                    inputLayout.setGravity(Gravity.CENTER_VERTICAL);

                                    EditText etWeight = new EditText(this);
                                    etWeight.setHint("kg");
                                    etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                    etWeight.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                    EditText etReps = new EditText(this);
                                    etReps.setHint("Wdh");
                                    etReps.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    etReps.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                                    Button btnAddSet = new Button(this);
                                    btnAddSet.setText("Satz speichern");

                                    // Klick-Logik zum Speichern und UI-Update
                                    btnAddSet.setOnClickListener(v -> {
                                        String weight = etWeight.getText().toString();
                                        String reps = etReps.getText().toString();

                                        if (!weight.isEmpty() && !reps.isEmpty()) {
                                            saveTrainingSet(planId, uebung, weight, reps, historyLayout);
                                            etWeight.setText("");
                                            etReps.setText("");
                                        } else {
                                            Toast.makeText(this, "Bitte Daten eingeben", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    inputLayout.addView(etWeight);
                                    inputLayout.addView(etReps);
                                    inputLayout.addView(btnAddSet);

                                    // Alles zur Hauptansicht hinzufügen
                                    row.addView(historyLayout);
                                    row.addView(inputLayout);
                                    exerciseContainer.addView(row);
                                }
                            }
                        }
                    });
        }

        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlanEditorActivity.class);
            intent.putExtra("PLAN_ID", planId);
            intent.putExtra("IS_EDIT_MODE", true);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            if (planId != null) {
                FirebaseFirestore.getInstance().collection("trainingsplaene").document(planId).delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Plan gelöscht", Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }
        });
    }

    private void saveTrainingSet(String planId, String uebung, String weight, String reps, LinearLayout historyLayout) {
        Map<String, Object> log = new HashMap<>();
        log.put("planId", planId);
        log.put("userId", FirebaseAuth.getInstance().getUid());
        log.put("uebung", uebung);
        log.put("gewicht", weight);
        log.put("wiederholungen", reps);
        log.put("timestamp", Timestamp.now());

        FirebaseFirestore.getInstance().collection("training_logs").add(log)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Satz gespeichert!", Toast.LENGTH_SHORT).show();
                    // UI sofort aktualisieren
                    loadExerciseHistory(planId, uebung, historyLayout);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadExerciseHistory(String planId, String uebung, LinearLayout historyLayout) {
        FirebaseFirestore.getInstance().collection("training_logs")
                .whereEqualTo("planId", planId)
                .whereEqualTo("uebung", uebung)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyLayout.removeAllViews();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        TextView tvLog = new TextView(this);
                        tvLog.setText("Letzter Satz: " + doc.getString("gewicht") + "kg x " + doc.getString("wiederholungen"));
                        tvLog.setTextSize(14);
                        tvLog.setAlpha(0.6f);
                        historyLayout.addView(tvLog);
                    }
                });
    }
}