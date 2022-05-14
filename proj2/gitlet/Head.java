package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Head implements Serializable {

    static final File HEAD = Utils.join(Repository.GITLET_DIR, "HEAD");

    static void set(String commitUID) {
        writeContents(HEAD, commitUID);
    }

    /** Load the current commit referenced by HEAD. */
    static Commit load() {
        String commitUID = readContentsAsString(HEAD);
        File commitFile = join(Commit.COMMIT_FOLDER, commitUID);
        return readObject(commitFile, Commit.class);
    }
}
