package whatswrong;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * @author Sebastian Riedel
 */
public class WhatsWrongWithMyNLP extends JPanel {

  private NLPCanvas nlpCanvas = new NLPCanvas();
  private JScrollPane nlpScrollPane;

  static {
    System.setProperty("apple.laf.useScreenMenuBar", "true");

  }

  public WhatsWrongWithMyNLP() {
    super(new BorderLayout());

    nlpScrollPane = new JScrollPane(nlpCanvas);
    nlpScrollPane.setPreferredSize(new Dimension(300, 200));

    add(nlpScrollPane, BorderLayout.CENTER);


    setMinimumSize(new Dimension(700, 600));
  }

  public NLPCanvas getNlpCanvas() {
    return nlpCanvas;
  }

  public void scrollToBottom() {
    nlpCanvas.scrollRectToVisible(new Rectangle(
            nlpScrollPane.getViewport().getX(),
            nlpCanvas.getHeight() - nlpScrollPane.getViewport().getHeight(),
            nlpScrollPane.getViewport().getWidth(),
            nlpScrollPane.getViewport().getHeight()));
  }

  private static class WindowMenuItem extends JCheckBoxMenuItem {
    public WindowMenuItem(final JWindow window, String text) {
      super(text, window.isVisible());
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          window.setVisible(!window.isVisible());
        }
      });
    }


    public WindowMenuItem(final Dialog window){
      this(window,window.getTitle());
    }

    public WindowMenuItem(final Window window, String title) {
      super(title, window.isVisible());
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          window.setVisible(!window.isVisible());
        }
      });

      window.addWindowListener(new WindowAdapter() {

        public void windowClosing(WindowEvent e) {
          WindowMenuItem.this.setSelected(false);
        }


        public void windowOpened(WindowEvent e) {
          WindowMenuItem.this.setSelected(true);
        }
      });
    }
  }

  private static class ControllerDialog extends JDialog {


    public ControllerDialog(Frame owner, String title, boolean resizable) throws HeadlessException {
      super(owner, title, false);
      setResizable(resizable);
    }

    public ControllerDialog(String title, boolean resizable){
      this(null,title,resizable);
    }

  }


  public static void main(String[] args) {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "What's Wrong ...");

    final NLPCanvas canvas = new NLPCanvas();

    int canvasWidth = 900;
    int canvasHeight = 300;
    int canvasX = 50;
    int canvasY = 50;
    int canvasBottom = canvasHeight + canvasY;

    CorpusLoader gold = new CorpusLoader("Select Gold");
    CorpusLoader guess = new CorpusLoader("Select Guess");

    //Menu
    JMenuBar menuBar = new JMenuBar();
    JMenu file = new JMenu("File");
    JMenuItem exportEps = new JMenuItem("Export EPS");
    final JFileChooser fc = new JFileChooser();
    exportEps.setAccelerator(KeyStroke.getKeyStroke('E', java.awt.event.InputEvent.ALT_MASK));
    exportEps.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = fc.showSaveDialog(canvas);
        if (returnVal == JFileChooser.APPROVE_OPTION)
          try {
            canvas.exportToEPS(fc.getSelectedFile());
          } catch (IOException e1) {
            e1.printStackTrace();
          }

      }
    });
    file.add(exportEps);
    file.setMnemonic('F');
    JMenu window = new JMenu("Window");

    menuBar.add(file);
    menuBar.add(window);

    //Toolbar
    JToolBar toolBar = new JToolBar("Blub");
    toolBar.add(new JButton("Test"));

    //dummy Frame
    JFrame dummy = new JFrame();
    dummy.setVisible(false);

    //canvas frame
    JFrame canvasFrame = new JFrame("What's Wrong With My NLP?");
    canvasFrame.setSize(canvasWidth, canvasHeight);
    canvasFrame.getContentPane().setLayout(new BorderLayout());
    canvasFrame.getContentPane().add(new JScrollPane(canvas), BorderLayout.CENTER);
    JLabel status = new JLabel("What's Wrong With My NLP version 0.1");
    status.setForeground(Color.LIGHT_GRAY);
    canvasFrame.getContentPane().add(status, BorderLayout.SOUTH);
    canvasFrame.setJMenuBar(menuBar);
    //canvasFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
    canvasFrame.setLocation(canvasX, canvasY);
    canvasFrame.setVisible(true);
    //canvasFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    //window.add(new WindowMenuItem(canvasFrame,"Canvas"));
    //desktop.add(canvasFrame);

    //file selection frame
    final ControllerDialog fileWindow = new ControllerDialog("File Selection",false);
    fileWindow.getContentPane().setLayout(new BoxLayout(fileWindow.getContentPane(), BoxLayout.Y_AXIS));
    fileWindow.getContentPane().add(gold);
    fileWindow.getContentPane().add(new JSeparator());
    fileWindow.getContentPane().add(guess);
    fileWindow.setLocation(canvasX + 20, canvasBottom + 20);
    fileWindow.pack();
    fileWindow.setVisible(true);
    //fileWindow.toBack();
    window.add(new WindowMenuItem(fileWindow));
    //fileFrame.setResizable(false);
    //desktop.add(fileFrame);

    //filter frame
    ControllerDialog filterWindow = new ControllerDialog("Dependency Filters",false);
    filterWindow.getContentPane().setLayout(new BoxLayout(filterWindow.getContentPane(), BoxLayout.Y_AXIS));
    filterWindow.getContentPane().add(new DependencyTypeFilterPanel("Filter By Type", canvas));
    filterWindow.getContentPane().add(new JSeparator());
    filterWindow.getContentPane().add(new DependencyFilterPanel(canvas));
    filterWindow.pack();
    filterWindow.setLocation(canvasX + 250, canvasBottom + 15);
    filterWindow.setVisible(true);
    window.add(new WindowMenuItem(filterWindow));

    //token filter frame
    ControllerDialog tokenFilterWindow = new ControllerDialog("Token Filters",false);
    tokenFilterWindow.getContentPane().setLayout(new BoxLayout(tokenFilterWindow.getContentPane(), BoxLayout.Y_AXIS));
    tokenFilterWindow.getContentPane().add(new TokenFilterPanel(canvas));
    tokenFilterWindow.pack();
    tokenFilterWindow.setLocation(canvasX + 360, canvasBottom + 230);
    tokenFilterWindow.setVisible(true);
    window.add(new WindowMenuItem(tokenFilterWindow));

    //appearance
    ControllerDialog appearance = new ControllerDialog("Appearance", false);
    appearance.getContentPane().setLayout(new BoxLayout(appearance.getContentPane(), BoxLayout.Y_AXIS));
    appearance.getContentPane().add(new AppearancePanel(canvas));
    appearance.pack();
    appearance.setLocation(canvasX + 500, canvasBottom + 25);
    appearance.setVisible(true);
    window.add(new WindowMenuItem(appearance));

    //navigator
    ControllerDialog navigatorWindow = new ControllerDialog("Navigator",false);
    navigatorWindow.getContentPane().setLayout(new BoxLayout(navigatorWindow.getContentPane(), BoxLayout.Y_AXIS));
    navigatorWindow.getContentPane().add(new CorpusNavigator(canvas, gold, guess));
    navigatorWindow.pack();
    navigatorWindow.setMinimumSize(navigatorWindow.getSize());
    navigatorWindow.setLocation(canvasX + 800, canvasBottom + 20);

    navigatorWindow.setVisible(true);
    window.add(new WindowMenuItem(navigatorWindow, "Navigator"));

    canvasFrame.requestFocus();
    //canvasFrame.requestFocusInWindow();

  }

}
