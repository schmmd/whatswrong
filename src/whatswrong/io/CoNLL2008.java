package whatswrong.io;

import whatswrong.TokenProperty;
import whatswrong.NLPInstance;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Sebastian Riedel
 */

public class CoNLL2008 implements CoNLLProcessor {

    public static final String name = "2008";

    private TokenProperty
            ner1 = new TokenProperty("ner1", 10),
            ner2 = new TokenProperty("ner2", 11);

    public String toString() {
        return name;
    }

    public NLPInstance create(List<? extends List<String>> rows) {
        NLPInstance instance = new NLPInstance();
        instance.addToken().addProperty("Word", "-Root-");
        ArrayList<Integer> predicates = new ArrayList<Integer>();
        for (List<String> row : rows) {
            instance.addToken().
                    addProperty("Word", row.get(1)).
                    addProperty("Index", row.get(0)).
                    addProperty("Lemma", row.get(2)).
                    addProperty("Pos", row.get(3)).
                    addProperty("Predicate", row.get(10));
            if (!row.get(10).equals("_"))
                predicates.add(Integer.parseInt(row.get(0)));
        }
        for (List<String> row : rows) {
            //dependency
            instance.addDependency(Integer.parseInt(row.get(8)), Integer.parseInt(row.get(0)), row.get(9), "dep");
            //role
            for (int col = 11; col < row.size(); ++col) {
                String label = row.get(col);
                if (!label.equals("_")) {
                    Integer pred = predicates.get(col - 11);
                    int arg = Integer.parseInt(row.get(0));
                    //if (arg != pred)
                    instance.addDependency(pred, arg, label, "role");
                }
            }
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
        return true;
    }
}