import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class BPETokenizer {
    private Map<String, Integer> vocab;
    private List<String> merges;
    private static final String WORD_END_TOKEN = "</w>";

    public BPETokenizer() {
        this.vocab = new HashMap<>();
        this.merges = new ArrayList<>();
    }

    private Map<String, Integer> getWordFrequencies(String corpus) {
        Map<String, Integer> wordFreqs = new HashMap<>();
        String[] words = corpus.toLowerCase().replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                String processedWord = String.join(" ", word.split("")) + " " + WORD_END_TOKEN;
                wordFreqs.put(processedWord, wordFreqs.getOrDefault(processedWord, 0) + 1);
            }
        }
        return wordFreqs;
    }

    private void initializeVocabulary(Map<String, Integer> wordFreqs) {
        Set<String> chars = new HashSet<>();
        for (String word : wordFreqs.keySet()) {
            String[] tokens = word.split(" ");
            Collections.addAll(chars, tokens);
        }
        int index = 0;
        for (String ch : chars) {
            vocab.put(ch, index++);
        }
        System.out.println("Initial vocabulary size: " + vocab.size());
    }

    private String findBestPair(Map<String, Integer> wordFreqs) {
        Map<String, Integer> pairFreqs = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wordFreqs.entrySet()) {
            String[] tokens = entry.getKey().split(" ");
            int freq = entry.getValue();
            for (int i = 0; i < tokens.length - 1; i++) {
                String pair = tokens[i] + " " + tokens[i + 1];
                pairFreqs.put(pair, pairFreqs.getOrDefault(pair, 0) + freq);
            }
        }
        return pairFreqs.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    private Map<String, Integer> mergePair(Map<String, Integer> wordFreqs, String pair) {
        Map<String, Integer> newWordFreqs = new HashMap<>();
        String[] pairParts = pair.split(" ");
        for (Map.Entry<String, Integer> entry : wordFreqs.entrySet()) {
            String word = entry.getKey();
            int freq = entry.getValue();
            String newWord = word.replaceAll(
                    Pattern.quote(pairParts[0] + " " + pairParts[1]),
                    pairParts[0] + pairParts[1]
            );
            newWordFreqs.put(newWord, freq);
        }
        return newWordFreqs;
    }

    public void train(String corpus, int numMerges) {
        Map<String, Integer> wordFreqs = getWordFrequencies(corpus);
        initializeVocabulary(wordFreqs);

        for (int i = 0; i < numMerges; i++) {
            String bestPair = findBestPair(wordFreqs);
            if (bestPair == null) {
                System.out.println("No more pairs to merge. Stopping early.");
                break;
            }
            wordFreqs = mergePair(wordFreqs, bestPair);
            merges.add(bestPair);
            vocab.put(bestPair.replace(" ", ""), vocab.size());

            // Print updated corpus
            System.out.println("\n=== Iteration " + (i + 1) + " Corpus ===");
            for (Map.Entry<String, Integer> entry : wordFreqs.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }

            // Print vocabulary
            System.out.println("\n=== Vocabulary after Iteration " + (i + 1) + " ===");
            vocab.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> System.out.println(entry.getValue() + ": " + entry.getKey()));
        }

        System.out.println("\n=== Final Vocabulary Size ===");
        System.out.println(vocab.size());
    }

    public static String downloadGutenbergText(String url) {
        System.out.println("Downloading text from: " + url);
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line;
            boolean startReading = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("*** START OF") || startReading) {
                    startReading = true;
                    if (line.contains("*** END OF")) break;
                    content.append(line).append(" ");
                }
            }
        } catch (IOException e) {
            System.err.println("Error downloading text: " + e.getMessage());
            return "default fallback text used for BPE.";
        }

        System.out.println("Downloaded " + content.length() + " characters");
        return content.toString();
    }

    public static void main(String[] args) {
        BPETokenizer tokenizer = new BPETokenizer();

        // Default to Gutenberg
        String corpus = downloadGutenbergText("https://www.gutenberg.org/cache/epub/76339/pg76339.txt");

        // Run BPE for 84 iterations for visibility
        tokenizer.train(corpus, 84);
    }
}
