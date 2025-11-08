package app.yufaan.barcodescaner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private static final int CREATE_FILE_REQUEST_CODE = 1001;

    TextView tvResult;
    Button btnSaveText;
    ArrayList<String> codes;

    private String textToSave;
    private String tempFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvResult = findViewById(R.id.tvResult);
        btnSaveText = findViewById(R.id.btnSavePdf);

        // ✅ Get data passed from previous activity
        codes = getIntent().getStringArrayListExtra("codes");

        // ✅ Build result text
        StringBuilder sb = new StringBuilder();
        sb.append("NAME1, NAME2, BARCODE, QTY\n");
        sb.append("----------------------------------\n");

        if (codes != null && !codes.isEmpty()) {
            for (String line : codes) {
                sb.append(line).append("\n");
            }
        } else {
            sb.append("No barcode data scanned.");
        }

        tvResult.setText(sb.toString());

        // ✅ Handle Save Button
        btnSaveText.setOnClickListener(v -> showFileNameDialog());
    }

    // Step 1: Ask user for file name
    private void showFileNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter file name");

        final EditText input = new EditText(this);
        input.setHint("e.g. MyBarcodes");
        input.setText("Barcode_Result_" + System.currentTimeMillis());
        input.setPadding(50, 40, 50, 10);

        builder.setView(input);

        builder.setPositiveButton("Next", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (!fileName.endsWith(".txt")) {
                fileName = fileName + ".txt";
            }
            tempFileName = fileName;
            textToSave = tvResult.getText().toString();
            createFile(fileName);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Step 2: Let user choose save location
    private void createFile(String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
    }

    // Step 3: Save text to chosen file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                saveTextToUri(uri, textToSave);
            }
        }
    }

    private void saveTextToUri(Uri uri, String text) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(text.getBytes());
                outputStream.flush();
                Toast.makeText(this, "✅ File saved successfully:\n" + tempFileName, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "❌ Failed to open file output stream.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
