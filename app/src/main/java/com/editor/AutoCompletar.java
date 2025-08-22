package com.editor;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.text.Layout;

public class AutoCompletar {
    public final Activity activity;
    public final EditText editText;
    public final String[] palavras;
    public final PopupWindow popup;
    public final LinearLayout layout;

    public AutoCompletar(Activity act, EditText et, String... lista) {
        activity = act;
        editText = et;
        palavras = lista;
        layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
        popup = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popup.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        popup.setOutsideTouchable(true);
        popup.setFocusable(false);

        editText.addTextChangedListener(new TextWatcher() {
				@Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
				@Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
				@Override public void afterTextChanged(Editable s) {
					mostrarSugestoes(obterTokenAtual());
				}
			});
    }

    public void mostrarSugestoes(String token) {
        layout.removeAllViews();
        if(token.length() == 0) {
            popup.dismiss();
            return;
        }
        List<String> sugestoes = new ArrayList<String>();
        for(String p : palavras) {
            if(p.startsWith(token)) sugestoes.add(p);
        }

        if(sugestoes.isEmpty()) {
            popup.dismiss();
            return;
        }

        for(final String s : sugestoes) {
            TextView t = new TextView(activity);
            t.setText(s);
            t.setPadding(20, 10, 20, 10);
            t.setTextSize(16);
            t.setTextColor(Color.BLACK);
            t.setBackgroundColor(0xFFE0E0E0);
            t.setOnClickListener(new View.OnClickListener() {
					@Override public void onClick(View v) {
						substituirToken(s);
						popup.dismiss();
						editText.requestFocus();
						InputMethodManager im = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
						im.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
					}
				});
            layout.addView(t);
        }
		
		int antes = editText.getSelectionStart();
		Layout layout = editText.getLayout();

		if(layout != null && antes >= 0) {
			int linha = layout.getLineForOffset(antes);
			int baselinha = layout.getLineBaseline(linha);
			int ascent = layout.getLineAscent(linha);
			float x = layout.getPrimaryHorizontal(antes);

			int[] loc = new int[2];
			editText.getLocationOnScreen(loc);

			int popupX = (int)(loc[0] + x);
			int popupY = loc[1] + baselinha + ascent;

			if (!popup.isShowing()) {
				popup.setWidth(editText.getWidth() / 2);
				popup.showAtLocation(editText, Gravity.NO_GRAVITY, popupX, popupY);
			} else {
				popup.update(popupX, popupY, popup.getWidth(), popup.getHeight());
			}
		}
    }

    public String obterTokenAtual() {
        int pos = editText.getSelectionStart();
        if(pos == 0) return "";
        CharSequence txt = editText.getText();
        int i = pos - 1;
        while(i >= 0 && Character.isLetterOrDigit(txt.charAt(i))) i--;
        return txt.subSequence(i + 1, pos).toString();
    }

    public void substituirToken(String nova) {
        int pos = editText.getSelectionStart();
        Editable txt = editText.getText();
        int i = pos - 1;
        while(i >= 0 && Character.isLetterOrDigit(txt.charAt(i))) i--;
        txt.replace(i + 1, pos, nova);
        editText.setSelection(i + 1 + nova.length());
    }
    
    public static String[] sintaxe(String linguagem) {
        if(linguagem.equals("java")) {
            return new String[]{
                "System.out.println(", "System.gc()", "Toast.makeText(",
                "while(", "if(", "for(", "switch(", "try {\n\n\n} catch(Exception e) {\nSystem.out.println(\"erro: \"+e);\n}", 
                ".replace(", ".split(", ".trim()", ".toString()", "setContentView(", ".setText(", ".getText(",

                "public", "private", "protected", "static", "class", "void", "final",

                "int", "String", "double", "float", "byte", "short", "Integer", "long", "char", "boolean",

                "import", "package", "new", "return", "case", "break;", "continue;", "else", "this",

                "@Override", "@javascriptInterface",

                "WebView", "EditText", "TextView", "Button", "GLSurfaceView", "SurfaceView",
                "Canvas", "Toast", "View", "MediaPlayer"
            };
        } else if(linguagem.equals("JS")) {
            return new String[]{
                ".log(", ".error(", ".table(", "fetch(", ".addEventListener(", ".replace(",
                ".trim()", ".split(",  "while(", "for(", "switch(", "if(", 
                ".getElementById(", ".forEach(",

                "let", "const", "var",

                "class", "static", "function", "import",

                "=>", "return", "new", "else", "case", "break", "of", "in",

                "document", "window", "Image", "Audio", "console"
            };
        } else if(linguagem.equals("FP")) {
            return new String[]{
                "log(", "se(", "enq(", "por(", "FPexec(",
                "mAleatorio()", "mPI()", "mCos(", "mSen(", "mAbs(",
                "lerArquivo(", "gravarArquivo(", "execArquivo(", "obExterno()",

                "classe", "novo", "este", "senao", "func",

                "var", "Tex", "Dobro", "Int", "Flutu", "Bool"
            };
        } else if(linguagem.equals("asm-arm64")) {
            return new String[]{
                "mov", "ldr", "str", "add", "sub", "mul", "udiv", "sdiv",
                "and", "orr", "eor", "lsl", "lsr", "asr", "cmp", "cmn",
                "b", "bl", "ret", "cbz", "cbnz", "b.eq", "b.ne", "b.lt", "b.gt",
                
                "sp", "pc",

                "text", "data", "rodata", "bss", "global", "section", "align", "asciz", "word", "byte",

                "adr", "adrp", "nop", "svc"
            };
        }
        return new String[]{"desconhecida"};
    }
}
