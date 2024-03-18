package com.asrevo.cvhome.dcm.repository;

import com.asrevo.cvhome.dcm.entity.FileEntity;
import com.asrevo.cvhome.dcm.entity.FileId;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface FilesRepository extends ListCrudRepository<FileEntity, FileId> {
    Optional<FileEntity> findFirstByPath(String path);
}
