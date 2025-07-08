import java.util.List;

public class Encoder {
    private final Tokenizer tokenizer;

    public Encoder(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public List<Integer> encode(String input) {
        return tokenizer.encode(input);
    }
}
