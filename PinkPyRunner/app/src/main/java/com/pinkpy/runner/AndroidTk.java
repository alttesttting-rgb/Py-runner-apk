package com.pinkpy.runner;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chaquo.python.PyObject;

import java.util.concurrent.CountDownLatch;

public final class AndroidTk {
    private static volatile Activity activity;
    private AndroidTk() {}

    public static void attach(Activity host) { activity = host; }

    public static void showWindow(String title, PyObject widgets) {
        Activity a = activity;
        if (a == null || widgets == null) return;
        a.runOnUiThread(() -> {
            LinearLayout layout = new LinearLayout(a);
            layout.setOrientation(LinearLayout.VERTICAL);
            int pad = (int)(16 * a.getResources().getDisplayMetrics().density);
            layout.setPadding(pad, pad, pad, pad);
            for (PyObject item : widgets.asList()) {
                String type = str(item, "kind", "label");
                String text = str(item, "text", "");
                if ("button".equals(type)) {
                    Button b = new Button(a);
                    b.setText(text);
                    b.setOnClickListener(v -> call(item, "command"));
                    layout.addView(b, new LinearLayout.LayoutParams(-1, -2));
                } else if ("entry".equals(type)) {
                    EditText e = new EditText(a);
                    e.setSingleLine(true);
                    e.setHint(text);
                    e.setInputType(InputType.TYPE_CLASS_TEXT);
                    layout.addView(e, new LinearLayout.LayoutParams(-1, -2));
                    e.addTextChangedListener(new android.text.TextWatcher() {
                        public void beforeTextChanged(CharSequence s,int st,int c,int af) {}
                        public void onTextChanged(CharSequence s,int st,int before,int count) {
                            try { item.callAttr("_set_value", s.toString()); } catch (Throwable ignored) {}
                        }
                        public void afterTextChanged(android.text.Editable e) {}
                    });
                } else if ("checkbutton".equals(type)) {
                    CheckBox c = new CheckBox(a);
                    c.setText(text);
                    c.setChecked(bool(item, "value"));
                    c.setOnCheckedChangeListener((v, checked) -> {
                        try { item.callAttr("_set_value", checked); } catch (Throwable ignored) {}
                        call(item, "command");
                    });
                    layout.addView(c, new LinearLayout.LayoutParams(-1, -2));
                } else if ("scale".equals(type)) {
                    TextView label = new TextView(a);
                    label.setText(text);
                    layout.addView(label);
                    SeekBar bar = new SeekBar(a);
                    bar.setMax((int)number(item, "to", 100));
                    bar.setProgress((int)number(item, "from", 0));
                    bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        public void onProgressChanged(SeekBar s,int value,boolean user) {
                            try { item.callAttr("_set_value", value); } catch (Throwable ignored) {}
                        }
                        public void onStartTrackingTouch(SeekBar s) {}
                        public void onStopTrackingTouch(SeekBar s) { call(item, "command"); }
                    });
                    layout.addView(bar);
                } else {
                    TextView label = new TextView(a);
                    label.setText(text);
                    label.setTextColor(Color.BLACK);
                    label.setTextSize(16);
                    layout.addView(label, new LinearLayout.LayoutParams(-1, -2));
                }
            }
            AlertDialog dialog = new AlertDialog.Builder(a)
                    .setTitle(title == null || title.isEmpty() ? "Python GUI" : title)
                    .setView(layout)
                    .setPositiveButton("CLOSE", (d, w) -> {})
                    .create();
            dialog.show();
        });
    }

    public static void showMessage(String title, String message) {
        Activity a = activity;
        if (a != null) a.runOnUiThread(() -> new AlertDialog.Builder(a)
                .setTitle(title).setMessage(message).setPositiveButton("OK", null).show());
    }

    private static String str(PyObject o, String key, String fallback) {
        try { return o.get(key).toString(); } catch (Throwable e) { return fallback; }
    }
    private static double number(PyObject o, String key, double fallback) {
        try { return o.get(key).toDouble(); } catch (Throwable e) { return fallback; }
    }
    private static boolean bool(PyObject o, String key) {
        try { return o.get(key).toBoolean(); } catch (Throwable e) { return false; }
    }
    private static void call(PyObject o, String key) {
        try { PyObject fn = o.get(key); if (fn != null && !fn.isNone()) fn.call(); } catch (Throwable ignored) {}
    }
}
