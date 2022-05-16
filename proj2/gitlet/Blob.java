package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {

    private String fileName;
    private String fileContent;
    private String uid;

    public Blob(File fileObj) {
        fileName = fileObj.getName();
        fileContent = readContentsAsString(fileObj);
    }

    String getFileName() {
        return fileName;
    }

    String getFileContent() {
        return fileContent;
    }

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
