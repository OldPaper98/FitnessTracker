package com.example.fitnesstracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PlanDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_detail);

        TextView tvPlanName = findViewById(R.id.tvDetailPlanName);
        LinearLayout exerciseContainer = findViewById(R.id.exerciseContainer);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnDelete = findViewById(R.id.btnDeletePlan);

        // 1. Daten aus dem Intent holen (vom Dashboard S2 übergeben)
        String planName = getIntent().getStringExtra("PLAN_NAME");
        String planId = getIntent().getStringExtra("PLAN_ID");

        tvPlanName.setText(planName);

        // 2. Details aus Firestore laden (Anforderung A5)
        if (planId != null) {
            FirebaseFirestore.getInstance()
                    .collection("trainingsplaene")
                    .document(planId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Übungen als Liste aus dem Dokument ziehen
                            List<String> uebungen = (List<String>) documentSnapshot.get("uebungen");

                            if (uebungen != null) {
                                for (String uebung : uebungen) {
                                    TextView tvUebung = new TextView(this);
                                    tvUebung.setText("• " + uebung);
                                    // Korrektur: Nur die Zahl 20, ohne 'sp'
                                    tvUebung.setTextSize(20);
                                    tvUebung.setPadding(0, 10, 0, 10);
                                    exerciseContainer.addView(tvUebung);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        // Zurück-Button Logik
        btnBack.setOnClickListener(v -> finish());

        btnDelete.setOnClickListener(v -> {
            if (planId != null) {
                // Den Plan direkt aus Firestore löschen (Anforderung A5)
                FirebaseFirestore.getInstance()
                        .collection("trainingsplaene")
                        .document(planId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Plan erfolgreich gelöscht", Toast.LENGTH_SHORT).show();
                            finish(); // Zurück zum Dashboard
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Fehler beim Löschen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}