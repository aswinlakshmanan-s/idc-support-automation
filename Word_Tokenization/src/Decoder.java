import java.util.List;

public class Decoder {
    private final Tokenizer tokenizer;

    public Decoder(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public String decode(List<Integer> tokenIds) {
        return tokenizer.decode(tokenIds);
    }
}
