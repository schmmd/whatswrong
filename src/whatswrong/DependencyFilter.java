package whatswrong;

import java.util.Collection;

/**
 * @author Sebastian Riedel
 */
public interface DependencyFilter {
  Collection<DependencyEdge> filter(Collection<DependencyEdge> original);
}
