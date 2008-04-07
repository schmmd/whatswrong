package whatswrong;

import whatswrong.io.CorpusFormat;
import whatswrong.io.CoNLLProcessor;
import whatswrong.io.CoNLL2008;
import whatswrong.io.CoNLLFormat;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author Sebastian Riedel
 */
public class CorpusLoader extends JPanel {

  private List<NLPInstance> selected;
  private List<List<NLPInstance>> corpora;
  private DefaultListModel fileNames;
  private HashMap<String, CorpusFormat> formats = new HashMap<String, CorpusFormat>();
  private HashMap<String, CoNLLProcessor> conllProcessors = new HashMap<String, CoNLLProcessor>();

  private ArrayList<Listener> changeListeners = new ArrayList<Listener>();
  private JButton remove;


  public static interface Listener {
    void corpusAdded(List<NLPInstance> corpus, CorpusLoader src);
    void corpusRemoved(List<NLPInstance> corpus, CorpusLoader src);
    void corpusSelected(List<NLPInstance> corpus, CorpusLoader src);
  }

  public void addChangeListener(Listener changeListener) {
    changeListeners.add(changeListener);
  }

  private void fireAdded(List<NLPInstance> corpus) {
    for (Listener listener : changeListeners) {
      listener.corpusAdded(corpus,this);
    }
  }

  private void fireRemoved(List<NLPInstance> corpus) {
    for (Listener listener : changeListeners) {
      listener.corpusRemoved(corpus,this);
    }
  }

  private void fireSelected(List<NLPInstance> corpus) {
    for (Listener listener : changeListeners) {
      listener.corpusSelected(corpus,this);
    }
  }


  public List<NLPInstance> getSelected() {
    return selected == null ? null : Collections.unmodifiableList(selected);
  }


  private class LoadAccessory extends JPanel {
    private JComboBox filetypeComboBox;
    private JSpinner start;
    private JSpinner end;
    private JComponent accessory;

    public LoadAccessory() {
      setLayout(new GridBagLayout());
      setBorder(new TitledBorder(new EtchedBorder(), "Parameters"));
      int y = 0;
      add(new JLabel("Format:"), new SimpleGridBagConstraints(y, true));

      filetypeComboBox = new JComboBox(new Vector<Object>(formats.values()));
      filetypeComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          remove(accessory);
          accessory = ((CorpusFormat) filetypeComboBox.getSelectedItem()).getAccessory();
          add(accessory, new SimpleGridBagConstraints(0, 2, 2, 1));
          repaint();
        }
      });
      start = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
      start.setPreferredSize(new Dimension(100, (int) start.getPreferredSize().getHeight()));
      end = new JSpinner(new SpinnerNumberModel(200, 0, Integer.MAX_VALUE, 1));
      end.setPreferredSize(new Dimension(100, (int) start.getPreferredSize().getHeight()));
      accessory = ((CorpusFormat) filetypeComboBox.getSelectedItem()).getAccessory();

      add(filetypeComboBox, new SimpleGridBagConstraints(y++, false));
      add(new JSeparator(), new SimpleGridBagConstraints(0, y++, 2, 1));
      add(accessory, new SimpleGridBagConstraints(0, y++, 2, 1));
      add(new JSeparator(), new SimpleGridBagConstraints(0, y++, 2, 1));
      add(new JLabel("From:"), new SimpleGridBagConstraints(y, true));
      add(start, new SimpleGridBagConstraints(y++, false));
      add(new JLabel("To:"), new SimpleGridBagConstraints(y, true));
      add(end, new SimpleGridBagConstraints(y++, false));
    }

    public CorpusFormat getFormat() {
      return (CorpusFormat) filetypeComboBox.getSelectedItem();
    }

    public int getStart() {
      return (Integer) start.getModel().getValue();
    }

    public int getEnd() {
      return (Integer) end.getModel().getValue();
    }

  }

  public void addFormat(CorpusFormat format) {
    formats.put(format.getName(), format);
  }

  public CorpusLoader(String title) {
    setLayout(new GridBagLayout());
    setBorder(new EmptyBorder(5, 5, 5, 5));

    //setBorder(new TitledBorder(new EtchedBorder(), title));
    GridBagConstraints c = new GridBagConstraints();

    conllProcessors.put(CoNLL2008.name, new CoNLL2008());
    addFormat(new CoNLLFormat());

    corpora = new ArrayList<List<NLPInstance>>();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 2;
    add(new JLabel(title), c);

    //file list
    c.gridy = 1;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.5;
    c.weighty = 0.5;
    fileNames = new DefaultListModel();
    final JList files = new JList(fileNames);
    files.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (files.getSelectedIndex() == -1) {
          selected = null;
          remove.setEnabled(false);
          fireSelected(null);

        } else {
          selected = corpora.get(files.getSelectedIndex());
          remove.setEnabled(true);
          fireSelected(selected);
        }
      }
    });
    files.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane pane = new JScrollPane(files);
    pane.setPreferredSize(new Dimension(150, 50));
    pane.setMinimumSize(new Dimension(150, 50));
    add(pane, c);

    //add files
    final JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Load Corpus");
    final LoadAccessory accessory = new LoadAccessory();
    fileChooser.setAccessory(accessory);
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 2;
    c.fill = GridBagConstraints.NONE;
    c.weighty = 0;
    JButton add = new JButton("Add");
    add.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = fileChooser.showOpenDialog(CorpusLoader.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            List<NLPInstance> corpus = accessory.getFormat().load(fileChooser.getSelectedFile(),
              accessory.getStart(), accessory.getEnd());
            corpora.add(corpus);
            fileNames.addElement(fileChooser.getSelectedFile().getName());
            files.setSelectedIndex(fileNames.size() - 1);
            fireAdded(corpus);
          } catch (FileNotFoundException e1) {
            e1.printStackTrace();
          } catch (IOException e1) {
            e1.printStackTrace();
          }
        }
      }
    });
    add(add, c);
    c.gridx = 1;
    remove = new JButton("Remove");
    remove.setEnabled(false);
    remove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = files.getSelectedIndex();
        if (index != -1) {
          fileNames.remove(index);
          List<NLPInstance> corpus = corpora.remove(index);
          fireRemoved(corpus);
          //repaint();
        }
      }
    });
    add(remove, c);

    setSize(new Dimension(150, 200));
    setMinimumSize(new Dimension(150, 200));
  }


}
