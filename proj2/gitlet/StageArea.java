package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;

public class StageArea {

    static final File STAGE_FOLDER = join(Repository.GITLET_DIR, "stage");
    static final File STAGEMAP = join(STAGE_FOLDER, "STAGEMAP");

    static void init() {
        STAGE_FOLDER.mkdirs();
        HashMap<String, String> stageMap = new HashMap<>();
        writeObject(STAGEMAP, stageMap);
    }

    static void add(Blob blob) {
        HashMap<String, String> stageMap = readObject(STAGEMAP, HashMap.class);
        if (stageMap.containsKey(blob.getFileName())) {
            remove(blob.getFileName());
        }
        stageMap.put(blob.getFileName(), blob.getUid());
        File blobFile = join(STAGE_FOLDER, blob.getUid());
        writeObject(blobFile, blob);
    }

    static void remove(String fileName) {
        HashMap<String, String> stageMap = readObject(STAGEMAP, HashMap.class);
        String fileUID = stageMap.remove(fileName);
        if (fileUID != null) {
            restrictedDelete(join(STAGE_FOLDER, fileUID));
        }
    }
}
