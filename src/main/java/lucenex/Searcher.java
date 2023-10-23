package lucenex;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class Searcher {

    public static void main(String[] args) {
        String indexPath = "/Users/alessandropesare/opt/lucene-index"; // Percorso dell'indice

        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("Inserisci una query semplice o una serie di parole chiave separate da spazi, oppure 'exit' per uscire: ");
                String userInput = br.readLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }

                // Split della stringa dell'utente in parole chiave
                String[] keywords = userInput.split(" ");

                // Creazione di una query booleana per cercare tutte le parole chiave nei campi desiderati
                BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
                for (String keyword : keywords) {
                    Query termQueryTitolo = new TermQuery(new Term("titolo", keyword));
                    Query termQueryContenuto = new TermQuery(new Term("contenuto", keyword));
                    booleanQueryBuilder.add(termQueryTitolo, BooleanClause.Occur.SHOULD); // "SHOULD" indica l'OR logico
                    booleanQueryBuilder.add(termQueryContenuto, BooleanClause.Occur.SHOULD);
                }
                Query userQuery = booleanQueryBuilder.build();

                System.out.println("Esecuzione di query: " + userQuery.toString());

                // Esegui la query
                TopDocs hits = searcher.search(userQuery, 3);
                if (hits.scoreDocs.length == 0) {
                    System.out.println("Nessun documento trovato");
                }
                for (int j = 0; j < hits.scoreDocs.length; j++) {
                    ScoreDoc scoreDoc = hits.scoreDocs[j];
                    Document doc = searcher.doc(scoreDoc.doc);
                    System.out.println("doc" + scoreDoc.doc + ":" + doc.get("titolo") + " (" + scoreDoc.score + ")");
                }
            }



            System.out.println("Query di test statiche:");
            // Array di query di test
            Query[] queries = {
                    new TermQuery(new Term("titolo", "automazione")),
                    new TermQuery(new Term("titolo", "L'impatto")),
                    new PhraseQuery.Builder().add(new Term("titolo","L'impatto")).add(new Term("titolo","dell'AI")).build(),
                    new TermQuery(new Term("contenuto", "ia")),
                    new TermQuery(new Term("contenuto", "siri")),
                    new PhraseQuery.Builder().add(new Term("contenuto", "automazione")).add(new Term("contenuto","industriale")).build()
            };

            for (int i = 0; i < queries.length; i++) {
                Query currentQuery = queries[i];
                System.out.println("Esecuzione di query " + i + ": " + currentQuery.toString());

                // Esegui la query
                TopDocs hits = searcher.search(currentQuery, 3);

                for (int j=0; j<hits.scoreDocs.length;j++) {
                   ScoreDoc scoreDoc = hits.scoreDocs[j];
                    Document doc = searcher.doc(scoreDoc.doc);
                    System.out.println("doc"+scoreDoc.doc+":"+doc.get("titolo")+" ("+ scoreDoc.score + ")");
                }
            }

            // Chiudi il reader
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
