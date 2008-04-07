package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class TokenPropertyFilter implements TokenFilter {

  private HashSet<TokenProperty> forbiddenProperties = new HashSet<TokenProperty>();


  public TokenPropertyFilter() {
  }

  public void addForbiddenProperty(String name){
    forbiddenProperties.add(new TokenProperty(name));
  }

  public void removeForbiddenProperty(String name){
    forbiddenProperties.remove(new TokenProperty(name));
  }

  public Set<TokenProperty> getForbiTokenProperties(){
    return Collections.unmodifiableSet(forbiddenProperties);
  }

  public Collection<TokenVertex> filter(Collection<TokenVertex> original) {
    ArrayList<TokenVertex> result = new ArrayList<TokenVertex>(original.size());
    for (TokenVertex vertex : original) {
      TokenVertex copy = new TokenVertex(vertex.getIndex());
      for (TokenProperty property : vertex.getPropertyTypes()) {
        if (!forbiddenProperties.contains(property))
          copy.addProperty(property,vertex.getProperty(property));
      }
      result.add(copy);
    }
    return result;
  }
}
