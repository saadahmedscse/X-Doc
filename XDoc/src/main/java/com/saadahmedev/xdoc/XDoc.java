package com.saadahmedev.xdoc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.saadahmedev.xdoc.utils.DocType;
import com.saadahmedev.xdoc.utils.Extension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class XDoc {

    @SuppressLint("StaticFieldLeak")
    private static XDoc instance = null;
    private final Context context;

    private String folderName = null;
    private String fileName = null;

    private XDoc (Context context) {
        this.context = context;
    }

    public static XDoc getInstance(Context context) {
        if (instance == null) instance = new XDoc(context);

        return instance;
    }

    public XDoc setFolderName(String folderName) {
        this.folderName = folderName;
        return this;
    }

    public XDoc setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public void download(View view, int docType) {
        switch (docType) {
            case DocType.PDF_DOCUMENT: {
                downloadPdf(view);
                break;
            }
            case DocType.IMAGE: {
                downloadImage(view);
                break;
            }
        }
    }

    private void downloadPdf(View view) {
        if (!hasPermission()) throw new RuntimeException("Storage write permission is required");

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getWidth(), view.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        view.draw(page.getCanvas());
        document.finishPage(page);

        String fileName = (this.fileName == null ? getTime() : this.fileName) + Extension.PDF_EXTENSION;
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + (folderName == null ? "XDoc" : folderName);

        File file = new File(dir);
        if (!file.exists()) file.mkdir();
        File outputFile = new File(dir, fileName);
        if (outputFile.exists()) outputFile.delete();

        try {
            outputFile.createNewFile();
            OutputStream outputStream = new FileOutputStream(outputFile);
            document.writeTo(outputStream);
            document.close();
            outputStream.close();

            //Intent to view the file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    outputFile
            );
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);

        } catch (IOException ignored) {}
    }

    private void downloadImage(View view) {
        if (!hasPermission()) throw new RuntimeException("Storage write permission is required");
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private String getTime() {
        return new SimpleDateFormat("dd/MM/yyyy - hh:mm:ss", Locale.getDefault()).format(new Date());
    }
}
