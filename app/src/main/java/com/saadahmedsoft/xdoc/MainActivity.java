package com.saadahmedsoft.xdoc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.widget.Button;

import com.saadahmedev.xdoc.XDoc;
import com.saadahmedev.xdoc.utils.DocType;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.btn_download);
        CardView card = findViewById(R.id.layout_card);

        button.setOnClickListener(v ->
                XDoc.getInstance(this)
                        .setFolderName("XX")
                        .setFileName("A file")
                        .download(card, DocType.PDF_DOCUMENT));
    }
}