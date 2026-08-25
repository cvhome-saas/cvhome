package com.asrevo.cvhome.content.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.errors.MediaStorageException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Keys are prefixed per store so one seller's media can never collide with another's, and the real content type
 * is written so browsers render PDFs and videos rather than downloading them.
 */
class MediaStorageTest {

    private static final String PUBLIC_URL = "https://cdn.test/files/s1/media/7/logo.png";

    private static final String PNG_TYPE = "image/png";

    private static final String DENIED = "denied";

    private static final String BUCKET = "pod-media";

    private static final String KEY = "files/s1/media/7/logo.png";

    private S3Client s3;

    private MediaStorage storage;

    @BeforeEach
    void setUp() {
        s3 = mock(S3Client.class);
        storage = new MediaStorage(s3, BUCKET, "https://cdn.test/");
    }

    @Test
    void theKeyIsPrefixedPerStoreAndCarriesTheAssetId() {
        assertThat(MediaStorage.key("s1", 7L, "logo.png")).isEqualTo(KEY);
    }

    @Test
    void aTrailingSlashOnTheBasePathIsNotDoubled() {
        assertThat(storage.url(KEY)).isEqualTo(PUBLIC_URL);
        assertThat(new MediaStorage(s3, BUCKET, "https://cdn.test").url(KEY))
                .isEqualTo(PUBLIC_URL);
    }

    @Test
    void anUploadCarriesTheRealContentType() throws Exception {
        storage.put(KEY, new byte[] {1, 2}, PNG_TYPE);

        var request = org.mockito.ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(request.getValue().key()).isEqualTo(KEY);
        assertThat(request.getValue().contentType()).isEqualTo(PNG_TYPE);
    }

    @Test
    void aRefusedUploadBecomesATypedStorageFailure() {
        when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message(DENIED).build());

        assertThatThrownBy(() -> storage.put(KEY, new byte[] {1}, PNG_TYPE))
                .isInstanceOf(MediaStorageException.class)
                .hasMessageContaining("upload");
    }

    @Test
    void aRefusedDeleteBecomesATypedStorageFailure() {
        when(s3.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message(DENIED).build());

        assertThatThrownBy(() -> storage.delete(KEY))
                .isInstanceOf(MediaStorageException.class)
                .hasMessageContaining("delete");
    }

    @Test
    void aDeleteNamesTheBucketAndKey() throws Exception {
        storage.delete(KEY);

        var request = org.mockito.ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3).deleteObject(request.capture());
        assertThat(request.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(request.getValue().key()).isEqualTo(KEY);
    }

}
