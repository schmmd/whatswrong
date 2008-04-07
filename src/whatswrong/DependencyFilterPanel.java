package whatswrong;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Sebastian Riedel
 */
public class DependencyFilterPanel extends ControllerPanel {

  public DependencyFilterPanel(final NLPCanvas nlpCanvas) {
    setLayout(new GridBagLayout());
    //setBorder(new TitledBorder(new EtchedBorder(), "Filter By Token"));
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.weightx = 0.0;
    c.anchor = GridBagConstraints.EAST;
    add(new JLabel("Label:"),c);

    //setBorder(new TitledBorder(new EtchedBorder(), "Filter By Label"));
    final JTextField labelField = new JTextField();
    labelField.setColumns(10);
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    //c.anchor = GridBagConstraints.WEST;
    c.gridx = 1;
    add(labelField,c);
    labelField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        nlpCanvas.getDependencyLabelFilter().clear();
        String[] split = labelField.getText().split("[,]");
        for (String label : split)
          nlpCanvas.getDependencyLabelFilter().addAllowedLabel(label);
        nlpCanvas.updateNLPGraphics();
      }
    });


    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    add(new JLabel("Token:"),c);


    c.gridx = 1;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    final JTextField textField = new JTextField();
    textField.setColumns(10);
    add(textField,c);
    textField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        nlpCanvas.getDependencyTokenFilter().clear();
        String[] split = textField.getText().split("[,]");
        for (String property : split)
          nlpCanvas.getDependencyTokenFilter().addAllowedProperty(property);
        nlpCanvas.updateNLPGraphics();
      }
    });
    final JCheckBox usePaths = new JCheckBox("Only Paths",nlpCanvas.getDependencyTokenFilter().isUsePaths());
    usePaths.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nlpCanvas.getDependencyTokenFilter().setUsePaths(usePaths.isSelected());
        nlpCanvas.updateNLPGraphics();
      }
    });

    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    add(new JLabel("Extra:"),c);

    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.WEST;
    add(usePaths,c);

    final JButton onlySelected = new JButton("Hide Unselected");
    //onlySelected.setEnabled(!nlpCanvas.getDependencyLayout().getSelected().isEmpty());
    onlySelected.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!nlpCanvas.getDependencyLayout().getSelected().isEmpty())
          nlpCanvas.getDependencyLayout().onlyShow(nlpCanvas.getDependencyLayout().getSelected());
        else
          nlpCanvas.getDependencyLayout().showAll();
        nlpCanvas.updateNLPGraphics();
      }
    });
    add(onlySelected, new SimpleGridBagConstraints(3,false));



    //setPreferredSize(new Dimension(200, (int) getPreferredSize().getHeight()));

  }
}
