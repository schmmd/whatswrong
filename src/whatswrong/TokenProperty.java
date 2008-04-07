package whatswrong;

/**
 * @author Sebastian Riedel
 */
public class TokenProperty implements Comparable<TokenProperty> {
  private final String name;
  private final int level;

  public TokenProperty(String name, int level) {
    this.name = name;
    this.level = level;
  }


  public TokenProperty(String name) {
    this(name, 0);
  }


  public String toString() {
    return name;
  }

  public TokenProperty(int level) {
    this("Property " + level, level);
  }

  public String getName() {
    return name;
  }

  public int getLevel() {
    return level;
  }


  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TokenProperty that = (TokenProperty) o;

    if (name != null ? !name.equals(that.name) : that.name != null) return false;

    return true;
  }

  public int hashCode() {
    return (name != null ? name.hashCode() : 0);
  }

  public int compareTo(TokenProperty o) {
    return level != o.level ? level - o.level : name.compareTo(o.name);
  }
}
