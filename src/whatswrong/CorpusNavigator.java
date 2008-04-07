package whatswrong;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryParser.QueryParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * @author Sebastian Riedel
 */
public class CorpusNavigator extends JPanel implements CorpusLoader.Listener {

  private CorpusLoader guess, gold;
  private NLPCanvas canvas;
  private JSpinner spinner;
  private SpinnerNumberModel numberModel;
  private HashMap<List<NLPInstance>, IndexSearcher> indices = new HashMap<List<NLPInstance>, IndexSearcher>();
  //private HashMap<List<NLPInstance>>
  private IndexSearcher indexSearcher;
  private Analyzer analyzer;
  private JButton searchButton;
  private JList results;
  private JTextField search;

  public void corpusAdded(List<NLPInstance> corpus, CorpusLoader src) {
  }

  public void corpusRemoved(List<NLPInstance> corpus, CorpusLoader src) {
    indices.remove(corpus);
  }

  public void corpusSelected(List<NLPInstance> corpus, CorpusLoader src) {
    updateCanvas();
  }

  private static class Result {
    public final String text;
    public final int nr;

    public Result(int nr, String text) {
      this.text = text;
      this.nr = nr;
    }

    public String toString() {
      return text;
    }
  }

  public CorpusNavigator(NLPCanvas canvas, CorpusLoader gold, CorpusLoader guess) {
    super(new GridBagLayout());
    this.guess = guess;
    this.gold = gold;
    this.canvas = canvas;
    guess.addChangeListener(this);
    gold.addChangeListener(this);
    setBorder(new EmptyBorder(5, 5, 5, 5));
    //setBorder(new TitledBorder(new EtchedBorder(), "Navigate"));

    numberModel = new SpinnerNumberModel();
    numberModel.setMinimum(0);
    numberModel.setMaximum(100);
    spinner = new JSpinner(numberModel);
    spinner.setEnabled(false);
    JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(spinner);
    spinner.setEditor(numberEditor);
    spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        updateCanvas();
      }
    });

    //add(new JSeparator(), new SimpleGridBagConstraints(0,1,2,1));
    search = new JTextField(10);
    search.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        searchCorpus();
      }
    });
    //add(search, new SimpleGridBagConstraints(1, false));

    //search button
    searchButton = new JButton("Search");
    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.add(search, BorderLayout.CENTER);
    searchPanel.add(searchButton, BorderLayout.EAST);
    //add(searchButton, new SimpleGridBagConstraints(2, false, false));
    searchButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        searchCorpus();
      }
    });

    results = new JList();
    results.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        int selectedIndex = results.getSelectedIndex();
        if (selectedIndex != -1) {
          int nr = ((Result) results.getSelectedValue()).nr;
          spinner.setValue(nr);
          repaint();
          CorpusNavigator.this.canvas.setNLPInstance(CorpusNavigator.this.gold.getSelected().get(nr));
          CorpusNavigator.this.canvas.updateNLPGraphics();
        }
      }
    });


    add(new JLabel("Nr:"), new SimpleGridBagConstraints(0, true));
    add(spinner, new SimpleGridBagConstraints(0, false, false));
    add(new JSeparator(), new SimpleGridBagConstraints(0,1,2,1));
    //add(new JLabel("Search:"), new SimpleGridBagConstraints(2, true));
    add(searchPanel, new SimpleGridBagConstraints(0,2,2,1));
    add(new JScrollPane(results), new SimpleGridBagConstraints(0,3,2,1));

    //setPreferredSize((new Dimension(100, (int) getPreferredSize().getHeight())));
    updateCanvas();
    analyzer = new SimpleAnalyzer();
    //analyzer.
  }

  private void searchCorpus() {
    if (search.getText().trim().equals("")) return;
    try {
      //System.out.println("Searching...");
      QueryParser parser = new QueryParser("Word", analyzer);
      Query query = parser.parse(search.getText());
      Hits hits = indexSearcher.search(query);
      Highlighter highlighter = new Highlighter(new QueryScorer(query));
      DefaultListModel model = new DefaultListModel();
      for (int i = 0; i < hits.length(); i++) {
        Document hitDoc = hits.doc(i);
        int nr = Integer.parseInt(hitDoc.get("<nr>"));
        //System.out.println(hitDoc.get("<nr>"));
        String best = null;
        for (Object field : hitDoc.getFields()) {
          Field f = (Field) field;
          best = highlighter.getBestFragment(analyzer, f.name(), hitDoc.get(f.name()));
          if (best != null) break;
        }
        if (best != null) model.addElement(new Result(nr, "<html>" + best + "</html>"));
        //System.out.println(highlighter.getBestFragment(analyzer, "Word", hitDoc.get("Word")));
        //assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
      }
      results.setModel(model);
      repaint();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }




  private IndexSearcher getIndex(List<NLPInstance> corpus) {
    IndexSearcher index = indices.get(corpus);
    if (index == null) {
      index = createIndex(corpus);
      indices.put(corpus, index);
    }
    return index;
  }

  private IndexSearcher createIndex(List<NLPInstance> corpus) {
    try {
      System.err.println("Creating Index");
      RAMDirectory directory = new RAMDirectory();
      IndexWriter iwriter;
      iwriter = new IndexWriter(directory, analyzer, true);
      iwriter.setMaxFieldLength(25000);

      int nr = 0;
      for (NLPInstance instance : corpus) {
        Document doc = new Document();
        HashMap<TokenProperty, StringBuffer> sentences = new HashMap<TokenProperty, StringBuffer>();
        for (TokenVertex token : instance.getTokens()) {
          for (TokenProperty p : token.getPropertyTypes()) {
            StringBuffer buffer = sentences.get(p);
            if (buffer == null) {
              buffer = new StringBuffer();
              sentences.put(p, buffer);
            }
            if (token.getIndex() > 0) buffer.append(" ");
            buffer.append(token.getProperty(p));
          }
        }
        for (TokenProperty p : sentences.keySet()) {
          doc.add(new Field(p.getName(), sentences.get(p).toString(), Field.Store.YES, Field.Index.TOKENIZED));
        }
        doc.add(new Field("<nr>", String.valueOf(nr), Field.Store.YES, Field.Index.UN_TOKENIZED));

        System.err.print(".");
        iwriter.addDocument(doc);
        nr++;
      }
      iwriter.optimize();
      iwriter.close();
      return new IndexSearcher(directory);
    } catch (IOException e) {
      throw new RuntimeException("Couldn't build the index");
    }

  }

  private void updateCanvas() {
    if (gold.getSelected() != null) {
      searchButton.setEnabled(true);
      search.setEnabled(true);
      spinner.setEnabled(true);
      results.setEnabled(true);
      if (guess.getSelected() == null) {
        int maxIndex = gold.getSelected().size() - 1;
        int index = Math.min((Integer) spinner.getValue(), maxIndex);
        spinner.setValue(index);
        numberModel.setMaximum(maxIndex);

        indexSearcher = getIndex(gold.getSelected());
        canvas.setNLPInstance(gold.getSelected().get(index));
        canvas.updateNLPGraphics();
      } else {
        int maxIndex = Math.min(gold.getSelected().size() - 1, guess.getSelected().size() - 1);
        numberModel.setMaximum(maxIndex);
        int index = Math.min((Integer) spinner.getValue(), maxIndex);
        spinner.setValue(index);
        NLPInstance goldInstance = gold.getSelected().get(index);
        NLPInstance guessInstance = guess.getSelected().get(index);
        NLPInstance diff = new NLPInstance();
        diff.addTokens(goldInstance.getTokens());
        HashSet<DependencyEdge> fn = new HashSet<DependencyEdge>(goldInstance.getDependencies());
        fn.removeAll(guessInstance.getDependencies());
        HashSet<DependencyEdge> fp = new HashSet<DependencyEdge>(guessInstance.getDependencies());
        fp.removeAll(goldInstance.getDependencies());
        HashSet<DependencyEdge> matches = new HashSet<DependencyEdge>(goldInstance.getDependencies());
        matches.retainAll(guessInstance.getDependencies());
        for (DependencyEdge edge : fn) {
          String type = edge.getType() + ":FN";
          diff.addDependency(edge.getFrom(), edge.getTo(), edge.getLabel(), type);
        }
        for (DependencyEdge edge : fp) {
          String type = edge.getType() + ":FP";
          diff.addDependency(edge.getFrom(), edge.getTo(), edge.getLabel(), type);
        }
        for (DependencyEdge edge : matches)
          diff.addDependency(edge.getFrom(), edge.getTo(), edge.getLabel(), edge.getType() + ":Match");
        canvas.getDependencyLayout().setColor("FN", Color.BLUE);
        canvas.getDependencyLayout().setColor("FP", Color.RED);
        canvas.setNLPInstance(diff);
        canvas.updateNLPGraphics();

      }
    } else {
      searchButton.setEnabled(false);
      search.setEnabled(false);
      spinner.setEnabled(false);
      searchButton.setEnabled(false);
      results.setEnabled(false);


      NLPInstance example = new NLPInstance();
      example.addToken().addProperty("Word", "[root]").addProperty("Index", "0");
      example.addToken().addProperty("Word", "Add").addProperty("Index", "1");
      example.addToken().addProperty("Word", "a").addProperty("Index", "2");
      example.addToken().addProperty("Word", "gold").addProperty("Index", "3");
      example.addToken().addProperty("Word", "corpus").addProperty("Index", "4");
      example.addToken().addProperty("Word", "!").addProperty("Index", "5");
      example.addDependency(0, 1, "ROOT", "dep");
      example.addDependency(0, 5, "PUNC", "dep");
      example.addDependency(1, 4, "OBJ", "dep");
      example.addDependency(4, 2, "DET", "dep");
      example.addDependency(4, 3, "MOD", "dep");
      example.addDependency(1, 4, "A1", "role");
      canvas.getDependencyLayout().setStroke("role", new BasicStroke(1.0f,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL, 10,
        new float[]{2.0f}, 0));
      canvas.setNLPInstance(example);
      canvas.updateNLPGraphics();

    }
  }
}
