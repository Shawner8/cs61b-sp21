package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/** Represents a gitlet HEAD object.
 *
 *  @author Shawn
 */
public class Head implements Serializable {

    static final File HEAD = join(Repository.GITLET_DIR, "HEAD");

    static void set(String commitUID) {
        writeContents(HEAD, commitUID);
    }

    /** Get the uid of the current head commit. */
    static String get() {
        return readContentsAsString(HEAD);
    }

    /** Load the current commit referenced by HEAD. */
    static Commit load() {
        File commitFile = join(Commit.COMMIT_FOLDER, get());
        return readObject(commitFile, Commit.class);
    }
}
