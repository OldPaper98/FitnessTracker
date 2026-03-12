package com.example.fitnesstracker;

// --- Android Basis & Lifecycle ---
import android.content.Intent; // Ermöglicht Navigation und Datenempfang (z.B. Plan-ID vom Dashboard).
import android.os.Bundle; // Speichert den Zustand der Activity beim Start oder bei Systemereignissen.
import androidx.appcompat.app.AppCompatActivity; // Basisklasse für moderne Android-Funktionen und Design-Kompatibilität.

// --- UI-Design & Styling (Besonders wichtig für dynamische Layouts) ---
import android.graphics.Typeface; // Ermöglicht das Ändern der Schriftart (z.B. Texte auf „Fett“ setzen).

import android.text.InputType; // Legt fest, welche Tastatur erscheint (z.B. nur Zahlen für Gewicht/Wiederholungen)
import android.view.Gravity; // Steuert die Ausrichtung von Elementen (z.B. Zentrieren von Text in dynamischen Zeilen).
import android.view.View; // Basisklasse für alle UI-Komponenten; wird für Sichtbarkeit und Klicks benötigt.

// --- UI-Komponenten (Widgets) ---
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout; // Der wichtigste Container, um Übungen untereinander/nebeneinander anzuordnen.
import android.widget.TextView; // Textfelder für den Timer, Plannamen und die Übungsbezeichnungen.
import android.widget.Toast;

// --- Hintergrund-Prozesse (Timer) ---
import android.os.Handler; // Das „Zeitmanagement“: Erlaubt es, Code zeitversetzt oder wiederholend auszuführen (Timer).

// --- Firebase & Cloud-Datenbank ---
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// --- Java Utilities (Datenstrukturen & Formatierung) ---
import java.util.HashMap; // Konkreter Speicher für Key-Value-Paare, um Daten an Firestore zu senden.
import java.util.List; // Ermöglicht den Umgang mit Listen (z.B. der Übungsliste eines Plans).
import java.util.Locale; // Stellt sicher, dass Zeitformate (00:00:00) überall gleich angezeigt werden.
import java.util.Map; // Interface für Key-Value-Strukturen.

public class PlanDetailActivity extends AppCompatActivity {

    // Timer Variablen
    private long startTime = 0L; // Startzeitpunkt in Millisekunden
    private boolean isTrainingActive = false;
    private Handler timerHandler = new Handler(); // Ein „Bote“, der Aufgaben zeitversetzt ausführt
    private TextView tvTimer;
    private Button btnStart, btnStop, btnCancel;

