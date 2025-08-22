package com.assemblyide;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.ScrollView;
import android.text.Editable;
import android.text.TextWatcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.ArrayList;
import java.net.URL;
import java.io.InputStream;

public class TerminalActivity extends Activity { 
    public EditText entrada;
    public TextView saida;
    public File dirTrabalho;
    public File dirPs;
    public List<String> bins;
    public Logs logs;
    public static String comandoPadrao;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.terminal);

        bins = new ArrayList<>();
        dirPs = new File(getFilesDir().getAbsolutePath()+"/pacotes");
        if(!dirPs.isDirectory()) dirPs.mkdirs();
        bins.add(dirPs.getAbsolutePath()+"/include");
        bins.add(dirPs.getAbsolutePath()+"/bin");
        bins.add(dirPs.getAbsolutePath()+"/libs");
        dirTrabalho = new File(getFilesDir().getAbsolutePath()+"/CASA");
        if(!dirTrabalho.isDirectory()) dirTrabalho.mkdirs();

        System.out.println("> comandos:");
        System.out.println("> instalar <pacote/caminho>");
        System.out.println("> limp");
        System.out.println(">");
        System.out.println("> pacotes disponiveis");
        System.out.println("> node");
        System.out.println("> asm");

        entrada = findViewById(R.id.entrada);
        saida = findViewById(R.id.saida);
        logs = new Logs(saida);
        if(comandoPadrao != null && !comandoPadrao.equals("")) executar(comandoPadrao);
        entrada.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int i, int c, int d) {}
                public void onTextChanged(CharSequence s, int i, int a, int c) {}
                public void afterTextChanged(Editable s) {
                    if(s.toString().contains("\n")) {
                        String[] comandos = entrada.getText().toString().trim().split("\n");
                        s.clear();
                        for(int i = comandos.length-1; i >= 0; i--) {
                            if(comandos[i].length() == 0) return;
                            System.out.println("> " + comandos[i]);
                            executar(comandos[i]);
                        }
                    }
                }
            });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    public void executar(final String comandoStr) {
        new Thread(new Runnable() {
                public void run() {
                    if(comandoStr.startsWith("cd ")) {
                        executarCd(comandoStr.substring(3).trim());
                        return;
                    } else if(comandoStr.startsWith("limp")) {
                        saida.setText("");
                        return;
                    } else if(comandoStr.startsWith("instalar node")) {  
                        instalarWeb("https://github.com/Shiniga-OP/Terminal-simples-android/releases/download/NodeJS-v22.17.1-aarch64/node.zip");  
                        return;
                    } else if(comandoStr.startsWith("instalar asm")) {  
                        instalarWeb("https://github.com/Shiniga-OP/Terminal-simples-android/releases/download/Assembly-aarch64/asm.zip");  
                        return;
                    } else if(comandoStr.startsWith("instalar ")) {
                        instalarPacote(comandoStr.substring(9).trim());
                        return;
                    }
                    executarProcesso(comandoStr);
                }
            }).start();
    }

    public void instalarWeb(final String url) {
        File tmp = new File(getFilesDir().getAbsolutePath()+"/tmp");
        if(!tmp.exists()) tmp.mkdirs();
        final File zip = new File(tmp, "pacote.zip");
        try {
            URL u = new URL(url);
            InputStream en = u.openStream();
            FileOutputStream s = new FileOutputStream(zip);
            byte[] buffer = new byte[8192];
            int l;
            while((l = en.read(buffer)) > 0) s.write(buffer, 0, l);
            s.close();
            en.close();
            instalarPacote(zip.getAbsolutePath());
        } catch(Exception e) {
            erro("Falha no download: " + e.getMessage());
        }
    }

    public void executarCd(String novoDir) {
        if(novoDir.trim().equals("")) {
            dirTrabalho = new File(getFilesDir().getAbsolutePath()+"/CASA");
            return;
        }
        File novo;
        if(novoDir.isEmpty()) {
            novo = new File(getFilesDir().getAbsolutePath()+"/CASA");
            if(!novo.isDirectory()) novo.mkdir();
        } else if(novoDir.startsWith("/")) novo = new File(novoDir);
        else novo = new File(dirTrabalho, novoDir);

        if(!novo.isDirectory() || !novo.canRead()) {
            erro("cd: " + novoDir + ": Diretório não encontrado ou sem permissão");
            return;
        }
        dirTrabalho = novo;
        runOnUiThread(new Runnable() {
                public void run() {
                    System.out.println("Diretório atual: " + dirTrabalho.getAbsolutePath() + "\n");
                }
            });
    }

    public void executarProcesso(String comando) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            Map<String, String> cams = pb.environment();

            String camAtual = cams.get("PATH");
            if(bins != null) {
                for(String bin : bins) {
                    if(bin == null) break;
                    else camAtual = bin + ":" + camAtual;
                }
            }
            cams.put("PATH", camAtual);

            String biblisCam = dirPs.getAbsolutePath() + "/libs";
            String ldAtual = cams.get("LD_LIBRARY_PATH");
            if(ldAtual == null) ldAtual = "";
            cams.put("LD_LIBRARY_PATH", biblisCam + ":" + ldAtual);
            String etcCam = dirPs.getAbsolutePath() + "/etc";
            String etcAtual = cams.get("LD_LIBRARY_PATH");
            if(etcAtual == null) etcAtual = "";
            cams.put("OPENSSL_CONF", etcCam + ":" +etcAtual);

            pb.command("/system/bin/sh", "-c", comando);
            pb.directory(dirTrabalho);
            pb.redirectErrorStream(true);

            final Process p = pb.start();
            BufferedReader leitor = new BufferedReader(new InputStreamReader(p.getInputStream()));

            StringBuilder resultado = new StringBuilder();
            String linha;
            while((linha = leitor.readLine()) != null) resultado.append(linha).append("\n");

            final String s = resultado.toString();
            final int codigoSaida = p.waitFor();

            runOnUiThread(new Runnable() {
                    public void run() {
                        System.out.println(s);
                        System.out.println("saida: " + codigoSaida + "\n");
                    }
                });
        } catch(final Exception e) {
            erro(e.getMessage());
        }
    }

    public void erro(final String msg) {
        runOnUiThread(new Runnable() {
                public void run() {
                    System.out.println("erro: " + (msg != null ? msg : "Erro desconhecido") + "\n");
                }
            });
    }

    public void instalarPacote(String cam) {
        final File zipArq = new File(cam);
        if(!zipArq.exists()) return;
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipArq));
            ZipEntry entradas;
            File destDir = new File(dirPs.getAbsolutePath());
            if(!destDir.exists() && !destDir.mkdirs()) {
                erro("Falha ao criar diretório pacotes");
                return;
            }
            byte[] buffer = new byte[8192];
            while((entradas = zis.getNextEntry()) != null) {
                if(entradas.isDirectory()) continue;
                File saidaArq = new File(destDir, entradas.getName());
                File parente = saidaArq.getParentFile();
                if(!parente.exists() && !parente.mkdirs()) {
                    erro("Falha ao criar diretório " + parente.getAbsolutePath());
                    zis.closeEntry();
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(saidaArq);
                int tam;
                while((tam = zis.read(buffer)) > 0) fos.write(buffer, 0, tam);
                fos.getFD().sync();
                fos.close();

                if(!saidaArq.setExecutable(true, false)) {
                    Process chmod = Runtime.getRuntime().exec(new String[]{ "chmod", "+x", saidaArq.getAbsolutePath() });
                    chmod.waitFor();
                    if(!saidaArq.canExecute()) erro("Não foi possível definir permissão de execução: " + saidaArq.getName());
                }
                zis.closeEntry();
            }
            zis.close();
            executar("find " + dirPs.getAbsolutePath() + "/bin -type f -exec chmod +x {} \\;");
            runOnUiThread(new Runnable() {
                    public void run() {
                        System.out.println("[PACOTE INSTALADO]");
                    }
                });
            if(cam.startsWith(getFilesDir().getAbsolutePath()+"/tmp/")) zipArq.delete();
        } catch(Exception e) {
            erro("Erro ao instalar binários: " + e.getMessage());
        }
    }
}
