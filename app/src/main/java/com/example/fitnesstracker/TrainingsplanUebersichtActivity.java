package com.example.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class TrainingsplanUebersichtActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainingsplan_uebersicht);

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnCreatePlan = findViewById(R.id.btnCreatePlan);
        Button btnPlanExample = findViewById(R.id.btnPlanExample);

        // Logout Logik (S1)
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // Firebase Abmeldung
                Intent intent = new Intent(TrainingsplanUebersichtActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Neuen Plan erstellen (S3)
        btnCreatePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Jetzt navigieren wir zum Editor (S3)
                Intent intent = new Intent(TrainingsplanUebersichtActivity.this, PlanEditorActivity.class);
                startActivity(intent);
            }
        });

        // Klick auf einen existierenden Plan (S4 / Workout-Detail)
        btnPlanExample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigiert zum neuen Screen "Workout-Detail"
                Intent intent = new Intent(TrainingsplanUebersichtActivity.this, PlanDetailActivity.class);
                intent.putExtra("PLAN_NAME", "Brust/Bizeps");
                startActivity(intent);
            }
        });
    }
}