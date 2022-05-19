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

    String getName() {
        return name;
    }

    /** Write the branch into the folder "branches/". */
    void save() {
        File branch = join(BRANCH_FOLDER, name);
        writeContents(branch, reference);
    }

    /** Return the commit at the head of the branch. */
    static Commit load(String branchName) {
        File branchFile = join(BRANCH_FOLDER, branchName);
        String commitUID = readContentsAsString(branchFile);
        File commitFile = join(Commit.COMMIT_FOLDER, commitUID);
        return readObject(commitFile, Commit.class);
    }

    /** Write the branch into the file "BRANCH".
     *  set the branch to be the current branch.
     */
    static void set(String branchName) {
        writeContents(BRANCH, branchName);
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

    static boolean contains(String branchName) {
        return join(BRANCH_FOLDER, branchName).exists();
    }

    static void remove(String branchName) {
        File branchFile = join(BRANCH_FOLDER, branchName);
        branchFile.delete();
    }
}
