import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Tokenizer {
    private final Map<String, Integer> vocab = new HashMap<>();
    private final Map<Integer, String> idToWord = new HashMap<>();
    private static final String UNKNOWN_TOKEN = "<|unk|>";
    private int tokenIdCounter = 0;

    public void buildVocabulary(List<String> filePaths) throws IOException {
        // Add <|unk|> as the first token with ID 0
        vocab.put(UNKNOWN_TOKEN, tokenIdCounter);
        idToWord.put(tokenIdCounter, UNKNOWN_TOKEN);
        tokenIdCounter++;

        Set<String> uniqueWords = new HashSet<>();

        for (String path : filePaths) {
            String content = new String(Files.readAllBytes(Paths.get(path))).toLowerCase();
            String[] tokens = content.split("\\W+");  // Split by non-word characters
            uniqueWords.addAll(Arrays.asList(tokens));
        }

        for (String word : uniqueWords) {
            if (!vocab.containsKey(word)) {
                vocab.put(word, tokenIdCounter);
                idToWord.put(tokenIdCounter, word);
                tokenIdCounter++;
            }
        }
    }

    public List<Integer> encode(String text) {
        List<Integer> result = new ArrayList<>();
        String[] tokens = text.toLowerCase().split("\\W+");
        for (String token : tokens) {
            result.add(vocab.getOrDefault(token, vocab.get(UNKNOWN_TOKEN)));
        }
        return result;
    }

    public String decode(List<Integer> tokenIds) {
        StringBuilder sb = new StringBuilder();
        for (int id : tokenIds) {
            sb.append(idToWord.getOrDefault(id, UNKNOWN_TOKEN)).append(" ");
        }
        return sb.toString().trim();
    }
}
