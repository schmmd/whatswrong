package whatswrong;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Sebastian Riedel
 */
public class AppearancePanel extends JPanel {

  public AppearancePanel(final NLPCanvas nlpCanvas) {
    super(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1.0;
    c.weighty = 1.0;
    //c.fill = GridBagConstraints.HORIZONTAL;
    //setBorder(new TitledBorder(new EtchedBorder(),"Change Appearance"));
    setBorder(new EmptyBorder(5,5,5,5));
    final JSlider marginSlider = new JSlider(JSlider.HORIZONTAL,0,50,nlpCanvas.getTokenLayout().getMargin());
    marginSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        nlpCanvas.getTokenLayout().setMargin(marginSlider.getValue());
        nlpCanvas.updateNLPGraphics();
      }
    });
    c.gridx = 0;
    c.anchor = GridBagConstraints.EAST;
    add(new JLabel("Width:"),c);
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 1;
    add(marginSlider, c);
    marginSlider.setToolTipText("Margin between tokens");
    marginSlider.setMaximumSize(new Dimension(20,50));

    final JSlider heightSlider = new JSlider(JSlider.HORIZONTAL,10,50,nlpCanvas.getDependencyLayout().getHeightPerLevel());
    heightSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        nlpCanvas.getDependencyLayout().setHeightPerLevel(heightSlider.getValue());
        nlpCanvas.updateNLPGraphics();
        //scrollToBottom();
      }
    });
    c.gridx = 0;
    c.anchor = GridBagConstraints.EAST;
    add(new JLabel("Height:"),c);
    c.gridx = 1;
    c.anchor = GridBagConstraints.WEST;    
    add(heightSlider,c);
    heightSlider.setToolTipText("Tree height");
    heightSlider.setMaximumSize(new Dimension(20,50));
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 2;
    c.gridx = 0;
    add(new JSeparator(),c);
    c.fill = GridBagConstraints.NONE;
    c.gridwidth = 1;
    c.gridx = 0;
    c.anchor = GridBagConstraints.EAST;
    add(new JLabel("Edges:"),c);
    c.gridx = 1;
    c.anchor = GridBagConstraints.WEST;
    final JCheckBox curved = new JCheckBox("Curved", nlpCanvas.getDependencyLayout().isCurve());
    curved.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nlpCanvas.getDependencyLayout().setCurve(curved.isSelected());
        nlpCanvas.updateNLPGraphics();
      }
    });
    add(curved,c);

    c.fill = GridBagConstraints.NONE;
    c.gridwidth = 1;
    c.gridx = 1;
    c.anchor = GridBagConstraints.WEST;
    final JCheckBox anti = new JCheckBox("Antialiasing", nlpCanvas.isAntiAliasing());
    anti.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nlpCanvas.setAntiAliasing(anti.isSelected());
        nlpCanvas.updateNLPGraphics();
      }
    });
    add(anti,c);


    setMinimumSize(new Dimension(0,150));
    

  }
}