    // Das Runnable aktualisiert die Zeit jede Sekunde
    // Das Runnable ist die „Anweisung“, was der Bote tun soll
    private Runnable timerRunnable = new Runnable() { //Runnable Interface (eine Schnittstelle) aus dem Standard-Java-Paket java.lang
        //Runnable stellt lediglich einen "Befehl" oder eine "Aufgabe" dar, die irgendwann ausgeführt werden soll
        @Override
        public void run() {
            // Differenz berechnen: Jetzt minus Startzeit
            long millis = System.currentTimeMillis() - startTime; //Ergebnis: 65.500.
            int seconds = (int) (millis / 1000); //Wir wandeln Millisekunden in ganze Sekunden um. Ergebnis: 65 (Die Nachkommastellen fallen weg).
            int minutes = seconds / 60; // Wie viele volle Minuten passen in 65 Sekunden? Ergebnis: 1.
            int hours = minutes / 60; //Wie viele volle Stunden passen in 1 Minute? Ergebnis: 0.
            seconds = seconds % 60; //Der Modulo-Operator gibt den Rest einer Division an. 65 geteilt durch 60 ist 1, Rest 5. Damit wird aus 65 Sekunden die Anzeige "05".
            minutes = minutes % 60; //Falls wir 70 Minuten hätten, wäre 70 % 60 = 10. Die Anzeige wäre also "01:10:xx".

            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)); //%02d: Das ist ein Platzhalter. So wird aus 5 Sekunden die schöne Anzeige 05
            timerHandler.postDelayed(this, 1000);
            //
        }
    };
    // --- TIMER LOGIK ---

    /**
     * Dieses Runnable fungiert als asynchrone Arbeitseinheit für den Timer.
     * Es nutzt das Prinzip der Rekursion über den Handler, um eine tickende Uhr zu simulieren.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Ruft die Basis-Logik der Mutterklasse auf
        setContentView(R.layout.activity_plan_detail);

        // UI Elemente finden
        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStartTraining);
        btnStop = findViewById(R.id.btnStopTraining);
        btnCancel = findViewById(R.id.btnCancelTraining);

        TextView tvPlanName = findViewById(R.id.tvDetailPlanName);
        LinearLayout exerciseContainer = findViewById(R.id.exerciseContainer);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnDelete = findViewById(R.id.btnDeletePlan);
        Button btnEdit = findViewById(R.id.btnEditPlan);

        String planName = getIntent().getStringExtra("PLAN_NAME");
        String planId = getIntent().getStringExtra("PLAN_ID");

        tvPlanName.setText(planName);

        // Timer Button Klick-Events
        btnStart.setOnClickListener(v -> startTraining());
        btnCancel.setOnClickListener(v -> cancelTraining());
        btnStop.setOnClickListener(v -> stopTraining(planId, planName));

        // --- 4. Dynamisches Laden der Übungen aus Firestore ---
        if (planId != null) {
            FirebaseFirestore.getInstance()
                    .collection("trainingsplaene")
                    .document(planId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> uebungen = (List<String>) documentSnapshot.get("uebungen");

                            if (uebungen != null) {
                                exerciseContainer.removeAllViews(); // Löscht Platzhalter aus dem Container
                                for (String uebung : uebungen) {
                                    LinearLayout row = new LinearLayout(this); // Erstellt einen neuen Container-Block
                                    row.setOrientation(LinearLayout.VERTICAL); // Stapelt Elemente innerhalb der Reihe übereinander
                                    row.setPadding(0, 20, 0, 40); // Setzt Abstände für eine saubere Optik

                                    TextView tvUebung = new TextView(this); // Erstellt das Textfeld für den Übungsnamen
                                    tvUebung.setText(uebung); // Setzt den Namen der aktuellen Übung
                                    tvUebung.setTextSize(22); // Macht die Schrift groß (22sp)
                                    tvUebung.setTypeface(null, Typeface.BOLD); // Macht die Schrift fett
                                    row.addView(tvUebung); // Fügt den Namen dem Block hinzu

                                    LinearLayout historyLayout = new LinearLayout(this); // Erstellt Bereich für die Historie
                                    historyLayout.setOrientation(LinearLayout.VERTICAL); // Historie-Einträge untereinander
                                    historyLayout.setPadding(10, 5, 0, 10); // Kleiner Einzug für die Optik
                                    loadExerciseHistory(planId, uebung, historyLayout); // Lädt letzte Sätze aus der DB

                                    LinearLayout inputLayout = new LinearLayout(this); // Erstellt Zeile für die Eingabefelder
                                    inputLayout.setOrientation(LinearLayout.HORIZONTAL); // Felder nebeneinander (Gewicht | Wdh)
                                    inputLayout.setGravity(Gravity.CENTER_VERTICAL); // Zentriert die Elemente vertikal

                                    EditText etWeight = new EditText(this); // Erstellt Eingabefeld für das Gewicht
                                    etWeight.setHint("kg"); // Grauer Hinweistext im Feld
                                    etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL); // Erlaubt nur Zahlen & Komma
                                    etWeight.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Nutzt verfügbaren Platz (Gewichtung 1)

                                    EditText etReps = new EditText(this); // Erstellt Eingabefeld für Wiederholungen
                                    etReps.setHint("Wdh"); // Grauer Hinweistext
                                    etReps.setInputType(InputType.TYPE_CLASS_NUMBER); // Nur ganze Zahlen erlaubt
                                    etReps.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Nutzt verfügbaren Platz (Gewichtung 1)

                                    Button btnAddSet = new Button(this);
                                    btnAddSet.setText("Speichern");

                                    btnAddSet.setOnClickListener(v -> {
                                        // ZWEITER WEG: Auto-Start, wenn noch nicht aktiv
                                        if (!isTrainingActive) {
                                            startTraining();
                                        }

                                        String weight = etWeight.getText().toString();
                                        String reps = etReps.getText().toString();

                                        if (!weight.isEmpty() && !reps.isEmpty()) { // Prüft, ob beide Felder ausgefüllt sind
                                            saveTrainingSet(planId, uebung, weight, reps, historyLayout); // Speichert Daten in Firestore
                                            etWeight.setText(""); // Leert das Gewichtsfeld für den nächsten Satz
                                            etReps.setText(""); // Leert das Wiederholungsfeld
                                        } else {
                                            Toast.makeText(this, "Bitte Daten eingeben", Toast.LENGTH_SHORT).show(); // Fehlermeldung
                                        }
                                    });

                                    inputLayout.addView(etWeight); // Fügt Gewicht-Feld der Eingabezeile hinzu
                                    inputLayout.addView(etReps); // Fügt Wiederholungs-Feld der Eingabezeile hinzu
                                    inputLayout.addView(btnAddSet); // Fügt Button der Eingabezeile hinzu

                                    row.addView(historyLayout); // Fügt die Historie dem Hauptblock hinzu
                                    row.addView(inputLayout); // Fügt die Eingabezeile dem Hauptblock hinzu
                                    exerciseContainer.addView(row); // Fügt den kompletten Übungsblock dem Screen hinzu
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

    // --- Timer Hilfsmethoden ---

    private void startTraining() {
        isTrainingActive = true;
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        btnStart.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
    }

    private void cancelTraining() {
        timerHandler.removeCallbacks(timerRunnable);
        isTrainingActive = false;
        tvTimer.setText("00:00:00");

        btnStart.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }

    private void stopTraining(String planId, String planName) {
        timerHandler.removeCallbacks(timerRunnable);
        String finalTime = tvTimer.getText().toString();

        Map<String, Object> session = new HashMap<>();
        session.put("planId", planId);
        session.put("planName", planName);
        session.put("duration", finalTime);
        session.put("timestamp", Timestamp.now());
        session.put("userId", FirebaseAuth.getInstance().getUid());

        FirebaseFirestore.getInstance().collection("finished_sessions").add(session)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Training beendet: " + finalTime, Toast.LENGTH_LONG).show();
                    finish(); // Zurück zum Dashboard
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- Bestehende Tracking Methoden ---

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
                    loadExerciseHistory(planId, uebung, historyLayout);
                });
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