package whatswrong;


import javautils.HashMultiMapList;
import javautils.Counter;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * @author Sebastian Riedel
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class DependencyLayout {

  private int baseline = -1;
  private int heightPerLevel = 15;
  private int arrowSize = 2;
  private int vertexExtraSpace = 12;
  private boolean curve = true;

  private HashMap<String, Color> colors = new HashMap<String, Color>();
  private HashMap<String, BasicStroke> strokes = new HashMap<String, BasicStroke>();
  private BasicStroke defaultStroke = new BasicStroke();
  private HashMap<DependencyEdge, Point> from;
  private HashMap<DependencyEdge, Point> to;
  private HashMap<Shape,DependencyEdge> shapes = new HashMap<Shape, DependencyEdge>();
  private HashSet<DependencyEdge> selected = new HashSet<DependencyEdge>();
  private HashSet<DependencyEdge> visible = new HashSet<DependencyEdge>();


  private int maxHeight;
  private int maxWidth;

  public void setColor(String type, Color color) {
    colors.put(type, color);
  }

  public void setStroke(String type, BasicStroke stroke) {
    strokes.put(type, stroke);
  }

  public BasicStroke getStroke(DependencyEdge edge){
    BasicStroke stroke = getStroke(edge.getType());
    return (selected.contains(edge)) ?
            new BasicStroke(stroke.getLineWidth()+ 1.5f, stroke.getEndCap(), stroke.getLineJoin(),
                    stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase()):
            stroke;
  }

  public BasicStroke getStroke(String type) {
    for (String substring : strokes.keySet())
      if (type.contains(substring)) {
        return strokes.get(substring);
      }
    return defaultStroke;
  }

  public Color getColor(String type) {
    for (String substring : colors.keySet())
      if (type.contains(substring)) return colors.get(substring);
    return Color.BLACK;
  }

  public void addToSelection(DependencyEdge edge){
    selected.add(edge);
  }

  public void removeFromSelection(DependencyEdge edge){
    selected.remove(edge);
  }

  public void clearSelection(){
    selected.clear();
  }

  public void onlyShow(Collection<DependencyEdge> edges){
    this.visible.clear();
    this.visible.addAll(edges);
  }

  public void showAll(){
    visible.clear();
  }

  public void toggleSelection(DependencyEdge edge){
    if (selected.contains(edge)) selected.remove(edge);
    else selected.add(edge);
  }


  public Set<DependencyEdge> getSelected() {
    return Collections.unmodifiableSet(selected);
  }

  public void select(DependencyEdge edge){
    selected.clear();
    selected.add(edge);
  }


  public DependencyEdge getEdgeAt(Point2D p, int radius){
    Rectangle2D cursor = new Rectangle.Double(p.getX()-radius/2,p.getY()-radius/2,radius,radius);
    double maxY = Integer.MIN_VALUE;
    DependencyEdge result = null;
    for (Shape s : shapes.keySet()){
      if (s.intersects(cursor) && s.getBounds().getY() > maxY) {
        result = shapes.get(s);
        maxY = s.getBounds().getY();
      }
    }
    return result;
  }

  public void layout(Collection<DependencyEdge> edges, TokenLayout tokenLayout, Graphics2D g2d) {
    if (visible.size() > 0){
      edges = new HashSet<DependencyEdge>(edges);
      edges.retainAll(visible);
    }

    //find out height of each edge
    shapes.clear();

    HashMultiMapList<TokenVertex, DependencyEdge> loops = new HashMultiMapList<TokenVertex, DependencyEdge>();
    HashSet<DependencyEdge> allLoops = new HashSet<DependencyEdge>();
    HashSet<TokenVertex> tokens = new HashSet<TokenVertex>();
    for (DependencyEdge edge : edges) {
      tokens.add(edge.getFrom());
      tokens.add(edge.getTo());
      if (edge.getFrom() == edge.getTo()) {
        loops.add(edge.getFrom(), edge);
        allLoops.add(edge);
      }
    }
    edges.removeAll(allLoops);

    Counter<DependencyEdge> depth = new Counter<DependencyEdge>();
    Counter<DependencyEdge> offset = new Counter<DependencyEdge>();
    HashMultiMapList<DependencyEdge, DependencyEdge>
            dominates = new HashMultiMapList<DependencyEdge, DependencyEdge>();

    for (DependencyEdge over : edges)
      for (DependencyEdge under : edges) {
        if (over != under && (over.strictlyCovers(under) ||
                over.covers(under) && over.lexicographicOrder(under) > 0)) {
          dominates.add(over, under);
        }
      }

    for (DependencyEdge edge : edges)
      calculateDepth(dominates, depth, edge);

    for (DependencyEdge left : edges)
      for (DependencyEdge right : edges) {
        if (left != right && left.crosses(right) &&
                depth.get(left) == depth.get(right)) {
          if (offset.get(left) == 0 && offset.get(right) == 0)
            offset.increment(left, heightPerLevel / 2);
          else if (offset.get(left) == offset.get(right)) {
            offset.put(left, heightPerLevel / 3);
            offset.put(right, heightPerLevel * 2 / 3);
          }
        }
      }

    //calculate maxHeight and maxWidth
    maxWidth = tokenLayout.getWidth();
    maxHeight = (depth.getMaximum() + 1) * heightPerLevel + 3;

    //build map from vertex to incoming/outgoing edges
    HashMultiMapList<TokenVertex, DependencyEdge> vertex2edges = new HashMultiMapList<TokenVertex, DependencyEdge>();
    for (DependencyEdge edge : edges) {
      vertex2edges.add(edge.getFrom(), edge);
      vertex2edges.add(edge.getTo(), edge);
    }
    //assign starting and end points of edges by sorting the edges per vertex
    from = new HashMap<DependencyEdge, Point>();
    to = new HashMap<DependencyEdge, Point>();
    for (final TokenVertex token : tokens) {
      List<DependencyEdge> connections = vertex2edges.get(token);
      Collections.sort(connections, new Comparator<DependencyEdge>() {
        public int compare(DependencyEdge edge1, DependencyEdge edge2) {
          //if they point in different directions order is defined by left to right
          if (edge1.leftOf(token) && edge2.rightOf(token)) return -1;
          if (edge2.leftOf(token) && edge1.rightOf(token)) return 1;
          //otherwise we order by length
          int diff = edge2.getLength() - edge1.getLength();
          if (edge1.leftOf(token) && edge2.leftOf(token)) {
            return diff != 0 ? -diff : edge1.lexicographicOrder(edge2);
          } else
            return diff != 0 ? diff : edge2.lexicographicOrder(edge1);
        }
      });
      //now put points along the token vertex wrt to ordering
      List<DependencyEdge> loopsOnVertex = loops.get(token);
      int width = (int) ((tokenLayout.getBounds(token).getWidth() + vertexExtraSpace) /
              (connections.size() + 1 + loopsOnVertex.size() * 2));
      int x = (int) (tokenLayout.getBounds(token).getMinX() - vertexExtraSpace / 2 + width);
      for (DependencyEdge loop : loopsOnVertex) {
        Point point = new Point(x, baseline + maxHeight);
        from.put(loop, point);
        x += width;
      }
      for (DependencyEdge edge : connections) {
        Point point = new Point(x, baseline + maxHeight);
        if (edge.getFrom().equals(token))
          from.put(edge, point);
        else
          to.put(edge, point);
        x += width;

      }
      for (DependencyEdge loop : loopsOnVertex) {
        Point point = new Point(x, baseline + maxHeight);
        to.put(loop, point);
        x += width;
      }
    }

    //draw each edge
    edges.addAll(allLoops);
    for (DependencyEdge edge : edges) {
      //set Color and remember old color
      Color old = g2d.getColor();
      g2d.setColor(getColor(edge.getType()));
      //draw lines
      int height = baseline + maxHeight - (depth.get(edge) + 1) * heightPerLevel + offset.get(edge);
      if (edge.getFrom() == edge.getTo()) height -= heightPerLevel / 2;
      Point p1 = from.get(edge);
      if (p1 == null) System.out.println(edge);
      Point p2 = new Point(p1.x, height);
      Point p4 = to.get(edge);
      if (p4 == null) System.out.println(edges);
      Point p3 = new Point(p4.x, height);
      g2d.setStroke(getStroke(edge));
      //connection
      //GeneralPath shape = createRectArrow(p1, p2, p3, p4);
      GeneralPath shape = curve ? createCurveArrow(p1, p2, p3, p4) : createRectArrow(p1, p2, p3, p4);
      //GeneralPath shape = createCurveArrow(curveLength, p1, p2, p3, p4);
      g2d.draw(shape);
      //shape.intersects()
      //g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
      //g2d.drawLine(p2.x, p2.y, p3.x, p3.y);
      //g2d.drawLine(p3.x, p3.y, p4.x, p4.y);
      //arrow
      //g2d.setStroke(defaultStroke);
      g2d.drawLine(p4.x - arrowSize , p4.y - arrowSize , p4.x, p4.y);
      g2d.drawLine(p4.x + arrowSize , p4.y - arrowSize, p4.x, p4.y);

      //write label in the middle under
      Font font = new Font(g2d.getFont().getName(), Font.PLAIN, 8);
      FontRenderContext frc = g2d.getFontRenderContext();
      TextLayout layout = new TextLayout(edge.getLabel(), font, frc);
      int labelx = (int) (Math.min(p1.x, p3.x) + Math.abs(p1.x - p3.x) / 2 - layout.getBounds().getWidth() / 2);
      int labely = (int) (height + layout.getBounds().getHeight()) + 1;
      layout.draw(g2d, labelx, labely);
      g2d.setColor(old);
      //Area area = new Area();
      //area.add(shape);
      //shape.append(layout.getOutline(null), false);
      Rectangle2D labelBounds = layout.getBounds();
      shapes.put(shape, edge);
      //shapes.put(new Rectangle.Double(labelx,labely,labelBounds.getWidth(), labelBounds.getHeight()), edge);


    }


  }




  private GeneralPath createRectArrow(Point p1, Point p2, Point p3, Point p4) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(p1.x, p1.y);
    shape.lineTo(p2.x, p2.y);
    shape.lineTo(p3.x, p3.y);
    shape.lineTo(p4.x, p4.y);
    return shape;
  }

  private GeneralPath createCurveArrow(Point p1, Point p2, Point p3, Point p4) {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(p1.x, p1.y);
    shape.curveTo(p2.x, p2.y, p2.x, p2.y, p2.x + (p3.x - p2.x) / 2, p2.y);
    shape.curveTo(p3.x, p3.y, p3.x, p3.y, p4.x, p4.y);
    shape.moveTo(p3.x, p3.y);
    shape.closePath();
    return shape;
  }

  private GeneralPath createCurveArrow(int curveLength, Point p1, Point p2, Point p3, Point p4) {
    GeneralPath shape = new GeneralPath();
    Point c1 = p1;
    Point c2 = new Point(p2.x, p2.y + curveLength);
    Point c3 = new Point(p2.x + (p3.x > p2.x ? curveLength : -curveLength), p2.y);
    Point c4 = new Point(p3.x - (p3.x > p2.x ? curveLength : -curveLength), p2.y);
    Point c5 = new Point(p3.x, p3.y + curveLength);
    Point c6 = p4;

//    System.out.println("c1 = " + c1);
//    System.out.println("c2 = " + c2);
//    System.out.println("c3 = " + c3);
//    System.out.println("c4 = " + c4);
//    System.out.println("c5 = " + c5);
//    System.out.println("c6 = " + c6);

    shape.moveTo(c1.x, c1.y);
    shape.lineTo(c2.x, c2.y);
    shape.curveTo(p2.x, p2.y, p2.x, p2.y, c3.x, c3.y);
    shape.lineTo(c4.x, c4.y);
    shape.curveTo(p3.x, p3.y, p3.x, p3.y, c5.x, c5.y);
    shape.lineTo(c6.x, c6.y);
    return shape;
  }


  private int calculateDepth(HashMultiMapList<DependencyEdge, DependencyEdge> dominates,
                             Counter<DependencyEdge> depth,
                             DependencyEdge root) {
    if (depth.get(root) > 0) return depth.get(root);
    if (dominates.get(root).size() == 0) {
      return 0;
    }
    int max = 0;
    for (DependencyEdge children : dominates.get(root)) {
      int current = calculateDepth(dominates, depth, children);
      if (current > max) max = current;
    }
    depth.put(root, max + 1);
    return max + 1;

  }

  public Point getFrom(DependencyEdge edge) {
    return from.get(edge);
  }

  public Point getTo(DependencyEdge edge) {
    return to.get(edge);
  }

  public int getHeight() {
    return maxHeight;
  }

  public int getWidth() {
    return maxWidth;
  }

  public TextLayout getLabel() {
    return null;
  }


  public int getHeightPerLevel() {
    return heightPerLevel;
  }


  public boolean isCurve() {
    return curve;
  }

  public void setCurve(boolean curve) {
    this.curve = curve;
  }

  public void setBaseline(int baseline) {
    this.baseline = baseline;
  }

  public void setHeightPerLevel(int heightPerLevel) {
    this.heightPerLevel = heightPerLevel;
  }

  public void setArrowSize(int arrowSize) {
    this.arrowSize = arrowSize;
  }

  public void setVertexExtraSpace(int vertexExtraSpace) {
    this.vertexExtraSpace = vertexExtraSpace;
  }

  public int getVertexExtraSpace() {
    return vertexExtraSpace;
  }

  public int getArrowSize() {
    return arrowSize;
  }

  public int getBaseline() {
    return baseline;
  }
}
