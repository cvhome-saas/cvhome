package com.asrevo.cvhome.domaincertificatemanager.repository;

import com.asrevo.cvhome.domaincertificatemanager.entity.FileEntity;
import com.asrevo.cvhome.domaincertificatemanager.entity.FileId;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface FilesRepository extends ListCrudRepository<FileEntity, FileId> {
    Optional<FileEntity> findFirstByPath(String path);
}
