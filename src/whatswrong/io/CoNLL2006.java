package whatswrong.io;

import whatswrong.TokenProperty;
import whatswrong.NLPInstance;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Sebastian Riedel
 */

public class CoNLL2006 implements CoNLLProcessor {

    public static final String name = "2006";

    private TokenProperty
            ner1 = new TokenProperty("ner1", 10),
            ner2 = new TokenProperty("ner2", 11);

    public String toString() {
        return name;
    }

    public NLPInstance create(List<? extends List<String>> rows) {

        NLPInstance instance = new NLPInstance();
        instance.addToken().addProperty("Word", "-Root-");
        for (List<String> row : rows) {
            instance.addToken().
                    addProperty("Word", row.get(1)).
                    addProperty("Index", row.get(0)).
                    addProperty("Lemma", row.get(2)).
                    addProperty("CPos", row.get(3)).
                    addProperty("Pos", row.get(4)).
                    addProperty("Feats", row.get(5));
        }
        for (List<String> row : rows) {
            //dependency
            int mod = Integer.parseInt(row.get(0));
            try {
                instance.addDependency(Integer.parseInt(row.get(6)), mod, row.get(7), "dep");
            } catch (Exception e) {
                System.err.println("Can't parse dependency");
                instance.getTokens().get(mod).addProperty("DepMissing","missing");
            }
            //role
        }
        return instance;
    }

    public NLPInstance createOpen(List<? extends List<String>> rows) {
        NLPInstance instance = new NLPInstance();
        instance.addToken();
        for (List<String> row : rows) {
            instance.addToken().
                    addProperty(ner1, row.get(1)).
                    addProperty(ner2, row.get(2));
        }
        int index = 0;
        for (List<String> row : rows) {
            //dependency
            instance.addDependency(Integer.parseInt(row.get(3)), index++, row.get(4), "nivre");
        }
        return instance;
    }

    public boolean supportsOpen() {
        return false;
    }
}
