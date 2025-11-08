package app.yufaan.barcodescaner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;

    private DecoratedBarcodeView barcodeView;
    private TextView tvLiveResult;
    private Button btnShowResult, btnScan;
    private ImageView torchButton;

    private final Map<String, Integer> scannedData = new HashMap<>();
    private String name1, name2;
    private boolean isTorchOn = false;
    private boolean isScanning = false;

    // ✅ Add ToneGenerator for beep sound
    private ToneGenerator toneGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);
        tvLiveResult = findViewById(R.id.tvLiveResult);
        btnShowResult = findViewById(R.id.btnShowResult);
        btnScan = findViewById(R.id.btnScan);
        torchButton = findViewById(R.id.btnTorch);

        name1 = getIntent().getStringExtra("name1");
        name2 = getIntent().getStringExtra("name2");

        // ✅ Initialize ToneGenerator
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        // Camera permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        torchButton.setOnClickListener(v -> toggleTorch());
        btnShowResult.setOnClickListener(v -> openResultScreen());
        btnScan.setOnClickListener(v -> startSingleScan());
    }

    // Start scanning only when Scan button is pressed
    private void startSingleScan() {
        if (isScanning) return;
        isScanning = true;

        Toast.makeText(this, "Scanning started...", Toast.LENGTH_SHORT).show();

        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                isScanning = false;

                if (result.getText() != null && !result.getText().trim().isEmpty()) {
                    String code = result.getText().trim();
                    processScan(code);

                    // ✅ Play beep sound immediately on successful scan
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);

                    Toast.makeText(ScannerActivity.this, "✅ Scan Successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ScannerActivity.this, "❌ Scan not successful, try again", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) { }
        });
    }

    // Update / add quantity for each scanned code
    private void processScan(String code) {
        if (scannedData.containsKey(code)) {
            int qty = scannedData.get(code);
            scannedData.put(code, qty + 1);
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
        i.putExtra("name1", name1);
        i.putExtra("name2", name2);
        startActivity(i);
    }

    // Torch toggle
    private void toggleTorch() {
        if (isTorchOn) {
            barcodeView.setTorchOff();
            torchButton.setImageResource(R.drawable.ic_torch_off);
            isTorchOn = false;
        } else {
            barcodeView.setTorchOn();
            torchButton.setImageResource(R.drawable.ic_torch_on);
            isTorchOn = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
