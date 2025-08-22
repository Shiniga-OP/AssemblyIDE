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
     public Button criarArquivo;
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
        criarArquivo = findViewById(R.id.criarArquivo);
        
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

        criarArquivo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    escreverArq(dirTrabalho.getAbsolutePath()+"/"+nomeArquivo.getText().toString(), editor.getText().toString());
                    Toast.makeText(getApplicationContext(), "arquivo criado", Toast.LENGTH_SHORT).show();
                    _capturar_pasta();
                }
            });

        salvar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(!arqExiste(dirTrabalho.getAbsolutePath()+"/"+nomeArquivo.getText().toString())) {
                            escreverArq(dirTrabalho.getAbsolutePath()+nomeArquivo.getText().toString(), editor.getText().toString());
                            Toast.makeText(getApplicationContext(), "arquivo salvo", Toast.LENGTH_SHORT).show();
                            _capturar_pasta();
                        } else {
                            escreverArq(dirTrabalho.getAbsolutePath()+nomeArquivo.getText().toString(), editor.getText().toString());
                            Toast.makeText(getApplicationContext(), "arquivo criado", Toast.LENGTH_SHORT).show();
                            _capturar_pasta();
                        }
                    } catch(Exception e) {
                       System.out.println("erro: "+e+"\n"+dirTrabalho.getAbsolutePath()+nomeArquivo.getText().toString());
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
            while((linha = br.readLine()) != null) {
                sb.append(linha).append("\n");
            }
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

        List<Map<String, Object>> _data;

        public PastasAdapter(List<Map<String, Object>> _arr) {
            _data = _arr;
        }

        @Override
        public int getCount() {
            return _data.size();
        }

        @Override
        public Map<String, Object> getItem(int _index) {
            return _data.get(_index);
        }

        @Override
        public long getItemId(int _index) {
            return _index;
        }

        @Override
        public View getView(final int _position, View _v, ViewGroup _container) {
            LayoutInflater _inflater = getLayoutInflater();
            View _view = _v;
            if(_view == null) {
                _view = _inflater.inflate(R.layout.diretorios, null);
            }

            final ImageView imageview1 = _view.findViewById(R.id.iconeArq);
            final TextView textview1 = _view.findViewById(R.id.texArq);

            textview1.setText(Uri.parse(_data.get(_position).get(dirTrabalho.getAbsolutePath()).toString()).getLastPathSegment());
            if(Uri.parse(_data.get(_position).get(dirTrabalho.getAbsolutePath()).toString()).getLastPathSegment().endsWith(".fp")) {
                imageview1.setImageResource(R.drawable.imagem);
            }
            else {
                if(Uri.parse(_data.get(_position).get(dirTrabalho.getAbsolutePath()).toString()).getLastPathSegment().endsWith(".imagem")) {
                    imageview1.setImageResource(R.drawable.imagem);
                }
                else {
                    imageview1.setImageResource(R.drawable.pasta);
                }
            }
            return _view;
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
