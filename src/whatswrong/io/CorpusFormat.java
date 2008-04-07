package whatswrong.io;

import whatswrong.NLPInstance;

import javax.swing.*;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * @author Sebastian Riedel
 */

public interface CorpusFormat {

    String getName();

    JComponent getAccessory();

    List<NLPInstance> load(File file, int from, int to) throws IOException;

}
