## sobre:
é um mini ambiente de desenvolvimento integrado ao Android.

especificamente para arquitetura ARM64 Linux (Android), AssemblyIDE contém

1. destaque de sintaxe.
2. sugestões dinamicas (via popop).
3. autocompletar (via Enter).
4. sistema de indentação por símbolos específicos "{", ":".
5. **Terminal Simples** integrado para uso de de as (assembler) e ld (linker).
6. botão para compilar, linkar, e executar o arquivo com apenas 1 clique.
7. vindo juntamente com uma interface para navegação entre arquivos (beta).

## instruções do gerenciador de arquivos:
caso você queira renomear ou excluir um arquivo especifico, apenas pressione o arquivo desejado, e as opções aparecerão.

## aviso:
em caso de trabalho com o armazenamento externo, é necessario dar permissão de acesso total manualmente para melhor experiencia.

## propósito:
na verdade eu só fiz isso porque queria fazer mais coisa com Assembly, e programar no Termux é intankavel.

## como fazer pacotes
caso você precise de um pacote específico ou uma versão dos binários para outra arquitetura, aqui está um pequeno tutorial de como fazer:

você precisa de um zip exatamente com essa estrutura:
```
pacote.zip:
  bin:
    as
    ld
  libs:
    libsframe.so.1
    libz.so.1
    libctf.so.0
    libzstd.so.1
    libbfd-2.44.so
    libopcodes-2.44.so
```
## onde conseguir os binários certos:
no caso do assembler e do linker, você pode pegar os binários diretamente do Termux na pasta */data/data/com.termux/files/usr/bin/*, lá estará o binário compilado para sua arquitetura corretamente.

e para as libs */data/data/com.termux/files/usr/lib/*.

## instalação
utilize

instalar /caminho/para/o/pacote.zip

e pronto, você poderá usar o assembler e o linker.
