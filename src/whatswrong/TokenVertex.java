package whatswrong;

import java.util.*;

/**
 * @author Sebastian Riedel
 */
public class TokenVertex {
  private int index;
  private HashMap<TokenProperty,String> tokenProperties = new HashMap<TokenProperty, String>();

  public TokenVertex(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  public Collection<TokenProperty> getPropertyTypes(){
    return Collections.unmodifiableCollection(tokenProperties.keySet());
  }



  public String getProperty(TokenProperty property){
    return tokenProperties.get(property);
  }

  public void removeProperty(int index){
    tokenProperties.remove(new TokenProperty(index));
  }

  public void removeProperty(String name){
    tokenProperties.remove(new TokenProperty(name));
  }

  public TokenVertex addProperty(String name, String value){
    tokenProperties.put(new TokenProperty(name,tokenProperties.size()),value);
    return this;
  }

  public void addProperty(int index, String property){
    tokenProperties.put(new TokenProperty(index),property);
  }

  public TokenVertex addProperty(TokenProperty property, String value){
    tokenProperties.put(property, value);
    return this;
  }

  public void addProperty(String property){
    addProperty(tokenProperties.size(),property);
  }

  public List<TokenProperty> getSortedProperties(){
    ArrayList<TokenProperty> sorted = new ArrayList<TokenProperty>(tokenProperties.keySet());
    Collections.sort(sorted);
    return sorted;
  }

  public Collection<String> getProperties(){
    return Collections.unmodifiableCollection(tokenProperties.values());
  }

  public boolean propertiesContain(String substring){
    for (String property : tokenProperties.values())
      if (property.contains(substring)) return true;
    return false;
  }

  public boolean propertiesContain(Collection<String> substrings){
    for (String property : tokenProperties.values())
      for (String substring: substrings)
      if (property.contains(substring)) return true;
    return false;

  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TokenVertex that = (TokenVertex) o;

    return index == that.index;

  }

  public int hashCode() {
    return index;
  }

  public void merge(TokenVertex tokenVertex) {
    tokenProperties.putAll(tokenVertex.tokenProperties);
  }
}
