package com.asrevo.cvhome.store.core.modules.cms.content;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.OutputContentFile;
import com.asrevo.cvhome.store.core.modules.cms.common.AssetsManager;

public interface ContentAssetsManager
        extends AssetsManager, FileGet, FilePut, FileRemove, FolderPut, FolderList, FolderRemove, Serializable {

    char UNIX_SEPARATOR = '/';

    char WINDOWS_SEPARATOR = '\\';

    String ROOT_NAME = "files";

    default String nodePath(String store, FileContentType type) {

        StringBuilder builder = new StringBuilder();
        String root = nodePath(store);
        builder.append(root);
        if (type != null && !FileContentType.IMAGE.name().equals(type.name())
                && !FileContentType.STATIC_FILE.name().equals(type.name())) {
            builder.append(type.name()).append(Constants.SLASH);
        }

        return builder.toString();
    }

    default String nodePath(String store) {

        StringBuilder builder = new StringBuilder();
        builder.append(ROOT_NAME).append(Constants.SLASH).append(store).append(Constants.SLASH);
        return builder.toString();
    }

    default OutputContentFile getOutputContentFile(byte[] byteArray) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(byteArray.length);
        baos.write(byteArray, 0, byteArray.length);
        OutputContentFile ct = new OutputContentFile();
        ct.setFile(baos);
        return ct;
    }

    default boolean isInsideSubFolder(String key) {
        int c = StringUtils.countMatches(key, Constants.SLASH);
        return c > 2;
    }

    default String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    default int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

}
