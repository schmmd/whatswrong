package whatswrong;

/**
 * @author Sebastian Riedel
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MoveWindow extends JWindow implements MouseListener, MouseMotionListener {
  Point location;
  MouseEvent pressed;
  private JPanel contentPane;
  private JLabel titleLabel;

  public MoveWindow(Frame owner, String title) {
    super(owner);
    getContentPane().setLayout(new BorderLayout());
    titleLabel = new JLabel(title);
    titleLabel.setBackground(Color.LIGHT_GRAY);
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setOpaque(true);
    getContentPane().add(titleLabel, BorderLayout.NORTH);
    contentPane = new JPanel();
    getContentPane().add(contentPane, BorderLayout.CENTER);
    addMouseListener(this);
    addMouseMotionListener(this);
    addWindowFocusListener(new WindowFocusListener() {
      public void windowGainedFocus(WindowEvent e) {
        titleLabel.setBackground(Color.GRAY);
      }

      public void windowLostFocus(WindowEvent e) {
        titleLabel.setBackground(Color.LIGHT_GRAY);

      }
    });
  }

  public JComponent getInternalContent(){
    return contentPane;
  }

  public void mousePressed(MouseEvent me) {
    pressed = me;
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent me) {
    location = getLocation(location);
    int x = location.x - pressed.getX() + me.getX();
    int y = location.y - pressed.getY() + me.getY();
    setLocation(x, y);
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public static void main(String args[]) {
    MoveWindow window = new MoveWindow(null,"Test");
    window.setSize(300, 300);
    window.setLocationRelativeTo(null);
    window.setVisible(true);
  }
}