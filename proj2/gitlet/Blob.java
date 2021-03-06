package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/** Represents a gitlet file object.
 *  contains 3 fields:
 *    fileName, fileContent and UID.
 *
 *  @author Shawn
 */
public class Blob implements Serializable {

    private String fileName;
    private String fileContent;
    private String uid;

    public Blob(File fileObj) {
        fileName = fileObj.getName();
        fileContent = readContentsAsString(fileObj);
    }

    public Blob(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    String getFileName() {
        return fileName;
    }

    String getFileContent() {
        return fileContent;
    }

    /* Lazy cache. */
    String getUid() {
        if (uid == null) {
            uid = sha1(toString());
        }
        return uid;
    }

    @Override
    public String toString() {
        return String.format("%s:\n%s", fileName, fileContent);
    }
}
