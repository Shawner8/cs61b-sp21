package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Branch implements Serializable {

    static final File BRANCH_FOLDER = join(Repository.GITLET_DIR, "branches");
    static final File BRANCH = join(Repository.GITLET_DIR, "BRANCH");

    private String reference;
    private String name;

    public Branch(String ref, String n) {
        reference = ref;
        name = n;
    }

    /** Write the branch into the folder "branches/". */
    void save() {
        File branch = join(BRANCH_FOLDER, name);
        writeContents(branch, reference);
    }

    /** Write the branch into the file "BRANCH".
     *  set the branch to be the current branch.
     */
    void set() {
        writeContents(BRANCH, name);
    }

    /** Return the name of the current branch. */
    static String get() {
        return readContentsAsString(BRANCH);
    }

    /** Update the reference of the current branch. */
    static void update(String ref) {
        String name = readContentsAsString(BRANCH);
        File currentBranch = join(BRANCH_FOLDER, name);
        writeContents(currentBranch, ref);
    }
}
