package app.yufaan.barcodescaner;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity {

    private EditText etScanInput;
    private TextView tvLiveResult;
    private Button btnShowResult;

    private final Map<String, Integer> scannedData = new HashMap<>();

    private String name1, name2;

    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        etScanInput = findViewById(R.id.etScanInput);
        tvLiveResult = findViewById(R.id.tvLiveResult);
        btnShowResult = findViewById(R.id.btnShowResult);

        name1 = getIntent().getStringExtra("name1");
        name2 = getIntent().getStringExtra("name2");

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        // Auto scan from CRUISE2 device (HID)
        etScanInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String code = s.toString().trim();
                if (code.length() > 0) {
                    processScan(code);
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                    etScanInput.setText(""); // Clear after scan
                }
            }
        });

        btnShowResult.setOnClickListener(v -> openResultScreen());

        etScanInput.requestFocus();
    }

    private void processScan(String code) {
        if (scannedData.containsKey(code)) {
            scannedData.put(code, scannedData.get(code) + 1);
        } else {
            scannedData.put(code, 1);
        }
        updateLiveResult();
    }

    private void updateLiveResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("NAME1, NAME2, BARCODE, QTY\n");
        sb.append("----------------------------------\n");

        for (Map.Entry<String, Integer> entry : scannedData.entrySet()) {
            sb.append(name1).append(", ")
                    .append(name2).append(", ")
                    .append(entry.getKey()).append(", ")
                    .append(entry.getValue()).append("\n");
        }

        tvLiveResult.setText(sb.toString());
    }

    private void openResultScreen() {
        ArrayList<String> resultList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : scannedData.entrySet()) {
            String line = name1 + ", " + name2 + ", " + entry.getKey() + ", " + entry.getValue();
            resultList.add(line);
        }

        Intent i = new Intent(ScannerActivity.this, ResultActivity.class);
        i.putStringArrayListExtra("codes", resultList);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) toneGenerator.release();
    }
}
