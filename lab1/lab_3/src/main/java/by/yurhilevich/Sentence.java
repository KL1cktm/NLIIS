package by.yurhilevich;

import java.util.List;

public class Sentence implements Comparable<Sentence> {

    private final String originalText;
    private final int originalIndex;
    private final int paragraphIndex;
    private final int docCharStart;
    private final int parCharStart;
    private final List<String> stems;
    private double weight;

    private final int totalParChars;

    public Sentence(String originalText, int originalIndex, int paragraphIndex,
                    int docCharStart, int parCharStart, List<String> stems,
                    int totalParChars) {
        this.originalText = originalText;
        this.originalIndex = originalIndex;
        this.paragraphIndex = paragraphIndex;
        this.docCharStart = docCharStart;
        this.parCharStart = parCharStart;
        this.stems = stems;
        this.totalParChars = totalParChars;
        this.weight = 0.0;
    }

    public String getOriginalText() { return originalText; }
    public int getOriginalIndex() { return originalIndex; }
    public List<String> getStems() { return stems; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getFposD(int totalDocChars) {
        if (totalDocChars == 0) return 0;
        // Формула: 1 - (B_D(Si) / |D|)
        return 1.0 - ((double) docCharStart / totalDocChars);
    }

    public double getFposP() {
        if (totalParChars == 0) return 0;
        // Формула: 1 - (B_P(Si) / |P|)
        // B_P(Si) - это 'parCharStart'
        // |P| - это 'totalParChars'
        return 1.0 - ((double) parCharStart / totalParChars);
    }

    @Override
    public int compareTo(Sentence o) {
        return Double.compare(o.weight, this.weight);
    }
}