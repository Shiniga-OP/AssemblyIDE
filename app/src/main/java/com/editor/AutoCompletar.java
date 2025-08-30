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
import android.widget.ScrollView;    
import android.view.KeyEvent;    
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.widget.Button;
import android.widget.Toast;
import android.view.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.*;

public class AutoCompletar {    
    public final Activity activity;    
    public final EditText editText;    
    public final Sugestao[] palavras;    
    public final List<Sugestao> sugestoes = new ArrayList<>();    
    public final PopupWindow popup;    
    public final LinearLayout layout;    
    public static boolean autocomplete = false;
    public final HashSet<Sugestao> palavrasDoc = new HashSet<>();
	
	public static class Sugestao {
		public String codigo, tipo;
		public CharSequence[] args;
		
		public Sugestao(String codigo, String tipo, CharSequence... args) {
			this.codigo = codigo;
			this.tipo = tipo;
			this.args = args;
		}
	}

    public AutoCompletar(Activity act, EditText et, Sugestao... lista) {    
        activity = act;    
        editText = et;    
        palavras = lista;    

        for(Sugestao palavra : palavras) palavrasDoc.add(palavra);

        editText.addTextChangedListener(new olhadorSintaxe(editText));    
        layout = new LinearLayout(activity);    
        layout.setOrientation(LinearLayout.VERTICAL);    
        layout.setBackgroundColor(Color.WHITE);    

        ScrollView sugs = new ScrollView(activity);    
        sugs.addView(layout);    

        popup = new PopupWindow(sugs, editText.getWidth(), 300);    
        popup.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));    
        popup.setOutsideTouchable(true);    
        popup.setFocusable(false);    

        editText.addTextChangedListener(new TextWatcher() {    
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}    
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}    
                @Override public void afterTextChanged(Editable s) {
					palavrasDoc.clear();
					coletarPalavrasDoDoc(s.toString());
                    mostrarSugestoes(obterTokenAtual());    
                }    
            });    
    }

    public void coletarPalavrasDoDoc(String texto) {
        Pattern p = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
        Matcher m = p.matcher(texto);
        while(m.find()) {
            Sugestao palavra = new Sugestao(m.group(), "vazio", "vazio");
            if(!palavrasDoc.contains(palavra) && palavra.codigo.length() > 1) palavrasDoc.add(palavra);
        }
    }

	public Drawable itemSugs() {
		GradientDrawable normal = new GradientDrawable();
		normal.setColor(0xFFFAFAFA); // cinza muito claro
		normal.setCornerRadius(6);

		GradientDrawable tocado = new GradientDrawable();
		tocado.setColor(0xFFE3F2FD); // azul claro
		tocado.setCornerRadius(6);

		StateListDrawable estados = new StateListDrawable();
		estados.addState(new int[]{android.R.attr.state_pressed}, tocado);
		estados.addState(new int[]{}, normal);

		return estados;
	}

	public int corPorTipo(String tipo) {
		switch(tipo != null ? tipo : "vazio") {
			case "Metodo": return 0xFF1976D2; // azul
			case "PalavraChave": return 0xFFD32F2F; // vermelho
			case "Tipo": return 0xFF388E3C; // verde
			case "Anotacao": return 0xFF7B1FA2; // roxo
			case "Classe": return 0xFFF57C00; // laranja
			case "Condicional": return 0xFF0288D1; // azul claro
			case "Bloco": return 0xFF5D4037; // marrom
			default: return 0xFF616161; // cinza escuro
		}
	}

    public void mostrarSugestoes(String token) {    
		sugestoes.clear();    
		layout.removeAllViews();    
		if(token.length() == 0) {    
			popup.dismiss();    
			return;    
		}    
		List<Sugestao> suges = new ArrayList<>();    
		for(Sugestao p : palavrasDoc) {    
			if(p.codigo.toLowerCase().startsWith(token.toLowerCase())) suges.add(p);    
		}
		if(suges.isEmpty()) {
			for(Sugestao p : palavras) {    
				if(p.codigo.toLowerCase().startsWith(token.toLowerCase())) suges.add(p);    
			}
		}
		if(suges.isEmpty()) {    
			popup.dismiss();    
			return;    
		}    
		for(final Sugestao s : suges) {
			LinearLayout itemLayout = new LinearLayout(activity);
			itemLayout.setOrientation(LinearLayout.HORIZONTAL);
			itemLayout.setPadding(30, 20, 30, 20);
			itemLayout.setBackground(itemSugs());
			itemLayout.setGravity(Gravity.CENTER_VERTICAL);

			View icone = new View(activity);
			LinearLayout.LayoutParams iconeArgs = new LinearLayout.LayoutParams(16, 16);
			iconeArgs.setMargins(0, 0, 20, 0);
			icone.setLayoutParams(iconeArgs);
			icone.setBackgroundColor(corPorTipo(s.tipo));
			GradientDrawable circulo = new GradientDrawable();
			circulo.setShape(GradientDrawable.OVAL);
			circulo.setColor(corPorTipo(s.tipo));
			icone.setBackground(circulo);
			itemLayout.addView(icone);

			TextView t = new TextView(activity);    
			sugestoes.add(s);    
			t.setText(s.codigo);
			t.setTextSize(16);
			t.setTextColor(0xFF212121);
			t.setTypeface(t.getTypeface(), Typeface.BOLD);
			
			if(s.args != null) {
				TextView argsView = new TextView(activity);
				String args = "(";
				for(int i = 0; i < s.args.length; i++) {
					if(i < s.args.length - 1) args += s.args[i] + ", ";
					else args += s.args[i];
				}
				argsView.setText(args + ")");
				argsView.setTextSize(12);
				argsView.setTextColor(0xFF757575); // cinza
				argsView.setPadding(10, 0, 0, 0);

				LinearLayout txtLayout = new LinearLayout(activity);
				txtLayout.setOrientation(LinearLayout.HORIZONTAL);
				txtLayout.addView(t);
				txtLayout.addView(argsView);

				itemLayout.addView(txtLayout);
			} else itemLayout.addView(t);
			itemLayout.setOnClickListener(new View.OnClickListener() {    
					@Override public void onClick(View v) {    
						v.setBackgroundColor(0xFFBBDEFB);
						new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									substituirToken(s.codigo);    
									popup.dismiss();    
									editText.requestFocus();    
									InputMethodManager em = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);    
									em.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);    
								}
							}, 100);
					}    
				});    
			layout.addView(itemLayout);    
			View divisor = new View(activity);
			divisor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
			divisor.setBackgroundColor(0xFFEEEEEE);
			layout.addView(divisor);
		}    
		if(layout.getChildCount() > 0) layout.removeViewAt(layout.getChildCount() - 1);

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

            if(!popup.isShowing()) {    
                popup.setWidth(editText.getWidth() / 2);    
                popup.showAtLocation(editText, Gravity.NO_GRAVITY, popupX, popupY);    
            } else popup.update(popupX, popupY, popup.getWidth(), popup.getHeight());    
        }    
    }    

    public String obterTokenAtual() {
        int pos = editText.getSelectionStart();
        if(pos == 0) return "";
        CharSequence txt = editText.getText();
        int i = pos - 1;
        while(i >= 0 && (Character.isLetterOrDigit(txt.charAt(i)) ||
              txt.charAt(i) == '#' || txt.charAt(i) == '_' || txt.charAt(i) == '.' ||
              txt.charAt(i) == '<' || txt.charAt(i) == '>' || txt.charAt(i) == '*' ||
              txt.charAt(i) == '\n'
        )) i--;
        return txt.subSequence(i + 1, pos).toString();
    }    

    public void substituirToken(String nova) {    
        int pos = editText.getSelectionStart();    
        Editable txt = editText.getText();    
        int i = pos - 1;    
        while(i >= 0 && (Character.isLetterOrDigit(txt.charAt(i)) ||
              txt.charAt(i) == '#' || txt.charAt(i) == '_' || txt.charAt(i) == '.' ||
              txt.charAt(i) == '<' || txt.charAt(i) == '>' || txt.charAt(i) == '*' ||
              txt.charAt(i) == '\n'
        )) i--;
        txt.replace(i + 1, pos, nova);    
        editText.setSelection(i + 1 + nova.length());    
    }    
	
	public void mostrarBuscaRapida() {
		try {
			LinearLayout layoutBusca = new LinearLayout(activity);
			layoutBusca.setOrientation(LinearLayout.VERTICAL);
			layoutBusca.setPadding(20, 20, 20, 20);

			final EditText campoBusca = new EditText(activity);
			campoBusca.setHint("Buscar...");

			final EditText campoSubstituir = new EditText(activity);
			campoSubstituir.setHint("Substituir por...");

			Button btnBuscar = new Button(activity);
			btnBuscar.setText("Buscar");

			Button btnSubstituir = new Button(activity);
			btnSubstituir.setText("Substituir");

			Button btnSubstituirTudo = new Button(activity);
			btnSubstituirTudo.setText("Substituir Tudo");

			layoutBusca.addView(campoBusca);
			layoutBusca.addView(campoSubstituir);
			layoutBusca.addView(btnBuscar);
			layoutBusca.addView(btnSubstituir);
			layoutBusca.addView(btnSubstituirTudo);

			final PopupWindow popupBusca = new PopupWindow(layoutBusca, 600, 400);
			popupBusca.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
			popupBusca.setOutsideTouchable(true);
			popupBusca.setFocusable(true);

			int[] local = new int[2];
			editText.getLocationOnScreen(local);
			popupBusca.showAtLocation(editText, Gravity.TOP, local[0], local[1] + 50);

			btnBuscar.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String textoBusca = campoBusca.getText().toString();
						if(!textoBusca.isEmpty()) {
							int pos = editText.getText().toString().indexOf(textoBusca);
							if(pos != -1) editText.setSelection(pos, pos + textoBusca.length());
						}
					}
				});

			btnSubstituir.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String textoBusca = campoBusca.getText().toString();
						String textoSubstituir = campoSubstituir.getText().toString();

						if(!textoBusca.isEmpty()) {
							Editable texto = editText.getText();
							int i = editText.getSelectionStart();
							int e = editText.getSelectionEnd();
							if(i != e) {
								String selecionado = texto.subSequence(i, e).toString();
								if(selecionado.equals(textoBusca)) texto.replace(i, e, textoSubstituir);
							}
						}
					}
				});

			btnSubstituirTudo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String textoBusca = campoBusca.getText().toString();
						String textoSubstituir = campoSubstituir.getText().toString();

						if(!textoBusca.isEmpty()) {
							String textoCompleto = editText.getText().toString();
							String novoTxt = textoCompleto.replace(textoBusca, textoSubstituir);
							editText.setText(novoTxt);
						}
					}
				});
		} catch(Exception e) {
			Toast.makeText(activity, "erro: "+e, Toast.LENGTH_SHORT).show();
		}
	}

    public class olhadorSintaxe implements TextWatcher {    
        public final EditText editor;    
        public boolean processando = false;    
        public int ultimoInicio, ultimoAntes, ultimaQuantidade;    

        public olhadorSintaxe(EditText editor) {    
            this.editor = editor;    
        }    

        @Override    
        public void beforeTextChanged(CharSequence s, int i, int c, int a) { }    

        @Override    
        public void onTextChanged(CharSequence s, int i, int a, int c) {    
            ultimoInicio = i;    
            ultimoAntes = a;    
            ultimaQuantidade = c;    
        }    

        @Override
        public void afterTextChanged(Editable txt) {
            if(processando) return;
            processando = true;

            if(ultimoAntes == 0 && ultimaQuantidade == 1) {
                char c = txt.charAt(ultimoInicio);
				int pos = ultimoInicio;
				if(pos >= 6 && txt.subSequence(pos-6, pos+1).toString().equals("//busca")) {
					txt.replace(pos-6, pos+1, "");
					mostrarBuscaRapida();
				}
                if(c == '\n') {
                    if(!sugestoes.isEmpty() && autocomplete) {
                        int i = pos - 1;
                        while(i >= 0 && (Character.isLetterOrDigit(txt.charAt(i)) ||
                              txt.charAt(i) == '#' || txt.charAt(i) == '_' || txt.charAt(i) == '.' ||
                              txt.charAt(i) == '<' || txt.charAt(i) == '>' || txt.charAt(i) == '*' ||
                              txt.charAt(i) == '\n'
                        )) i--;
                        int tokenI = i + 1;
                        String nova = sugestoes.get(0).codigo;
                        txt.replace(tokenI, pos, nova);
                        pos = tokenI + nova.length();
                        editor.setSelection(pos + 1);
                        popup.dismiss();
                        sugestoes.clear();
                    }
                    int lin = pos - 1;
                    while(lin >= 0 && txt.charAt(lin) != '\n') lin--;
                    int inicioLinha = lin + 1;
                    int contadorTabs = 0;
                    while(inicioLinha + contadorTabs < txt.length() && txt.charAt(inicioLinha + contadorTabs) == '\t') contadorTabs++;

                    StringBuilder sb = new StringBuilder();
                    for(int i2 = 0; i2 < contadorTabs; i2++) sb.append('\t');

                    int fim = pos - 1;
                    while(fim >= 0 && (txt.charAt(fim) == '\t' || txt.charAt(fim) == ' ')) fim--;
                    boolean abre = fim >= 0 && (txt.charAt(fim) == '{' || txt.charAt(fim) == ':');
                    if(abre) sb.append('\t');

                    String inden = sb.toString();
                    txt.insert(pos + 1, inden);
                    editor.setSelection(pos + 1 + inden.length());
                } else if(c == '{') {
                    if(ultimoInicio + 1 >= txt.length() || txt.charAt(ultimoInicio + 1) != '}')
                        txt.insert(ultimoInicio + 1, "}");
                    editor.setSelection(ultimoInicio);
                }
            }
            processando = false;
        }    
    }    
	
	public static Sugestao[] sintaxe(String linguagem) {    
		if(linguagem.equals("java")) {
			return new Sugestao[]{
				new Sugestao("System.out.println(", "Metodo", "Object"),
				new Sugestao("System.gc();", "Metodo", "void"),
				new Sugestao("Toast.makeText", "Metodo", "Activity", "String", "int"),
				new Sugestao("while(", "condicional", "boolean"),
				new Sugestao("if(", "condicional", "boolean"),
				new Sugestao("for(", "condicional", "Object, boolean, Object"),
				new Sugestao("switch(", "condicional", "Object"),
				new Sugestao("try {\n\n\n} catch(Exception e) {\nSystem.out.println(\"erro: \"+e);\n}", "bloco"),
				new Sugestao(".replace(", "Metodo", "String", "String"),
				new Sugestao(".split(", "Metodo", "String"),
				new Sugestao(".trim()", "Metodo", "void"),
				new Sugestao(".toString()", "Metodo", "void"),
				new Sugestao("setContentView(", "Metodo", "int"),
				new Sugestao(".setText(", "Metodo", "String"),
				new Sugestao(".getText()", "Metodo", "void"),
				new Sugestao("public", "palavraChave"),
				new Sugestao("private", "palavraChave"),
				new Sugestao("protected", "palavraChave"),
				new Sugestao("static", "palavraChave"),
				new Sugestao("class", "palavraChave"),
				new Sugestao("void", "palavraChave"),
				new Sugestao("final", "palavraChave"),
				new Sugestao("int", "tipo"),
				new Sugestao("String", "tipo"),
				new Sugestao("double", "tipo"),
				new Sugestao("float", "tipo"),
				new Sugestao("byte", "tipo"),
				new Sugestao("short", "tipo"),
				new Sugestao("Integer", "tipo"),
				new Sugestao("long", "tipo"),
				new Sugestao("char", "tipo"),
				new Sugestao("boolean", "tipo"),
				new Sugestao("import", "palavraChave"),
				new Sugestao("package", "palavraChave"),
				new Sugestao("new", "palavraChave"),
				new Sugestao("return", "palavraChave"),
				new Sugestao("case", "palavraChave"),
				new Sugestao("break;", "palavraChave"),
				new Sugestao("continue;", "palavraChave"),
				new Sugestao("else", "palavraChave"),
				new Sugestao("this", "palavraChave"),
				new Sugestao("@Override", "anotacao"),
				new Sugestao("@JavascriptInterface", "anotacao"),
				new Sugestao("@Deprecated", "anotacao"),
				new Sugestao("@Target", "anotacao"),
				new Sugestao("@TargetApi", "anotacao"),
				new Sugestao("WebView", "Classe", "Activity"),
				new Sugestao("EditText", "Classe", "Activity"),
				new Sugestao("TextView", "Classe", "Activity"),
				new Sugestao("Button", "Classe", "Activity"),
				new Sugestao("GLSurfaceView", "Classe", "Activity"),
				new Sugestao("SurfaceView", "Classe", "activity"),
				new Sugestao("Canvas", "Classe", "Activity"),
				new Sugestao("Toast", "Classe", "void"),
				new Sugestao("View", "Classe", "Activity"),
				new Sugestao("MediaPlayer", "Classe", "Activity"),
				new Sugestao("RuntimeException", "Classe", "void"),
				new Sugestao("Exception", "Classe", "void"),
				new Sugestao("InputStream", "Classe", "void"),
				new Sugestao("OutputStream", "Classe", "void"),
				new Sugestao("PrintStream", "Classe", "void")
			};
		}
		if(linguagem.equals("JS")) {    
            return new Sugestao[]{
				new Sugestao("console.log(", "Metodo", "Object"),
				new Sugestao("console.clear();", "Metodo", "void"),
				new Sugestao("Math.random(", "int"),
				new Sugestao("while(", "Condicional", "boolean"),
				new Sugestao("if(", "Condicional", "boolean"),
				new Sugestao("for(", "Condicional", "Object, boolean, Object"),
				new Sugestao("switch(", "Condicional", "Object"),
				new Sugestao("try {\n\n\n} catch(e) {\nconsole.log(\"erro: \"+e);\n}", "void"),
				new Sugestao(".replace(", "Metodo", "String", "String"),
				new Sugestao(".split(", "Metodo", "String"),
				new Sugestao(".trim()", "Metodo", "void"),
				new Sugestao("static", "PalavraChave"),
				new Sugestao("class", "PalavraChave"),
				new Sugestao("const", "Tipo"),
				new Sugestao("let", "Tipo"),
				new Sugestao("var", "Tipo"),
				new Sugestao("import", "PalavraChave"),
				new Sugestao("new", "PalavraChave"),
				new Sugestao("return", "PalavraChave"),
				new Sugestao("case", "PalavraChave"),
				new Sugestao("break;", "PalavraChave"),
				new Sugestao("continue;", "PalavraChave"),
				new Sugestao("else", "PalavraChave"),
				new Sugestao("this", "PalavraChave"),
				new Sugestao("Image", "Classe", "void"),
				new Sugestao("document", "Classe", "void"),
				new Sugestao("Audio", "Classe", "String"),
				new Sugestao("Array", "Classe", "int"),
				new Sugestao("Float32Array", "Classe", "int"),
				new Sugestao("window", "Classe", "Activity")
			};    
        }
		if(linguagem.equals("C")) {    
            return new Sugestao[]{
				new Sugestao("printf(", "função", "String", "Object"),
				new Sugestao("malloc(", "função", "int"),
				new Sugestao("free(", "função", "Object"),
				new Sugestao("while(", "Condicional", "boolean"),
				new Sugestao("if(", "Condicional", "boolean"),
				new Sugestao("for(", "Condicional", "Object, boolean, Object"),
				new Sugestao("switch(", "Condicional", "Object"),
				new Sugestao("static", "PalavraChave"),
				new Sugestao("const", "PalavraChave"),
				new Sugestao("short", "Tipo"),
				new Sugestao("int", "Tipo"),
				new Sugestao("byte", "Tipo"),
				new Sugestao("long", "Tipo"),
				new Sugestao("float", "Tipo"),
				new Sugestao("FILE", "Tipo"),
				new Sugestao("bool", "Tipo"),
				new Sugestao("#include", "PalavraChave"),
				new Sugestao("#define", "PalavraChave"),
				new Sugestao("#if", "PalavraChave"),
				new Sugestao("#ifdef", "PalavraChave"),
				new Sugestao("#end", "PalavraChave"),
				new Sugestao("#undef", "PalavraChave"),
				new Sugestao("return", "PalavraChave"),
				new Sugestao("case", "PalavraChave"),
				new Sugestao("break;", "PalavraChave"),
				new Sugestao("continue;", "PalavraChave"),
				new Sugestao("else", "PalavraChave"),
				new Sugestao("struct", "PalavraChave"),
				new Sugestao("typedef", "PalavraChave"),
				new Sugestao("enum", "PalavraChave"),
			};    
        }
		if(linguagem.equals("FP")) {
			return new Sugestao[]{
				new Sugestao("log(", "Metodo", "Objeto"),
				new Sugestao("se(", "Condicional", "booleano"),
				new Sugestao("enq()", "Condicional", "booleano"),
				new Sugestao("por(", "Condicional", "Objeto, booleano, Objeto"),
				new Sugestao("Int", "Tipo"),
				new Sugestao("Tex", "Tipo"),
				new Sugestao("Bool", "Tipo"),
				new Sugestao("Flutu", "Tipo"),
				new Sugestao("Dobro", "Tipo"),
				new Sugestao("var", "Tipo"),
				new Sugestao("#incluir", "PalavraChave"),
				new Sugestao("func", "PalavraChave"),
				new Sugestao("classe", "PalavraChave"),
				new Sugestao("novo", "PalavraChave"),
				new Sugestao("Este", "PalavraChave"),
				new Sugestao("mPI()", "Metodo", "vazio"),
				new Sugestao("mCos(", "Metodo", "Flutu"),
				new Sugestao("mSen(", "Metodo", "Flutu"),
				new Sugestao("mAbs(", "Metodo", "Flutu"),
				new Sugestao("mAleatorio()", "Metodo", "vazio"),
				new Sugestao("gravarArquivo(", "Metodo", "Tex", "Tex"),
				new Sugestao("lerArquivo(", "Metodo", "Tex"),
				new Sugestao("obExterno()", "Metodo", "vazio"),
				new Sugestao("execArquivo(", "Metodo", "Tex"),
				new Sugestao("FPexec(", "Metodo", "Tex"),
			};    
        }
		if(linguagem.equals("asm-arm64")) {    
			return new Sugestao[]{
				new Sugestao("mov", "instrucao", "registrador, registrador/valor"),
				new Sugestao("ldr", "instrucao", "registrador, [endereço]"),
				new Sugestao("str", "instrucao", "registrador, [endereço]"),
				new Sugestao("add", "instrucao", "registrador, registrador, registrador/valor"),
				new Sugestao("sub", "instrucao", "registrador, registrador, registrador/valor"),
				new Sugestao("mul", "instrucao", "registrador, registrador, registrador"),
				new Sugestao("udiv", "instrucao", "registrador, registrador, registrador"),
				new Sugestao("sdiv", "instrucao", "registrador, registrador, registrador"),
				new Sugestao("strb", "instrucao", "registrador, [endereço]"),
				new Sugestao("and", "instrucao", "registrador, registrador, registrador/valor"),
				new Sugestao("orr", "instrucao", "registrador, registrador, registrador/valor"),
				new Sugestao("eor", "instrucao", "registrador, registrador, registrador/valor"),
				new Sugestao("lsl", "instrucao", "registrador, registrador, registrador/valor"),
				new Sugestao("lsr", "instrucao", "registrador, registrador, registrador/valor"),
				new Sugestao("asr", "instrucao", "registrador, registrador, registrador/valor"),
				new Sugestao("cmp", "instrucao", "registrador, registrador/valor"),
				new Sugestao("cmn", "instrucao", "registrador, registrador/valor"),
				new Sugestao("b", "instrucao", "rótulo"),
				new Sugestao("bl", "instrucao", "rótulo"),
				new Sugestao("ret", "instrucao", ""),
				new Sugestao("cbz", "instrucao", "registrador, rótulo"),
				new Sugestao("cbnz", "instrucao", "registrador, rótulo"),
				new Sugestao("b.eq", "instrucao", "rótulo"),
				new Sugestao("b.ne", "instrucao", "rótulo"),
				new Sugestao("b.lt", "instrucao", "rótulo"),
				new Sugestao("b.gt", "instrucao", "rótulo"),
				new Sugestao("sp", "registrador", ""),
				new Sugestao("pc", "registrador", ""),
				new Sugestao(".text", "diretiva", ""),
				new Sugestao(".data", "diretiva", ""),
				new Sugestao(".rodata", "diretiva", ""),
				new Sugestao(".bss", "diretiva", ""),
				new Sugestao(".global", "diretiva", "símbolo"),
				new Sugestao(".section", "diretiva", "nome"),
				new Sugestao(".align", "diretiva", "valor"),
				new Sugestao(".asciz", "diretiva", "string"),
				new Sugestao(".word", "diretiva", "valor"),
				new Sugestao(".byte", "diretiva", "valor"),
				new Sugestao(".space", "diretiva", "tamanho"),
				new Sugestao("adr", "instrucao", "registrador, rótulo"),
				new Sugestao("adrp", "instrucao", "registrador, rótulo"),
				new Sugestao("nop", "instrucao", ""),
				new Sugestao("svc", "instrucao", "valor")
			};
		}    
        return new Sugestao[0];    
    }
}
