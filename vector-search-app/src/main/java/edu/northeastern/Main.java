package edu.northeastern;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        String ebookUrl = "https://www.gutenberg.org/files/1342/1342-0.txt"; // Pride and Prejudice

        System.out.println("Downloading eBook...");
        List<Document> documents = downloadAndChunk(ebookUrl);

        System.out.println("Generating embeddings...");
        SentenceEmbedder embedder = new SentenceEmbedder();
        for (Document doc : documents) {
            float[] v = embedder.embed(doc.text);
            //Debug print of vector dimensionality:
            System.out.println("DEBUG: vector length = " + v.length);
            doc.vector = v;
        }

        System.out.println("Uploading vectors to Qdrant...");
        QdrantClient qdrant = new QdrantClient();
        qdrant.upload(documents);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\nEnter your search query (or type 'exit'): ");
            String query = scanner.nextLine();
            if (query.equalsIgnoreCase("exit")) break;

            float[] queryVector = embedder.embed(query);
            List<String> results = qdrant.search(queryVector);

            System.out.println("Top Results:");
            for (String res : results) {
                System.out.println(" - " + res + "\n");
            }
        }
    }

    private static List<Document> downloadAndChunk(String ebookUrl) throws Exception {
        List<Document> docs = new ArrayList<>();
        URL url = new URL(ebookUrl);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder paragraph = new StringBuilder();

        int id = 1;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                if (paragraph.length() > 50) {
                    docs.add(new Document(String.valueOf(id++), paragraph.toString().trim()));
                    paragraph.setLength(0);
                }
            } else {
                paragraph.append(line).append(" ");
            }
        }
        if (paragraph.length() > 0) {
            docs.add(new Document(String.valueOf(id), paragraph.toString().trim()));
        }

        return docs.size() > 200 ? docs.subList(0, 200) : docs;
    }
}
