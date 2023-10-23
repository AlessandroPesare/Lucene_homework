package lucenex;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
Classe responsabile di gestire la logica dell'indicizzazione
 */
public class TextIndexer {

    public static void main(String[] args) {
        String indexPath = "/Users/alessandropesare/opt/lucene-index"; // Percorso in cui verrà salvato l'indice
        String docsPath = "/Users/alessandropesare/Documenti"; // Percorso della directory contenente i file .txt da indicizzare
        try {
            // Configura l'indice
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            // Creo un analyzer personalizzato
            Analyzer analyzer = createAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            //config.setCodec(new simpleTextCodec());
            IndexWriter writer = new IndexWriter(directory, config);

            // Indicizza i file .txt nella directory specificata
            System.out.println("Inizio lettura da file");
            long startTime = System.currentTimeMillis();
            indexTextFiles(writer, Paths.get(docsPath));
            writer.close();
            long endTime = System.currentTimeMillis();
            long time = endTime - startTime;

            System.out.println("Indicizzazione completata con successo. L'indice è stato salvato in " + indexPath);
            System.out.println("Il tempo trascorso per l'indicizzazione dei file è:"+ time + "millisecondi");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    indexTextFiles è responsabile dell'indicizzazione dei file e riceve come parametri l'indexwriter che si occupa
    dell'indicizzazione dei file e il percorso dove si trovano i file.txt
     */
    private static void indexTextFiles(IndexWriter writer, Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            // vedo solo i file.txt nella cartella specificata dal path
            File[] files = dir.toFile().listFiles((pathname) -> pathname.getName().endsWith(".txt"));
            if (files != null) {

                for (File file : files) {
                    System.out.println(file.getName());
                    // Crea un nuovo documento
                    Document doc = new Document();

                    /* Aggiungo il nome del file come campo (supponiamo che i file abbiano tutti un nome
                      abbastanza autoesplicativo)
                     */
                    doc.add(new TextField("titolo",file.getName(),Field.Store.YES));

                    // Leggo il contenuto del file e lo aggiungo come campo "contenuto"
                    String content = readTextFile(file);
                    doc.add(new TextField("contenuto",content,Field.Store.YES));
                    // Aggiungi il documento all'indice
                    writer.addDocument(doc);
                }
                writer.commit();
            }

        }
    }

    private static String readTextFile(File file) throws IOException {
        /* Leggiamo il contenuto del file .txt e lo restituiamo come una stringa, utilizzo File Reader
         ottimizziamo con un bufferReader la lettura del file minimizzando le operazioni di IO con una
         lettura bufferizzata*/

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            System.out.println(text.toString());
            return text.toString();
        }
    }

    private static Analyzer createAnalyzer() {
        //stop words
        CharArraySet contenutoStopWords = new CharArraySet(Arrays.asList("in","dei","di","il","la","lo","gli","dell'"), true);
        Analyzer titoloAnalyzer = new WhitespaceAnalyzer();
        Analyzer contenutoAnalyzer = new StandardAnalyzer(contenutoStopWords);
        //per eventuali nuovi campi
        Analyzer defaultAnalyzer = new ItalianAnalyzer();

        // Create a map to associate analyzers with fields
        Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();
        perFieldAnalyzers.put("titolo", titoloAnalyzer);
        perFieldAnalyzers.put("contenuto", contenutoAnalyzer);
        //System.out.println("Campo 'contenuto' analizzato come: " + contenutoAnalyzer.tokenStream("contenuto", new StringReader("il tuo testo qui")));

        // Create a PerFieldAnalyzerWrapper with a default analyzer
        return new PerFieldAnalyzerWrapper(defaultAnalyzer, perFieldAnalyzers);
    }
}
