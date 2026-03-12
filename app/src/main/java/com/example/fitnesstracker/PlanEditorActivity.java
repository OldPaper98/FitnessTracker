package com.example.fitnesstracker;

import android.os.Bundle; // Der „Zustands-Koffer“: Speichert Daten, falls die Activity neu erstellt wird.
import android.view.View; // Der „Zustands-Koffer“: Speichert Daten, falls die Activity neu erstellt wird.
import androidx.appcompat.app.AppCompatActivity; // Die moderne Basisklasse, die meine Activity mit nützlichen Funktionen ausstattet.

// UI-Komponenten (Die Steuerelemente auf deinem Screen)
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

// Firebase & Cloud (Die Verbindung nach außen)
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

// Java Utility (Datenstrukturen)
import java.util.ArrayList; // Dynamische Liste für die ausgewählten Übungen
import java.util.HashMap; // Key-Value Speicher für die Firestore-Daten
import java.util.List; // Interface für Listen-Typen
import java.util.Map; // Interface für Key-Value Strukturen

public class PlanEditorActivity extends AppCompatActivity {

    // UI-Elemente: Für die Checkboxen und Textfelder
    private EditText etPlanName;
    private CheckBox cbBenchPress, cbSquats, cbDeadlift, cbPullUps;
    private Button btnSavePlan;

    // Logik-Variablen zur Unterscheidung: Neu oder Bearbeiten?
    private String planId; // Speichert die ID des Plans, falls wir bearbeiten
    private boolean isEditMode = false; // Flag (Schalter), um den Modus zu prüfen
    private FirebaseFirestore db; // Datenbank-Referenz

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_editor);

        db = FirebaseFirestore.getInstance(); // Verbindung zur NoSQL-Datenbank herstellen

        // UI Elemente
        etPlanName = findViewById(R.id.etPlanName);
        cbBenchPress = findViewById(R.id.cbBenchPress);
        cbSquats = findViewById(R.id.cbSquats);
        cbDeadlift = findViewById(R.id.cbDeadlift);
        cbPullUps = findViewById(R.id.cbPullUps);
        btnSavePlan = findViewById(R.id.btnSavePlan);
        Button btnBack = findViewById(R.id.btnBack);

        // 2. Modus-Prüfung: Hat uns die vorherige Activity Daten mitgegeben?
        planId = getIntent().getStringExtra("PLAN_ID");
        isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);

        // Wenn wir bearbeiten, müssen wir die alten Daten zuerst laden
        if (isEditMode && planId != null) {
            loadExistingPlanData();
            btnSavePlan.setText("Änderungen speichern");
        }

        btnBack.setOnClickListener(v -> finish());

        btnSavePlan.setOnClickListener(v -> savePlan());
    }

    /**
     * Lädt die bestehenden Übungen aus Firestore und setzt die Haken in den Checkboxen
     */
    private void loadExistingPlanData() {
        db.collection("trainingsplaene").document(planId).get()
                .addOnSuccessListener(doc -> { // DocumentSnapshot ist das Paket, das die Daten aus der Cloud enthält.
                    if (doc.exists()) {
                        etPlanName.setText(doc.getString("planName")); //Holt den Wert, der in der Cloud unter dem Feldnamen „planName“ gespeichert ist.
                        List<String> selectedUebungen = (List<String>) doc.get("uebungen"); //Objekt zu Liste umwandeln

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
        String planName = etPlanName.getText().toString().trim(); // Trim entfernt Leerzeichen am Anfang und Ende, toString wandelt den Text in einen String um
        List<String> selectedExercises = new ArrayList<>();

        if (planName.isEmpty()) {
            Toast.makeText(this, "Bitte gib dem Plan einen Namen!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cbBenchPress.isChecked()) selectedExercises.add("Bankdrücken"); //isChecked(): Methode der Klasse CheckBox
        if (cbSquats.isChecked()) selectedExercises.add("Kniebeugen");
        if (cbDeadlift.isChecked()) selectedExercises.add("Kreuzheben");
        if (cbPullUps.isChecked()) selectedExercises.add("Klimmzüge");

        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "Wähle mindestens eine Übung aus!", Toast.LENGTH_SHORT).show();
            return;
        }

        //Das Umwandeln von UI-Elementen (Checkboxen) in eine Datenstruktur (Map), die Firestore versteht.
        Map<String, Object> planData = new HashMap<>(); //Das ist ein Schlüssel-Wert-Speicher (Dictionary). Der „Key“ ist immer ein Text (String), der „Value“ kann alles Mögliche sein (ein Text, eine Liste, eine ID).
        planData.put("planName", planName);
        planData.put("uebungen", selectedExercises);
        planData.put("userId", FirebaseAuth.getInstance().getUid());
        planData.put("timestamp", Timestamp.now()); //Erzeugt ein Google-Spezialobjekt mit dem exakten Datum und der Uhrzeit der Speicherung.

        if (isEditMode) {
            // ÜBUNG ENTFERNEN LOGIK: Da wir hier die Liste komplett neu senden,
            // werden abgewählte Checkboxen einfach nicht mehr mitgespeichert!
            db.collection("trainingsplaene").document(planId) //Wir zielen ganz genau auf das existierende Dokument, dessen ID wir beim Start bekommen haben.
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