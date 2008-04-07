package whatswrong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sebastian Riedel
 */
public class DependencyTypeFilter implements DependencyFilter {

  private HashSet<String> forbiddenTypes = new HashSet<String>();

  public DependencyTypeFilter(String ... forbiddenTypes){
    for (String type : forbiddenTypes) this.forbiddenTypes.add(type);
  }

  public void addForbiddenType(String type){
    forbiddenTypes.add(type);
  }

  public void removeForbiddenType(String type){
    forbiddenTypes.remove(type);
  }

  public DependencyTypeFilter(Set<String> forbiddenTypes) {
    this.forbiddenTypes.addAll(forbiddenTypes);
  }

  public Collection<DependencyEdge> filter(Collection<DependencyEdge> original) {
    ArrayList<DependencyEdge> result = new ArrayList<DependencyEdge>(original.size());
    main:
    for (DependencyEdge edge : original){
      for (String type : forbiddenTypes)
        if (edge.getType().contains(type)) continue main;
      result.add(edge);
    }
    return result;

  }

  public boolean forbids(String type) {
    return forbiddenTypes.contains(type);
  }
}
