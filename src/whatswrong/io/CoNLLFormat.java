package whatswrong.io;

import whatswrong.SimpleGridBagConstraints;
import whatswrong.NLPInstance;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Sebastian Riedel
 */
public class CoNLLFormat implements CorpusFormat {

    private JPanel accessory;
    private SortedMap<String, CoNLLProcessor> processors = new TreeMap<String, CoNLLProcessor>();
    private JComboBox year;
    private JCheckBox open;

    public CoNLLFormat() {
        addProcessor("2008", new CoNLL2008());
        addProcessor("2006", new CoNLL2006());

        accessory = new JPanel(new GridBagLayout());
        year = new JComboBox(new Vector<Object>(processors.values()));
        year.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                open.setEnabled(((CoNLLProcessor) year.getSelectedItem()).supportsOpen());
            }
        });
        open = new JCheckBox("open", false);
        open.setEnabled(((CoNLLProcessor) year.getSelectedItem()).supportsOpen());

        accessory.add(new JLabel("Year:"), new SimpleGridBagConstraints(0, true));
        accessory.add(year, new SimpleGridBagConstraints(0, false));
        accessory.add(open, new SimpleGridBagConstraints(1, false));

    }

    public void addProcessor(String name, CoNLLProcessor processor) {
        processors.put(name, processor);
    }

    public String toString() {
        return getName();
    }

    public String getName() {
        return "CoNLL";
    }

    public JComponent getAccessory() {
        return accessory;
    }

    public java.util.List<NLPInstance> load(File file, int from, int to) throws IOException {
        CoNLLProcessor processor = (CoNLLProcessor) year.getSelectedItem();
        java.util.List<NLPInstance> result = loadCoNLL08(file, from, to, processor, false);
        if (open.isSelected()) {
            String filename = file.getName().substring(0, file.getName().lastIndexOf('.')) + ".open";
            File openFile = new File(file.getParent() + "/" + filename);
            java.util.List<NLPInstance> openCorpus = loadCoNLL08(openFile, from, to, processor, true);
            for (int i = 0; i < openCorpus.size(); ++i) {
                result.get(i).merge(openCorpus.get(i));
            }

        }
        return result;
    }

    private java.util.List<NLPInstance> loadCoNLL08(File file, int from, int to, CoNLLProcessor processor, boolean open) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        ArrayList<NLPInstance> corpus = new ArrayList<NLPInstance>();
        ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
        int instanceNr = 0;
        for (String line = reader.readLine(); line != null && instanceNr < to; line = reader.readLine()) {
            line = line.trim();
            if (line.equals("")) {
                if (instanceNr++ < from) continue;
                NLPInstance instance = open ? processor.createOpen(rows) : processor.create(rows);
                corpus.add(instance);
                rows.clear();
            } else {
                if (instanceNr < from) continue;
                StringTokenizer tokenizer = new StringTokenizer(line, "[ \t]");
                ArrayList<String> row = new ArrayList<String>();
                while (tokenizer.hasMoreElements()) row.add(tokenizer.nextToken());
                rows.add(row);
            }

        }
        if (rows.size() > 0) corpus.add(open ? processor.createOpen(rows) : processor.create(rows));
        return corpus;

    }

}

