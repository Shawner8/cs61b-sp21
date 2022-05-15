package gitlet;

import java.io.File;

import static gitlet.Utils.*;

public class BlobArea {

    static final File BLOB_FOLDER = join(Repository.GITLET_DIR, "blobs");

    static void init() {
        BLOB_FOLDER.mkdirs();
    }

    static void save(Blob blob) {
        File blobFile = join(BLOB_FOLDER, blob.uid());
        writeObject(blobFile, blob);
    }
}
