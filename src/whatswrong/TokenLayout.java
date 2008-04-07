package whatswrong;

import javautils.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Sebastian Riedel
 */
public class TokenLayout {

  private HashMap<Pair<TokenVertex, Integer>, TextLayout>
          textLayouts = new HashMap<Pair<TokenVertex, Integer>, TextLayout>();

  private HashMap<TokenVertex, Rectangle2D> bounds = new HashMap<TokenVertex, Rectangle2D>();

  private int rowHeight = 14;
  private int baseline = 0;
  private int margin = 20;
  private int width;
  private int height;


  public void setRowHeight(int rowHeight) {
    this.rowHeight = rowHeight;
  }

  public void setBaseline(int baseline) {
    this.baseline = baseline;
  }

  public void setMargin(int margin) {
    this.margin = margin;
  }


  public int getRowHeight() {
    return rowHeight;
  }

  public int getBaseline() {
    return baseline;
  }

  public int getMargin() {
    return margin;
  }

  public void layout(Collection<TokenVertex> tokens, Graphics2D g2d) {
    textLayouts.clear();
    int lastx = 0;
    height = 0;

    g2d.setColor(Color.BLACK);

    for (TokenVertex token : tokens) {
      Font font = g2d.getFont();//Font.getFont("Helvetica-bold-italic");
      FontRenderContext frc = g2d.getFontRenderContext();
      int index = 0;
      int lasty = baseline + rowHeight;
      int maxX = 0;
      for (TokenProperty p : token.getSortedProperties()) {
        String property = token.getProperty(p);
        g2d.setColor(index == 0 ? Color.BLACK : Color.GRAY);
        TextLayout layout = new TextLayout(property, font, frc);
        layout.draw(g2d, lastx, lasty);
        lasty += rowHeight;
        if (layout.getBounds().getMaxX() > maxX)
          maxX = (int) layout.getBounds().getMaxX();
        textLayouts.put(new Pair<TokenVertex, Integer>(token, index++),layout);
      }
      bounds.put(token, new Rectangle(lastx,baseline,maxX, lasty - baseline));
      lastx+= maxX + margin;
      if (lasty - rowHeight > height) height = lasty - rowHeight;
    }
    width = lastx - margin;
  }

  public TextLayout getProperty(TokenVertex vertex, int index) {
    return textLayouts.get(new Pair<TokenVertex,Integer>(vertex,index));
  }

  public Rectangle2D getBounds(TokenVertex vertex){
    return bounds.get(vertex);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height + 4;
  }
}
