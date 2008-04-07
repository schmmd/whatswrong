package whatswrong;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sebastian Riedel
 */
public class DependencyTypeFilterPanel extends ControllerPanel implements ChangeListener {
  private NLPCanvas nlpCanvas;
  private String title;


  public DependencyTypeFilterPanel(String title, NLPCanvas nlpCanvas) {
    nlpCanvas.addChangeListenger(this);
    setLayout(new GridBagLayout());
    //setBorder(new TitledBorder(new EtchedBorder(), title));
    this.title = title;
    this.nlpCanvas = nlpCanvas;
    setPreferredSize(new Dimension(200,80));
    updateTypes();
  }

  private void separateTypes(Set<String> usedTypes, HashSet<String> prefixTypes, HashSet<String> postfixTypes) {
    for (String type : usedTypes){
      int index = type.indexOf(':');
      if (index == -1)
        prefixTypes.add(type);
      else {
        prefixTypes.add(type.substring(0,index));
        postfixTypes.add(type.substring(index+1));

      }
    }
  }


  public String getTitle() {
    return title;
  }

  public void addSupportedDependencyType(final String type) {
    addType(type, true);
    repaint();
  }

  private void addType(final String type, boolean prefix) {
    final JCheckBox checkBox = new JCheckBox(type, !nlpCanvas.getDependencyTypeFilter().forbids(type));
    checkBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (checkBox.isSelected()) nlpCanvas.getDependencyTypeFilter().removeForbiddenType(type);
        else nlpCanvas.getDependencyTypeFilter().addForbiddenType(type);
        nlpCanvas.updateNLPGraphics();
      }
    });
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1.0;
    c.anchor = GridBagConstraints.WEST;
    //c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = prefix ? 1 : 2;
    add(checkBox,c);
  }


  public void stateChanged(ChangeEvent e) {
    updateTypes();
    repaint();
  }

  private void updateTypes() {
    removeAll();
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1.0;    
    c.anchor = GridBagConstraints.EAST;
    add(new JLabel("Type:"),c);
    HashSet<String> prefixTypes = new HashSet<String>();
    HashSet<String> postfixTypes = new HashSet<String>();
    separateTypes(nlpCanvas.getUsedTypes(), prefixTypes, postfixTypes);
    for (String type : prefixTypes)
      addType(type,true);
    for (String type : postfixTypes)
      addType(type,false);
  }
}
