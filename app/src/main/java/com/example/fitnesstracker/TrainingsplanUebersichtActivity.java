package com.example.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout; // WICHTIG: Neu hinzugefügt
import android.widget.Toast;        // WICHTIG: Neu hinzugefügt
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;        // WICHTIG: Neu hinzugefügt
import com.google.firebase.firestore.QueryDocumentSnapshot;     // WICHTIG: Neu hinzugefügt

public class TrainingsplanUebersichtActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainingsplan_uebersicht);

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnCreatePlan = findViewById(R.id.btnCreatePlan);

        // HINWEIS: btnPlanExample wurde hier entfernt, da er in der XML
        // nicht mehr existiert. Wir laden die Pläne jetzt dynamisch!

        // Logout Logik (S1)
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
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
                Intent intent = new Intent(TrainingsplanUebersichtActivity.this, PlanEditorActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Jedes Mal, wenn man zum Dashboard zurückkehrt, laden wir die Liste neu
        loadUserPlans();
    }

    private void loadUserPlans() {
        LinearLayout planContainer = findViewById(R.id.planContainer);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId == null) return;

        // Pläne aus Firestore laden (Anforderung A5 & A6)
        db.collection("trainingsplaene")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    planContainer.removeAllViews(); // Erstmal aufräumen

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String planName = document.getString("planName");

                        // Dynamisch einen Button für den Plan erstellen
                        Button btn = new Button(this);
                        btn.setText(planName);

                        // Layout-Einstellungen für den Button
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 0, 0, 15);
                        btn.setLayoutParams(params);

                        // Klick-Logik: Öffnet die Detailansicht (S4)
                        btn.setOnClickListener(v -> {
                            Intent intent = new Intent(TrainingsplanUebersichtActivity.this, PlanDetailActivity.class);
                            intent.putExtra("PLAN_NAME", planName);
                            intent.putExtra("PLAN_ID", document.getId());
                            startActivity(intent);
                        });

                        planContainer.addView(btn);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fehler beim Laden: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}