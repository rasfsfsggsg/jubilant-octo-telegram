package app.yufaan.barcodescaner;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class InputActivity extends AppCompatActivity {

    EditText etName1, etName2;
    Button btnStartScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        etName1 = findViewById(R.id.etName1);
        etName2 = findViewById(R.id.etName2);
        btnStartScan = findViewById(R.id.btnStartScan);

        btnStartScan.setOnClickListener(v -> startScanner());
    }

    private void startScanner() {
        String name1 = etName1.getText().toString().trim();
        String name2 = etName2.getText().toString().trim();

        if (TextUtils.isEmpty(name1) || TextUtils.isEmpty(name2)) {
            Toast.makeText(this, "Please enter both names", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(InputActivity.this, ScannerActivity.class);
        i.putExtra("name1", name1);
        i.putExtra("name2", name2);
        startActivity(i);
        finish();
    }
}
