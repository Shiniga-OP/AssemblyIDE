package com.assemblyide;
 
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import com.editor.AutoCompletar;
import com.editor.Sintaxe;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Button;
import java.io.File;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.net.Uri;
import java.util.HashMap;

public class MainActivity extends Activity {
     public EditText editor, nomeArquivo;
     public ListView pastas;
     public String arquivoAtual;
     public ImageView salvar;
     public File dirTrabalho;
     public List<String> projetos = new ArrayList<>();
    public List<Map<String, Object>> projetosLista = new ArrayList<>();
     
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.ide);
        pastas = findViewById(R.id.pastas);
        editor = findViewById(R.id.editor);
        nomeArquivo = findViewById(R.id.nomeArquivo);
        salvar = findViewById(R.id.salvar);
        
        dirTrabalho = new File(getFilesDir()+"/CASA");
        if(!dirTrabalho.exists()) dirTrabalho.mkdir();
        
        new Sintaxe.ASMArm64().aplicar(editor);
        new AutoCompletar(this, editor, AutoCompletar.sintaxe("asm-arm64"));
        
        pastas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
                    editor.setText(lerArq(projetos.get(_param3)));
                    arquivoAtual = projetos.get(_param3);
                    String[] as = arquivoAtual.split("/");
                    nomeArquivo.setText(as[as.length-1]);
                }
            });
        salvar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(nomeArquivo.getText().toString().startsWith("/")) escreverArq(nomeArquivo.getText().toString(), editor.getText().toString());
                        else escreverArq(dirTrabalho.getAbsolutePath()+"/"+nomeArquivo.getText().toString(), editor.getText().toString());
                        Toast.makeText(getApplicationContext(), "arquivo salvo", Toast.LENGTH_SHORT).show();
                        _capturar_pasta();
                    } catch(Exception e) {
                        Toast.makeText(getApplicationContext(), "erro: "+e, Toast.LENGTH_SHORT).show();
                    }
                }
			});
            _capturar_pasta();
    }
    
    public static void listeDir(String cam, List<String> lista) {
        File dir = new File(cam);
        if(!dir.exists() || dir.isFile()) return;

        File[] listeArqs = dir.listFiles();
        if(listeArqs == null || listeArqs.length <= 0) return;

        if(lista == null) return;
        lista.clear();
        for(File arq : listeArqs) {
            lista.add(arq.getAbsolutePath());
        }
    }

    public static boolean arqExiste(String caminho) {
        File arquivo = new File(caminho);
        return arquivo.exists();
    }

    public static void criarDir(String caminho) {
        if(!arqExiste(caminho)) {
            File file = new File(caminho);
            file.mkdirs();
        }
    }

    public static void criarArq(String caminho) {
        int ultimoPasso= caminho.lastIndexOf(File.separator);
        if(ultimoPasso > 0) {
            String caminhoDiretorio = caminho.substring(0, ultimoPasso);
            criarDir(caminhoDiretorio);
        }
        File arquivo = new File(caminho);
        try {
            if(!arquivo.exists()) arquivo.createNewFile();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static String lerArq(String caminho) {
        criarArq(caminho);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(caminho));
            String linha;
            while((linha = br.readLine()) != null) sb.append(linha).append("\n");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void escreverArq(String caminho, String texto) {
        criarArq(caminho);
        FileWriter escritor = null;

        try {
            escritor = new FileWriter(new File(caminho), false);
            escritor.write(texto);
            escritor.flush();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(escritor != null)
                    escritor.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void _capturar_pasta() {
        projetosLista.clear();
        projetos.clear();
        listeDir(dirTrabalho.getAbsolutePath(), projetos);

        for(int i = 0; i < projetos.size(); i++) { {
                Map<String, Object> _item = new HashMap<>();
                _item.put(dirTrabalho.getAbsolutePath(), projetos.get(i));
                projetosLista.add(_item);
            }

            pastas.setAdapter(new PastasAdapter(projetosLista));
            ((BaseAdapter)pastas.getAdapter()).notifyDataSetChanged();
        }
    }

    public class PastasAdapter extends BaseAdapter {

        public List<Map<String, Object>> dados;

        public PastasAdapter(List<Map<String, Object>> arr) {
            dados = arr;
        }

        @Override
        public int getCount() {
            return dados.size();
        }

        @Override
        public Map<String, Object> getItem(int i) {
            return dados.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int posicao, View v, ViewGroup div) {
            LayoutInflater inflator = getLayoutInflater();
            View view = v;
            if(view == null) {
                view = inflator.inflate(R.layout.diretorios, null);
            }

            final ImageView iconeArq = view.findViewById(R.id.iconeArq);
            final TextView texArq = view.findViewById(R.id.texArq);

            texArq.setText(Uri.parse(dados.get(posicao).get(dirTrabalho.getAbsolutePath()).toString()).getLastPathSegment());
            if(dados.get(posicao).get(dirTrabalho.getAbsolutePath()).toString().endsWith(".asm")) {
                iconeArq.setImageResource(R.drawable.asm);
            }
            else {
                if(Uri.parse(dados.get(posicao).get(dirTrabalho.getAbsolutePath()).toString()).getLastPathSegment().endsWith(".imagem")) {
                    iconeArq.setImageResource(R.drawable.imagem);
                }
                else {
                    iconeArq.setImageResource(R.drawable.pasta);
                }
            }
            return view;
        }
	}
    
    public void praTerminal(View v) {
        if(arquivoAtual != null && !arquivoAtual.equals("")) {
            String nomeArquivo = new File(arquivoAtual).getName();
            TerminalActivity.comandoPadrao = 
                "as " + nomeArquivo + " -o " + nomeArquivo.replace(".asm",".o") + "\n" +
                "ld " + nomeArquivo.replace(".asm",".o") + " -o " + nomeArquivo.replace(".asm","") + "\n" +
                "./" + nomeArquivo.replace(".asm","");
        } else TerminalActivity.comandoPadrao = null;
        Intent t = new Intent(this, TerminalActivity.class);
        startActivity(t);
    }
}
