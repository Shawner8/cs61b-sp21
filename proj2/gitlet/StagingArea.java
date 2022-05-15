package gitlet;

import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.*;

public class StagingArea {

    static final File STAGING_FOLDER = join(Repository.GITLET_DIR, "staging");

    static class AdditionArea {
        static final File ADDITION_FOLDER = join(STAGING_FOLDER, "addition");
        static final File ADDITIONMAP = join(ADDITION_FOLDER, "ADDITIONMAP");

        static void init() {
            ADDITION_FOLDER.mkdirs();
            TreeMap<String, String> additionMap = new TreeMap<>();
            writeObject(ADDITIONMAP, additionMap);
        }

        static TreeMap<String, String> getMap() {
            return readObject(ADDITIONMAP, TreeMap.class);
        }

        static boolean contains(String fileName) {
            TreeMap<String, String> additionMap = getMap();
            return additionMap.containsKey(fileName);
        }

        static boolean isEmpty() {
            TreeMap<String, String> additionMap = getMap();
            return additionMap.isEmpty();
        }

        static void add(Blob blob) {
            TreeMap<String, String> additionMap = getMap();
            if (additionMap.containsKey(blob.getFileName())) {
                remove(blob.getFileName());
            }
            additionMap.put(blob.getFileName(), blob.uid());
            writeObject(ADDITIONMAP, additionMap);
            File blobFile = join(ADDITION_FOLDER, blob.uid());
            writeObject(blobFile, blob);
        }

        static void remove(String fileName) {
            TreeMap<String, String> additionMap = getMap();
            String fileUID = additionMap.remove(fileName);
            writeObject(ADDITIONMAP, additionMap);
            if (fileUID != null) {
                join(ADDITION_FOLDER, fileUID).delete();
            }
        }

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

    static class RemovalArea {
        static final File REMOVALSET = join(STAGING_FOLDER, "REMOVALSET");

        static void init() {
            TreeSet<String> removalSet = new TreeSet<>();
            writeObject(REMOVALSET, removalSet);
        }

        static TreeSet<String> getSet() {
            return readObject(REMOVALSET, TreeSet.class);
        }

        static boolean contains(String fileName) {
            TreeSet<String> removalSet = getSet();
            return removalSet.contains(fileName);
        }

        static boolean isEmpty() {
            TreeSet<String> removalSet = getSet();
            return removalSet.isEmpty();
        }

        static void add(String fileName) {
            TreeSet<String> removalSet = getSet();
            removalSet.add(fileName);
            writeObject(REMOVALSET, removalSet);
        }

        static void remove(String fileName) {
            TreeSet<String> removalSet = getSet();
            removalSet.remove(fileName);
            writeObject(REMOVALSET, removalSet);
        }
    }

    static void init() {
        AdditionArea.init();
        RemovalArea.init();
    }

    static boolean isEmpty() {
        return AdditionArea.isEmpty() && RemovalArea.isEmpty();
    }
}
