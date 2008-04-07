package whatswrong;

import java.util.Collection;

/**
 * @author Sebastian Riedel
 */
public interface TokenFilter {
  Collection<TokenVertex> filter(Collection<TokenVertex> original);
}
