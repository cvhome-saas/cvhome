package com.asrevo.cvhome.domaincertificatemanager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("files")
public class FileEntity extends BaseEntity<FileEntity, FileId> {
    private String path;
    private String content;

    public static FileEntity create(String fileName, String content) {
        FileEntity fileEntity = new FileEntity(fileName, content);
        fileEntity.setNew();
        return fileEntity;
    }

    @Override
    protected FileId generateId() {
        return FileId.newId();
    }

    public static String resolveFileUri(String basePath, String fileName, String ext) {
        return "file://" + basePath + "/" + fileName + ext;
    }
}