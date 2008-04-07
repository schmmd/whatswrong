package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class DependencyTokenFilter implements DependencyFilter {

  private HashSet<String> allowedProperties = new HashSet<String>();
  private boolean usePaths = false;

  public DependencyTokenFilter(String... allowedProperties) {
    for (String type : allowedProperties) this.allowedProperties.add(type);
  }


  public boolean isUsePaths() {
    return usePaths;
  }

  public void setUsePaths(boolean usePaths) {
    this.usePaths = usePaths;
  }

  public void addAllowedProperty(String type) {
    allowedProperties.add(type);
  }

  public void removeAllowedProperty(String type) {
    allowedProperties.remove(type);
  }

  public DependencyTokenFilter(Set<String> forbiddenTypes) {
    this.allowedProperties.addAll(forbiddenTypes);
  }

  public void clear() {
    allowedProperties.clear();
  }

  private static class Path extends HashSet<DependencyEdge> {

  }

  private static class Paths extends HashMap<TokenVertex, HashMap<TokenVertex, HashSet<Path>>> {
    public Set<Path> getPaths(TokenVertex from, TokenVertex to) {
      HashMap<TokenVertex, HashSet<Path>> paths = get(from);
      return paths == null ? null : paths.get(to);
    }

    public Set<TokenVertex> getTos(TokenVertex from) {
      HashMap<TokenVertex, HashSet<Path>> result = get(from);
      return result != null ? result.keySet() : new HashSet<TokenVertex>();
    }

    public void addPath(TokenVertex from, TokenVertex to, Path path) {
      HashMap<TokenVertex, HashSet<Path>> paths = get(from);
      if (paths == null) {
        paths = new HashMap<TokenVertex, HashSet<Path>>();
        put(from, paths);
      }
      HashSet<Path> set = paths.get(to);
      if (set == null) {
        set = new HashSet<Path>();
        paths.put(to, set);
      }
      set.add(path);
    }


  }

  private Paths calculatePaths(Collection<DependencyEdge> edges) {
    List<Paths> pathsPerLength = new ArrayList<Paths>();

    Paths paths = new Paths();
    //initialize
    for (DependencyEdge edge : edges) {
      Path path = new Path();
      path.add(edge);
      paths.addPath(edge.getFrom(), edge.getTo(), path);
      paths.addPath(edge.getTo(), edge.getFrom(), path);
    }
    pathsPerLength.add(paths);
    Paths previous = paths;
    Paths first = paths;
    do {
      paths = new Paths();
      //go over each paths of the previous length and increase their size by one
      for (TokenVertex from : previous.keySet())
        for (TokenVertex over : previous.getTos(from))
          for (TokenVertex to : first.getTos(over))
            for (Path path1 : previous.getPaths(from, over))
              for (Path path2 : first.getPaths(over, to)) {
                if (!path1.containsAll(path2) &&
                        path1.iterator().next().getTypePrefix().equals(path2.iterator().next().getTypePrefix())) {
                  Path path = new Path();
                  path.addAll(path1);
                  path.addAll(path2);
                  paths.addPath(from, to, path);
                  paths.addPath(to, from, path);
                }
              }
      if (!paths.isEmpty()) pathsPerLength.add(paths);
      previous = paths;
    } while (paths.size() > 0);
    Paths result = new Paths();
    for (Paths p : pathsPerLength)
      for (TokenVertex from : p.keySet())
        for (TokenVertex to : p.getTos(from))
          for (Path path : p.getPaths(from, to))
            result.addPath(from, to, path);
    return result;
  }

  public Collection<DependencyEdge> filter(Collection<DependencyEdge> original) {
    if (allowedProperties.size() == 0) return original;
    if (usePaths) {
      Paths paths = calculatePaths(original);
      HashSet<DependencyEdge> result = new HashSet<DependencyEdge>();
      for (TokenVertex from : paths.keySet())
        if (from.propertiesContain(allowedProperties))
          for (TokenVertex to : paths.getTos(from))
            if (to.propertiesContain(allowedProperties))
              for (Path path : paths.getPaths(from, to))
                result.addAll(path);
      return result;
    } else {
      ArrayList<DependencyEdge> result = new ArrayList<DependencyEdge>(original.size());
      main:
      for (DependencyEdge edge : original)
        for (String allowed : allowedProperties) {
          for (String property : edge.getFrom().getProperties())
            if (property.contains(allowed)) {
              result.add(edge);
              continue main;
            }
          for (String property : edge.getTo().getProperties())
            if (property.contains(allowed)) {
              result.add(edge);
              continue main;
            }
        }
      return result;
    }

  }

  public boolean allows(String type) {
    return allowedProperties.contains(type);
  }
}
