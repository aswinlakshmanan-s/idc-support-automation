import java.util.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            List<String> filePaths = Arrays.asList(
                    "src/resources/book1.txt",
                    "src/resources/book2.txt",
                    "src/resources/book3.txt"
            );

            Tokenizer tokenizer = new Tokenizer();
            tokenizer.buildVocabulary(filePaths);

            Encoder encoder = new Encoder(tokenizer);
            Decoder decoder = new Decoder(tokenizer);
            Scanner scanner = new Scanner(System.in);

            // Encoding user input
            System.out.print("Enter a sentence to encode: ");
            String input = scanner.nextLine();
            List<Integer> encoded = encoder.encode(input);
            System.out.println("Encoded token IDs: " + encoded);

            // Decoding token IDs
            System.out.print("Enter encoded token IDs (comma-separated): ");
            String encodedInput = scanner.nextLine();
            List<Integer> tokenIds = new ArrayList<>();
            for (String s : encodedInput.split(",")) {
                tokenIds.add(Integer.parseInt(s.trim()));
            }

            String decoded = decoder.decode(tokenIds);
            System.out.println("Decoded text: " + decoded);

        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
