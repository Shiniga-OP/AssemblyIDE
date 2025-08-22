package com.editor;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.text.Spanned;
import android.graphics.Typeface;

public class Sintaxe {
    public final HashSet<String> PALAVRAS_NAO_FUNCOES = new HashSet<>();
    public static final long DELAY_MILLIS = 300;

    public void aplicar(final EditText editor) {
        editor.addTextChangedListener(new olhadorSintaxe(editor));
        destacarSintaxe(editor);
		editor.setTypeface(Typeface.MONOSPACE);
		editor.setLineSpacing(7, 1.1f);
    }

    public void destacarSintaxe(EditText editor) {}

    public void destacarComentarios(Spannable span, String txt) {
        Pattern p1 = Pattern.compile("//.*?$", Pattern.MULTILINE);
        Matcher m1 = p1.matcher(txt);
        while(m1.find()) {
            span.setSpan(
                new ForegroundColorSpan(Color.parseColor("#9E9E9E")),
                m1.start(), m1.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        Pattern p2 = Pattern.compile("/\\*(.|\\n)*?\\*/");
        Matcher m2 = p2.matcher(txt);
        while(m2.find()) {
            span.setSpan(
                new ForegroundColorSpan(Color.parseColor("#9E9E9E")),
                m2.start(),
                m2.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

	public void destacarNumeros(Spannable span, String txt, String cor) {
		Pattern p = Pattern.compile("(?<!\\w)(-?\\d+(\\.\\d+)?)(?!\\w)");
		Matcher m = p.matcher(txt);
		while(m.find()) {
			span.setSpan(
				new ForegroundColorSpan(Color.parseColor(cor)),
				m.start(), m.end(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
			);
		}
	}

    public void limparSpans(Editable e) {
        ForegroundColorSpan[] spans = e.getSpans(0, e.length(), ForegroundColorSpan.class);
        for(ForegroundColorSpan span : spans) {
            e.removeSpan(span);
        }
    }

    public void destacarPalavra(Spannable s, String texto, String palavra, String cor) {
        Pattern p = Pattern.compile("\\b" + palavra + "\\b");
        Matcher m = p.matcher(texto);
        while(m.find()) {
            s.setSpan(
                new ForegroundColorSpan(Color.parseColor(cor)),
                m.start(),
                m.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    public void destacarAspas(Spannable s, String texto, String cor) {
        Pattern p = Pattern.compile("\"(?:\\\\\"|[^\"])*?\"");
        Matcher m = p.matcher(texto);
        while(m.find()) {
            s.setSpan(
                new ForegroundColorSpan(Color.parseColor(cor)),
                m.start(),
                m.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

	public void destacarAspasSim(Spannable s, String texto, String cor) {
        Pattern p = Pattern.compile("\'(?:\\\\\'|[^\'])*?\'");
        Matcher m = p.matcher(texto);
        while(m.find()) {
            s.setSpan(
                new ForegroundColorSpan(Color.parseColor(cor)),
                m.start(),
                m.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }
	
	public void destacarAspasEs(Spannable s, String texto, String cor) {
		Pattern p = Pattern.compile("`(?:\\\\`|[^`])*?`");
		Matcher m = p.matcher(texto);
		while(m.find()) {
			s.setSpan(
				new ForegroundColorSpan(Color.parseColor(cor)),
				m.start(),
				m.end(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
			);
		}
	}

    public void destacarFuncoes(Spannable s, String texto, String cor) {
        Pattern p = Pattern.compile("\\b(\\w+)\\s*\\(");
        Matcher m = p.matcher(texto);
        while(m.find()) {
            String nomeFuncao = m.group(1);
            if(!PALAVRAS_NAO_FUNCOES.contains(nomeFuncao)) {
                s.setSpan(
                    new ForegroundColorSpan(Color.parseColor(cor)),
                    m.start(1),
                    m.end(1),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
    }

    public void destacarProximaPalavra(Spannable s, String texto, String palavraChave, String cor) {
        Pattern p = Pattern.compile("\\b" + palavraChave + "\\s+(\\w+)\\b");
        Matcher m = p.matcher(texto);
        while(m.find()) {
            s.setSpan(
                new ForegroundColorSpan(Color.parseColor(cor)),
                m.start(1),
                m.end(1),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    public class olhadorSintaxe implements TextWatcher {
		private final EditText editor;
		private final Runnable tarefaDestaque = new Runnable() {
			@Override public void run() {
				if(editor.getWindowToken() != null) {
					destacarSintaxe(editor);
				}
			}
		};
		private boolean processando = false;
		private int ultimoInicio, ultimoAntes, ultimaQuantidade;

		public olhadorSintaxe(EditText editor) {
			this.editor = editor;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			ultimoInicio = start;
			ultimoAntes = before;
			ultimaQuantidade = count;
		}

		@Override
		public void afterTextChanged(Editable s) {
			if(processando) return;
			processando = true;

			if(ultimoAntes == 0 && ultimaQuantidade == 1) {
				char c = s.charAt(ultimoInicio);
				if(c == '{') {
					if(ultimoInicio + 1 >= s.length() || s.charAt(ultimoInicio + 1) != '}') {
						s.insert(ultimoInicio + 1, "}");
					}
					editor.setSelection(ultimoInicio);
				} else if(c == '\n') {
					int pos = ultimoInicio;
					int lin = pos - 1;
					while(lin >= 0 && s.charAt(lin) != '\n') lin--;
					int inicioLinha = lin + 1;
					int contadorTabs = 0;
					while(inicioLinha + contadorTabs < s.length()
						   && s.charAt(inicioLinha + contadorTabs) == '\t') {
						contadorTabs++;
					}
					StringBuilder sb = new StringBuilder();
					for(int i = 0; i < contadorTabs; i++) sb.append('\t');
					int fim = pos - 1;
					while(fim >= 0 && (s.charAt(fim) == '\t' || s.charAt(fim) == ' ')) fim--;
					boolean abre = fim >= 0 && (s.charAt(fim) == '{' || s.charAt(fim) == ':');
					if(abre) sb.append('\t');
					String indent = sb.toString();
					s.insert(pos + 1, indent);
					editor.setSelection(pos + 1 + indent.length());
				}
			}
			editor.removeCallbacks(tarefaDestaque);
			editor.postDelayed(tarefaDestaque, Sintaxe.DELAY_MILLIS);
			processando = false;
		}
	}
	
	public static class FP extends Sintaxe {
		public FP() {
			PALAVRAS_NAO_FUNCOES.add("log");
			PALAVRAS_NAO_FUNCOES.add("se");
			PALAVRAS_NAO_FUNCOES.add("por");
			PALAVRAS_NAO_FUNCOES.add("enq");
		}

		@Override
		public void destacarSintaxe(EditText editor) {
			Editable e = editor.getText();
			if(e == null) return;

			int selecaoComeco = editor.getSelectionStart();
			int selecaoFinal = editor.getSelectionEnd();

			limparSpans(e);

			String texto = e.toString();

			// azul escuro
			destacarPalavra(e, texto, "func", "#3F51B5");
			destacarPalavra(e, texto, "incluir", "#3F51B5");
			destacarPalavra(e, texto, "classe", "#64B5F6");
			// verde claro
			destacarPalavra(e, texto, "log", "#98FB98");
			destacarPalavra(e, texto, "este", "#98FB98");
			// rosa claro
			destacarPalavra(e, texto, "se", "#FF69B4");
			destacarPalavra(e, texto, "senao", "#FF69B4");
			destacarPalavra(e, texto, "retorne", "#FF69B4");
			destacarPalavra(e, texto, "por", "#FF69B4");
			destacarPalavra(e, texto, "enq", "#FF69B4");
			destacarPalavra(e, texto, "novo", "#FF69B4");
			// rosa forte
			destacarNumeros(e, texto, "#FF1493");
			// azul claro
			destacarPalavra(e, texto, "var", "#64B5F6");
			destacarPalavra(e, texto, "Tex", "#64B5F6");
			destacarPalavra(e, texto, "Dobro", "#64B5F6");
			destacarPalavra(e, texto, "Bool", "#64B5F6");
			destacarPalavra(e, texto, "Flutu", "#64B5F6");
			destacarPalavra(e, texto, "Int", "#64B5F6");
			// bege
			destacarFuncoes(e, texto, "#F4A460");
			// cinza
			destacarPalavra(e, texto, "#incluir", "#9E9E9E");
			destacarProximaPalavra(e, texto, "#incluir", "#9E9E9E");
			destacarComentarios(e, texto);
			// verde
			destacarProximaPalavra(e, texto, "classe", "#66BB6A");
			destacarProximaPalavra(e, texto, "novo", "#66BB6A");
			destacarAspas(e, texto, "#66BB6A");
			destacarAspasSim(e, texto, "#66BB6A");

			if(selecaoComeco >= 0 && selecaoFinal >= 0 && selecaoComeco <= e.length() && selecaoFinal <= e.length()) {
				editor.setSelection(selecaoComeco, selecaoFinal);
			}
		}
	}
	
	public static class Java extends Sintaxe {
		public Java() {
			PALAVRAS_NAO_FUNCOES.add("while");
			PALAVRAS_NAO_FUNCOES.add("if");
			PALAVRAS_NAO_FUNCOES.add("for");
			PALAVRAS_NAO_FUNCOES.add("switch");
		}

		@Override
		public void destacarSintaxe(EditText editor) {
			Editable e = editor.getText();
			if(e == null) return;

			int selecaoComeco = editor.getSelectionStart();
			int selecaoFinal = editor.getSelectionEnd();

			limparSpans(e);

			String texto = e.toString();

			// azul escuro
			destacarPalavra(e, texto, "public", "#3F51B5");
			destacarPalavra(e, texto, "private", "#3F51B5");
			destacarPalavra(e, texto, "static", "#3F51B5");
			destacarPalavra(e, texto, "protected", "#3F51B5");
			destacarPalavra(e, texto, "package", "#3F51B5");
			destacarPalavra(e, texto, "import", "#3F51B5");
			destacarPalavra(e, texto, "class", "#3F51B5");
			// rosa claro
			destacarPalavra(e, texto, "if", "#FF69B4");
			destacarPalavra(e, texto, "else", "#FF69B4");
			destacarPalavra(e, texto, "for", "#FF69B4");
			destacarPalavra(e, texto, "while", "#FF69B4");
			destacarPalavra(e, texto, "switch", "#FF69B4");
			destacarPalavra(e, texto, "return", "#FF69B4");
			destacarPalavra(e, texto, "new", "#FF69B4");
			// rosa forte
			destacarNumeros(e, texto, "#FF1493");
			// bege
			destacarFuncoes(e, texto, "#F4A460");
			// azul claro
			destacarPalavra(e, texto, "void", "#64B5F6");
			destacarPalavra(e, texto, "String", "#64B5F6");
			destacarPalavra(e, texto, "double", "#64B5F6");
			destacarPalavra(e, texto, "boolean", "#64B5F6");
			destacarPalavra(e, texto, "float", "#64B5F6");
			destacarPalavra(e, texto, "int", "#64B5F6");
			destacarPalavra(e, texto, "short", "#64B5F6");
			destacarPalavra(e, texto, "long", "#64B5F6");
			destacarPalavra(e, texto, "byte", "#64B5F6");
			destacarPalavra(e, texto, "char", "#64B5F6");
			// cinza
			destacarComentarios(e, texto);
			// verde
			destacarProximaPalavra(e, texto, "class", "#66BB6A"); 
			destacarProximaPalavra(e, texto, "new", "#66BB6A");
			destacarAspas(e, texto, "#66BB6A");
			destacarAspasSim(e, texto, "#66BB6A");

			if(selecaoComeco >= 0 && selecaoFinal >= 0 && selecaoComeco <= e.length() && selecaoFinal <= e.length()) {
				editor.setSelection(selecaoComeco, selecaoFinal);
			}
		}
	}
	
	public static class JS extends Sintaxe {
		public JS() {
			PALAVRAS_NAO_FUNCOES.add("for");
			PALAVRAS_NAO_FUNCOES.add("if");
			PALAVRAS_NAO_FUNCOES.add("switch");
			PALAVRAS_NAO_FUNCOES.add("while");
		}

		@Override
		public void destacarSintaxe(EditText editor) {
			Editable e = editor.getText();
			if(e == null) return;

			int selecaoComeco = editor.getSelectionStart();
			int selecaoFinal = editor.getSelectionEnd();

			limparSpans(e);

			String texto = e.toString();

			// azul escuro
			destacarPalavra(e, texto, "function", "#3F51B5");
			destacarPalavra(e, texto, "class", "#64B5F6");
			// verde claro
			destacarPalavra(e, texto, "console", "#98FB98");
			destacarPalavra(e, texto, "document", "#98FB98");
			destacarPalavra(e, texto, "window", "#98FB98");
			// rosa claro
			destacarPalavra(e, texto, "import", "#FF69B4");
			destacarPalavra(e, texto, "if", "#FF69B4");
			destacarPalavra(e, texto, "for", "#FF69B4");
			destacarPalavra(e, texto, "of", "#FF69B4");
			destacarPalavra(e, texto, "in", "#FF69B4");
			destacarPalavra(e, texto, "while", "#FF69B4");
			destacarPalavra(e, texto, "switch", "#FF69B4");
			destacarPalavra(e, texto, "return", "#FF69B4");
			destacarPalavra(e, texto, "break", "#FF69B4");
			destacarPalavra(e, texto, "case", "#FF69B4");
			destacarPalavra(e, texto, "new", "#FF69B4");
			// rosa forte
			destacarNumeros(e, texto, "#FF1493");
			// azul claro
			destacarPalavra(e, texto, "let", "#64B5F6");
			destacarPalavra(e, texto, "const", "#64B5F6");
			destacarPalavra(e, texto, "var", "#64B5F6");
			// bege
			destacarFuncoes(e, texto, "#F4A460");
			// cinza
			destacarComentarios(e, texto);
			// verde
			destacarProximaPalavra(e, texto, "class", "#66BB6A");
			destacarProximaPalavra(e, texto, "new", "#66BB6A");
			destacarAspas(e, texto, "#66BB6A");
			destacarAspasSim(e, texto, "#66BB6A");
			destacarAspasEs(e, texto, "#66BB6A");

			if(selecaoComeco >= 0 && selecaoFinal >= 0 && selecaoComeco <= e.length() && selecaoFinal <= e.length()) {
				editor.setSelection(selecaoComeco, selecaoFinal);
			}
		}
	}
    
    public static class ASMArm64 extends Sintaxe {
        @Override
        public void destacarSintaxe(EditText editor) {
            Editable e = editor.getText();
            if(e == null) return;

            int selecaoComeco = editor.getSelectionStart();
            int selecaoFinal = editor.getSelectionEnd();

            limparSpans(e);

            String texto = e.toString();

            // azul escuro
            destacarPalavra(e, texto, "text", "#3F51B5");
            destacarPalavra(e, texto, "data", "#3F51B5");
            destacarPalavra(e, texto, "rodata", "#3F51B5");
            destacarPalavra(e, texto, "asciz", "#3F51B5");
            destacarPalavra(e, texto, "byte", "#3F51B5");
            destacarPalavra(e, texto, "bss", "#3F51B5");
            destacarPalavra(e, texto, "word", "#3F51B5");
            destacarPalavra(e, texto, "align", "#3F51B5");
            destacarPalavra(e, texto, "include", "#3F51B5");
            // verde claro
            destacarPalavra(e, texto, "svc", "#98FB98");
            // rosa claro
            destacarPalavra(e, texto, "ret", "#FF69B4");
            // vermelho
            destacarPalavra(e, texto, "global", "#FF0000");
            destacarPalavra(e, texto, "section", "#FF0000");
            // rosa forte
            destacarNumeros(e, texto, "#FF1493");
            destacarPalavra(e, texto, "x0", "#FF1493");
            destacarPalavra(e, texto, "x1", "#FF1493");
            destacarPalavra(e, texto, "x2", "#FF1493");
            destacarPalavra(e, texto, "x3", "#FF1493");
            destacarPalavra(e, texto, "x4", "#FF1493");
            destacarPalavra(e, texto, "x5", "#FF1493");
            destacarPalavra(e, texto, "x6", "#FF1493");
            destacarPalavra(e, texto, "x7", "#FF1493");
            destacarPalavra(e, texto, "x8", "#FF1493");
            destacarPalavra(e, texto, "x9", "#FF1493");
            destacarPalavra(e, texto, "x10", "#FF1493");
            destacarPalavra(e, texto, "x11", "#FF1493");
            destacarPalavra(e, texto, "x12", "#FF1493");
            // azul claro
            destacarPalavra(e, texto, "mov", "#64B5F6");
            destacarPalavra(e, texto, "ldr", "#64B5F6");
            destacarPalavra(e, texto, "cmp", "#64B5F6");
            destacarPalavra(e, texto, "cmn", "#64B5F6");
            destacarPalavra(e, texto, "sub", "#64B5F6");
            destacarPalavra(e, texto, "add", "#64B5F6");
            destacarPalavra(e, texto, "mul", "#64B5F6");
            destacarPalavra(e, texto, "udiv", "#64B5F6");
            destacarPalavra(e, texto, "sdiv", "#64B5F6");
            destacarPalavra(e, texto, "str", "#64B5F6");
            destacarPalavra(e, texto, "b", "#64B5F6");
            destacarPalavra(e, texto, "bl", "#64B5F6");
            destacarPalavra(e, texto, "str", "#64B5F6");
            // bege
            // destacarPalavra(e, texto, "_", "#F4A460");
            // cinza
            destacarComentarios(e, texto);
            // verde
            destacarAspas(e, texto, "#66BB6A");
            destacarAspasSim(e, texto, "#66BB6A");
            destacarAspasEs(e, texto, "#66BB6A");

            if(selecaoComeco >= 0 && selecaoFinal >= 0 && selecaoComeco <= e.length() && selecaoFinal <= e.length()) {
                editor.setSelection(selecaoComeco, selecaoFinal);
            }
        }
	}
}
