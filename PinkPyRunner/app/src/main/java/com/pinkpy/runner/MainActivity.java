package com.pinkpy.runner;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends Activity {
    private static final int PICK_PYTHON_FILE = 4107;
    private static final int BG = Color.rgb(24, 11, 25);
    private static final int PANEL = Color.rgb(39, 20, 42);
    private static final int PINK = Color.rgb(255, 79, 163);
    private static final int PALE = Color.rgb(255, 207, 232);

    private TextView selectedLabel;
    private TextView statusLabel;
    private TextView console;
    private ScrollView consoleScroll;
    private Button runButton;
    private Button stopButton;
    private File selectedScript;
    private ExecutorService executor;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private volatile boolean running = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        executor = Executors.newSingleThreadExecutor();
        buildUi();
        appendConsole("PinkPy Runner ready. Select a .py file to begin.\n");
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(20));
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("PinkPy Runner");
        title.setTextColor(PINK);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView subtitle = new TextView(this);
        subtitle.setText("PYTHON • ANDROID • QUEST");
        subtitle.setTextColor(PALE);
        subtitle.setTextSize(12);
        subtitle.setLetterSpacing(0.14f);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, dp(28));
        subParams.bottomMargin = dp(18);
        root.addView(subtitle, subParams);

        Button selectButton = makeButton("SELECT A PY FILE", PINK, Color.WHITE);
        root.addView(selectButton, new LinearLayout.LayoutParams(-1, dp(56)));
        selectButton.setOnClickListener(v -> openPicker());

        selectedLabel = new TextView(this);
        selectedLabel.setText("No file selected");
        selectedLabel.setTextColor(PALE);
        selectedLabel.setTextSize(14);
        selectedLabel.setGravity(Gravity.CENTER_VERTICAL);
        selectedLabel.setPadding(dp(14), 0, dp(14), 0);
        selectedLabel.setSingleLine(true);
        selectedLabel.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        selectedLabel.setBackground(panelBackground());
        LinearLayout.LayoutParams fileParams = new LinearLayout.LayoutParams(-1, dp(44));
        fileParams.topMargin = dp(12);
        root.addView(selectedLabel, fileParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, dp(52));
        actionsParams.topMargin = dp(14);
        root.addView(actions, actionsParams);

        runButton = makeButton("RUN", Color.rgb(235, 55, 145), Color.WHITE);
        stopButton = makeButton("STOP", Color.rgb(105, 48, 83), Color.WHITE);
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, -1, 1f);
        actions.addView(runButton, half);
        LinearLayout.LayoutParams secondHalf = new LinearLayout.LayoutParams(0, -1, 1f);
        secondHalf.leftMargin = dp(10);
        actions.addView(stopButton, secondHalf);
        runButton.setOnClickListener(v -> runSelectedScript());
        stopButton.setOnClickListener(v -> requestStop());
        stopButton.setEnabled(false);

        statusLabel = new TextView(this);
        statusLabel.setText("●  IDLE");
        statusLabel.setTextColor(PALE);
        statusLabel.setTextSize(13);
        statusLabel.setPadding(dp(4), dp(14), dp(4), dp(8));
        root.addView(statusLabel);

        TextView consoleTitle = new TextView(this);
        consoleTitle.setText("CONSOLE OUTPUT");
        consoleTitle.setTextColor(PINK);
        consoleTitle.setTextSize(12);
        consoleTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        consoleTitle.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams consoleTitleParams = new LinearLayout.LayoutParams(-1, dp(30));
        consoleTitleParams.topMargin = dp(4);
        root.addView(consoleTitle, consoleTitleParams);

        consoleScroll = new ScrollView(this);
        consoleScroll.setFillViewport(true);
        consoleScroll.setBackground(panelBackground());
        console = new TextView(this);
        console.setTextColor(Color.rgb(245, 225, 238));
        console.setTextSize(13);
        console.setTypeface(Typeface.MONOSPACE);
        console.setPadding(dp(14), dp(14), dp(14), dp(14));
        console.setTextIsSelectable(true);
        consoleScroll.addView(console, new ScrollView.LayoutParams(-1, -2));
        LinearLayout.LayoutParams consoleParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        root.addView(consoleScroll, consoleParams);

        TextView footer = new TextView(this);
        footer.setText("Python 3.13 bundled • ARM64 build");
        footer.setTextColor(Color.rgb(160, 123, 150));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(-1, dp(30));
        footerParams.topMargin = dp(8);
        root.addView(footer, footerParams);

        setContentView(root);
    }

    private Button makeButton(String text, int color, int textColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(textColor);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setBackground(roundBackground(color, dp(15)));
        button.setPadding(dp(12), 0, dp(12), 0);
        return button;
    }

    private GradientDrawable roundBackground(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private GradientDrawable panelBackground() {
        GradientDrawable shape = roundBackground(PANEL, dp(14));
        shape.setStroke(dp(1), Color.rgb(92, 42, 76));
        return shape;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "text/x-python", "application/x-python-code", "text/plain", "application/octet-stream"
        });
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, PICK_PYTHON_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PYTHON_FILE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        String name = getDisplayName(uri);
        if (name == null || !name.toLowerCase(java.util.Locale.ROOT).endsWith(".py")) {
            Toast.makeText(this, "Please select a .py file", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            File folder = new File(getFilesDir(), "selected_scripts");
            if (!folder.exists() && !folder.mkdirs()) throw new java.io.IOException("Cannot create script folder");
            selectedScript = new File(folder, UUID.randomUUID().toString() + ".py");
            try (InputStream input = getContentResolver().openInputStream(uri);
                 FileOutputStream output = new FileOutputStream(selectedScript)) {
                if (input == null) throw new java.io.IOException("Unable to open selected file");
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            }
            selectedLabel.setText(name);
            statusLabel.setText("●  FILE READY");
            appendConsole("\n[PinkPy] Selected: " + name + "\n");
        } catch (Exception error) {
            selectedScript = null;
            appendConsole("[File error] " + error + "\n");
            Toast.makeText(this, "Could not read that file", Toast.LENGTH_LONG).show();
        }
    }

    private String getDisplayName(Uri uri) {
        String result = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) result = cursor.getString(index);
            }
        } catch (Exception ignored) { }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }

    private void runSelectedScript() {
        if (running) return;
        if (selectedScript == null || !selectedScript.exists()) {
            Toast.makeText(this, "Select a .py file first", Toast.LENGTH_SHORT).show();
            return;
        }
        stopRequested.set(false);
        running = true;
        runButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusLabel.setText("●  RUNNING");
        appendConsole("\n------------------------------\n");

        executor.execute(() -> {
            try {
                Python python = Python.getInstance();
                PyObject runner = python.getModule("runner");
                runner.callAttr("run_script", selectedScript.getAbsolutePath(), new ConsoleSink() {
                    @Override public void onOutput(String text) {
                        runOnUiThread(() -> appendConsole(text));
                    }
                }, (ScriptControl) stopRequested::get);
            } catch (Throwable error) {
                runOnUiThread(() -> appendConsole("\n[Runner error] " + error + "\n"));
            } finally {
                runOnUiThread(() -> {
                    running = false;
                    runButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    statusLabel.setText(stopRequested.get() ? "●  STOPPED" : "●  IDLE");
                });
            }
        });
    }

    private void requestStop() {
        if (!running) return;
        stopRequested.set(true);
        statusLabel.setText("●  STOPPING...");
        stopButton.setEnabled(false);
    }

    private void appendConsole(String text) {
        console.append(text);
        int maxChars = 90000;
        if (console.length() > maxChars) {
            console.setText(console.getText().subSequence(console.length() - maxChars, console.length()));
        }
        consoleScroll.post(() -> consoleScroll.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        stopRequested.set(true);
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }
}
