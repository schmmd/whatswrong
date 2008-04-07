package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class NLPInstance {

  private List<DependencyEdge> dependencies = new ArrayList<DependencyEdge>();
  private List<TokenVertex> tokens = new ArrayList<TokenVertex>();

  public void addDependency(int from, int to, String label, String type){
    dependencies.add(new DependencyEdge(tokens.get(from),tokens.get(to),label,type));
  }

  public void addDependency(TokenVertex from, TokenVertex to, String label, String type){
    addDependency(from.getIndex(),to.getIndex(),label,type);
  }

  public void addTokens(Collection<TokenVertex> tokens){
    this.tokens.addAll(tokens);
  }

  public void addDependencies(Collection<DependencyEdge> dependencies){
    this.dependencies.addAll(dependencies);
  }

  public void merge(NLPInstance nlp){
    addDependencies(nlp.dependencies);
    for (int i = 0; i < Math.min(tokens.size(), nlp.tokens.size()); ++i){
      tokens.get(i).merge(nlp.tokens.get(i));
    }
  }

  public void addTokenWithProperties(String ... properties){
    TokenVertex token = new TokenVertex(tokens.size());
    for (String property : properties) token.addProperty(property);
    tokens.add(token);
  }

  public TokenVertex addToken(){
    TokenVertex vertex = new TokenVertex(tokens.size());
    tokens.add(vertex);
    return vertex;
  }

  public List<DependencyEdge> getDependencies(){
    return Collections.unmodifiableList(dependencies);
  }

  public List<TokenVertex> getTokens(){
    return Collections.unmodifiableList(tokens);
  }




}
