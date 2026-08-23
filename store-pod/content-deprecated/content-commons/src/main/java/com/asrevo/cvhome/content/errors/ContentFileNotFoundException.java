package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * The named file does not exist in this store's CMS folder.
 *
 * <p>
 * Raised by rename, where the source file has to be read before it can be re-created under a new name. It was a
 * {@code ServiceException} and therefore a 500, which told the seller the platform had broken when in fact they had
 * asked to rename something that is not there.
 * </p>
 */
public class ContentFileNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ContentFileNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ContentFileNotFoundException of(String fileName, String merchantStoreCode) {
        return new ErrorBuilder<>(ContentErrors.FILE_NOT_FOUND, ContentFileNotFoundException::new)
                .detail("No file named %s in store %s.", fileName, merchantStoreCode)
                .param("fileName", fileName)
                .param("store", merchantStoreCode)
                .build();
    }

}
