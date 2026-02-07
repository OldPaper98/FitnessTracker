package com.example.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PlanDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);

        // 1. Elemente aus der XML finden
        TextView tvTitle = findViewById(R.id.tvSelectedPlanName);
        Button btnExercise1 = findViewById(R.id.btnExercise1); // Bankdrücken
        Button btnExercise2 = findViewById(R.id.btnExercise2); // Butterfly
        Button btnFinishWorkout = findViewById(R.id.btnFinishWorkout);
        Button btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Geht zurück zum Dashboard (S2)
            }
        });

        // 2. Den Namen aus dem Intent empfangen (von S2)
        String planName = getIntent().getStringExtra("PLAN_NAME");
        if (planName != null) {
            tvTitle.setText(planName);
        }

        // 3. Klick auf eine Übung -> Navigiert zur Detail-Eingabe (S5)
        btnExercise1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlanDetailActivity.this, UebungDetailActivity.class);
                intent.putExtra("EXERCISE_NAME", "Bankdrücken");
                startActivity(intent);
            }
        });

        // 4. Training beenden & speichern -> Zurück zum Dashboard (S2)
        btnFinishWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In deinem Diagramm: Vorher würden hier die Daten in Firestore gespeichert
                finish(); // Schließt S4 und geht zurück zu S2
            }
        });

        // Aktiviert den "Zurück"-Pfeil in der oberen Leiste (ActionBar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Beendet die Seite und geht zurück
        return true;
    }
}