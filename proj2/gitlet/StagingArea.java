package gitlet;

import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.*;

/** An abstraction of the staging area represented by the STAGING_FOLDER.
 *  Because we don't need to instantiate any BlobArea objects,
 *  so all the methods in this class are static methods.
 *
 *  @author Shawn
 */
public class StagingArea {

    static final File STAGING_FOLDER = join(Repository.GITLET_DIR, "staging");

    /** Initialize the staging area. */
    static void init() {
        AdditionArea.init();
        RemovalArea.init();
    }

    /** Clear all the things saved in the staging area and reinitialize. */
    static void clear() {
        for (String blobUID : plainFilenamesIn(AdditionArea.ADDITION_FOLDER)) {
            File blobFile = join(AdditionArea.ADDITION_FOLDER, blobUID);
            blobFile.delete();
        }
        init();
    }

    /** Return whether the staging area is empty. */
    static boolean isEmpty() {
        return AdditionArea.isEmpty() && RemovalArea.isEmpty();
    }

    /** An abstraction of the staging area for addition represented by the ADDITION_FOLDER.
     *  Because we don't need to instantiate any BlobArea objects,
     *  so all the methods in this class are static methods.
     */
    static class AdditionArea {
        static final File ADDITION_FOLDER = join(STAGING_FOLDER, "addition");
        /** Addition map saves the map between the file name and the UID of the file. */
        static final File ADDITIONMAP = join(ADDITION_FOLDER, "ADDITIONMAP");

        /** Initialize the staging area for addition. */
        static void init() {
            ADDITION_FOLDER.mkdirs();
            TreeMap<String, String> additionMap = new TreeMap<>();
            writeObject(ADDITIONMAP, additionMap);
        }

        static TreeMap<String, String> getMap() {
            return readObject(ADDITIONMAP, TreeMap.class);
        }

        /** Return whether the staging area for addition contains the file with name "fileName". */
        static boolean contains(String fileName) {
            TreeMap<String, String> additionMap = getMap();
            return additionMap.containsKey(fileName);
        }

        /** Return whether the staging area for addition is empty. */
        static boolean isEmpty() {
            TreeMap<String, String> additionMap = getMap();
            return additionMap.isEmpty();
        }

        /** Add the blob into the staging area for addition. */
        static void add(Blob blob) {
            TreeMap<String, String> additionMap = getMap();
            if (additionMap.containsKey(blob.getFileName())) {
                remove(blob.getFileName());
            }
            additionMap.put(blob.getFileName(), blob.getUid());
            writeObject(ADDITIONMAP, additionMap);
            File blobFile = join(ADDITION_FOLDER, blob.getUid());
            writeObject(blobFile, blob);
        }

        /** Remove the file from the staging area for addition. */
        static void remove(String fileName) {
            TreeMap<String, String> additionMap = getMap();
            String fileUID = additionMap.remove(fileName);
            writeObject(ADDITIONMAP, additionMap);
            if (fileUID != null) {
                join(ADDITION_FOLDER, fileUID).delete();
            }
        }

        /** Move all the blobs saved in the staging area for addition into the Blob folder. */
        static void moveToBlobs() {
            TreeMap<String, String> additionMap = getMap();
            for (String fileRef : additionMap.values()) {
                File blobFile = join(ADDITION_FOLDER, fileRef);
                Blob blob = readObject(blobFile, Blob.class);
                blobFile.delete();
                BlobArea.save(blob);
            }
        }
    }

    /** An abstraction of the staging area for removal.
     *  Because we don't need to instantiate any BlobArea objects,
     *  so all the methods in this class are static methods.
     */
    static class RemovalArea {
        /** Removal set saves all the files waiting to be removed from the commit. */
        static final File REMOVALSET = join(STAGING_FOLDER, "REMOVALSET");

        /** Initialize the staging area for removal. */
        static void init() {
            TreeSet<String> removalSet = new TreeSet<>();
            writeObject(REMOVALSET, removalSet);
        }

        static TreeSet<String> getSet() {
            return readObject(REMOVALSET, TreeSet.class);
        }

        /** Return whether the staging area for removal contains the file with name "fileName". */
        static boolean contains(String fileName) {
            TreeSet<String> removalSet = getSet();
            return removalSet.contains(fileName);
        }

        /** Return whether the staging area for removal is empty. */
        static boolean isEmpty() {
            TreeSet<String> removalSet = getSet();
            return removalSet.isEmpty();
        }

        /** Add the file into the staging area for removal. */
        static void add(String fileName) {
            TreeSet<String> removalSet = getSet();
            removalSet.add(fileName);
            writeObject(REMOVALSET, removalSet);
        }

        /** Remove the file from the staging area for removal. */
        static void remove(String fileName) {
            TreeSet<String> removalSet = getSet();
            removalSet.remove(fileName);
            writeObject(REMOVALSET, removalSet);
        }
    }
}
