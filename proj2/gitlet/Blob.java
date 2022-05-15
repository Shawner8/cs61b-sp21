package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {

    private String fileName;
    private String fileContents;
    private String uid;

    public Blob(File fileObj) {
        fileName = fileObj.getName();
        fileContents = readContentsAsString(fileObj);
    }

    String getFileName() {
        return fileName;
    }

    String uid() {
        if (uid == null) {
            uid = sha1(toString());
        }
        return uid;
    }

    @Override
    public String toString() {
        return String.format("%s:\n%s", fileName, fileContents);
    }
}
