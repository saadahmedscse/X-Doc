/*
 * Copyright 2018-2023 Saad Ahmed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.saadahmedev.xdoc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.saadahmedev.xdoc.utils.DocType;
import com.saadahmedev.xdoc.utils.Extension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class XDoc {

    @SuppressLint("StaticFieldLeak")
    private static XDoc instance = null;
    private final Context context;

    private String folderName = null;
    private String fileName = null;
    private File outputFile;

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
        String filePrefix = null;
        String extension = null;

        switch (docType) {
            case DocType.PDF_DOCUMENT: {
                filePrefix = "X Doc PDF ";
                extension = Extension.PDF_EXTENSION;
                break;
            }
            case DocType.IMAGE: {
                filePrefix = "X Doc Image ";
                extension = Extension.IMAGE_EXTENSION;
                break;
            }
        }

        fileName = (fileName == null ? filePrefix + getTime() : fileName) + extension;
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        dir += folderName == null ? "" : ("/" + folderName);

        File file = new File(dir);
        if (!file.exists()) file.mkdir();
        outputFile = new File(dir, fileName);
        if (outputFile.exists()) outputFile.delete();
        fileName = null;

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
        makeIntent(docType);
    }

    private void downloadPdf(View view) {
        if (!hasPermission()) throw new RuntimeException("Storage write permission is required");

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getWidth(), view.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        view.draw(page.getCanvas());
        document.finishPage(page);

        try {
            outputFile.createNewFile();
            OutputStream outputStream = new FileOutputStream(outputFile);
            document.writeTo(outputStream);
            document.close();
            outputStream.close();
        } catch (IOException ignored) {}
    }

    private void downloadImage(View view) {
        if (!hasPermission()) throw new RuntimeException("Storage write permission is required");

        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();

        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch (IOException ignored) {}
    }

    private void makeIntent(int docType) {
        String fileType = null;

        switch (docType) {
            case DocType.PDF_DOCUMENT: {
                fileType = "application/pdf";
                break;
            }
            case DocType.IMAGE: {
                fileType = "image/*";
                break;
            }
        }

        try {
            //Intent to view the file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    outputFile
            );
            intent.setDataAndType(uri, fileType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception ignored) {}
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return true;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    private String getTime() {
        String time = String.valueOf(System.currentTimeMillis());
        return time.substring(time.length() - 4);
    }
}
