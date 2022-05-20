package gitlet;

import java.io.File;

import static gitlet.Utils.*;

/** An abstraction of the folder which contains all blob files.
 *  Because we don't need to instantiate any BlobArea objects,
 *  so all the methods in this class are static methods.
 *
 *  @author Shawn
 */
public class BlobArea {

    static final File BLOB_FOLDER = join(Repository.GITLET_DIR, "blobs");

    /** Initialize the Blob folder. */
    static void init() {
        BLOB_FOLDER.mkdirs();
    }

    /** Save the blob object into the Blob folder. */
    static void save(Blob blob) {
        File blobFile = join(BLOB_FOLDER, blob.getUid());
        writeObject(blobFile, blob);
    }

    /** Load the blob object from the Blob folder. */
    static Blob load(String blobUID) {
        File blobFile = join(BLOB_FOLDER, blobUID);
        return readObject(blobFile, Blob.class);
    }
}
