package gitlet;

import java.io.File;

import static gitlet.Utils.*;

public class BlobArea {

    static final File BLOB_FOLDER = join(Repository.GITLET_DIR, "blobs");

    static void init() {
        BLOB_FOLDER.mkdirs();
    }
}
