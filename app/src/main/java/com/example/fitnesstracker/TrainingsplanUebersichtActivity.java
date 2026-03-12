package com.example.fitnesstracker;

// Android Basis-Pakete
import android.content.Intent; // Das „Reiseticket“: Ermöglicht den Wechsel zwischen Activities (z.B. zur Detailansicht).
import android.os.Bundle; // Ein Container für Zustandsdaten, der beim Erstellen der Activity (onCreate) übergeben wird.
import android.view.View; // Die Basisklasse für alle Oberflächenelemente; wird hier für Klick-Events benötigt.

// UI (User Interface) Elemente
import android.widget.Button; // Speziell für die klickbaren Schaltflächen in deiner Übersicht.
import android.widget.LinearLayout; // Der Container im XML, in den wir die Pläne dynamisch als Liste untereinander einfügen.
import android.widget.Toast;

// AndroidX Kompatibilität
import androidx.appcompat.app.AppCompatActivity; // Die Basisklasse für deine Activity, die modernes Design auf alten Android-Versionen ermöglicht.

// Firebase Pakete (Die Verbindung zur Cloud)
import com.google.firebase.auth.FirebaseAuth; // Ermöglicht den Zugriff auf den aktuell eingeloggten Nutzer (z.B. für die userId).
import com.google.firebase.firestore.FirebaseFirestore;        // Das Hauptwerkzeug für den Zugriff auf deine NoSQL-Datenbank (Firestore).
import com.google.firebase.firestore.QueryDocumentSnapshot;     // Repräsentiert ein einzelnes „Dokument“ (einen Plan), das wir aus der Datenbank laden.

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
        // 1. Zugriff auf den Container im XML, in dem die Buttons erscheinen sollen
        LinearLayout planContainer = findViewById(R.id.planContainer);

        // 2. Datenbank-Instanz und die eindeutige ID des aktuellen Nutzers abrufen
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getUid();

        // Sicherheits-Check: Wenn kein User eingeloggt ist, brechen wir ab
        if (currentUserId == null) return;

        // 3. Datenbank-Abfrage: Suche in "trainingsplaene" nur nach Plänen dieses Nutzers
        db.collection("trainingsplaene")
                .whereEqualTo("userId", currentUserId) // Filter für Datenschutz und Personalisierung
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 4. UI aufräumen: Alte Buttons entfernen, bevor wir neue zeichnen (verhindert Dopplungen)
                    planContainer.removeAllViews(); // Erstmal aufräumen

                    // 5. Schleife: Gehe jedes gefundene Dokument (Plan) einzeln durch um für jeden Plan einen entsprechenden Button zu erstellen
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String planName = document.getString("planName");

                        // Dynamisch einen Button für den Plan erstellen
                        Button btn = new Button(this);
                        btn.setText(planName);

                        // Layout-Regeln für den Button (Breite: fülle Display, Höhe: so viel wie nötig)
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 0, 0, 15); // Kleiner Abstand nach unten zum nächsten Button
                        btn.setLayoutParams(params);

                        // Klick-Logik: Öffnet die Detailansicht (S4)
                        btn.setOnClickListener(v -> {
                            Intent intent = new Intent(TrainingsplanUebersichtActivity.this, PlanDetailActivity.class);
                            // Wir geben den Namen und die ID als "Gepäck" (Extra) an die nächste Seite weiter
                            intent.putExtra("PLAN_NAME", planName);
                            intent.putExtra("PLAN_ID", document.getId());
                            startActivity(intent);
                        });

                        // 8. Den fertig konfigurierten Button in die Ansicht einfügen
                        planContainer.addView(btn);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fehler beim Laden: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}