package com.asrevo.cvhome.content.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.billing.guard.StoreEntitlements;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.media.MediaAsset;
import com.asrevo.cvhome.content.errors.InvalidMediaException;
import com.asrevo.cvhome.content.errors.MediaStorageException;
import com.asrevo.cvhome.content.model.MediaAssetView;
import com.asrevo.cvhome.content.model.MediaKind;
import com.asrevo.cvhome.content.model.MediaProcessingStatus;
import com.asrevo.cvhome.content.repository.MediaAssetRepository;

@Service
public class MediaAssetService {
    private static final String FILE_SIZE = "file-size";
    private static final String PDF_MIME = "application/pdf";
    private static final String SVG_MIME = "image/svg+xml";
    private static final long MAX_BYTES = 50L * 1024L * 1024L;
    private static final long MAX_PIXELS = 40_000_000L;
    private static final Set<String> ACCEPTED_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp", SVG_MIME, PDF_MIME);

    private final MediaAssetRepository repository;
    private final ObjectStorage storage;
    private final StoreEntitlements entitlements;
    private final Clock clock = Clock.systemUTC();
    private final Tika tika = new Tika();

    public MediaAssetService(MediaAssetRepository repository, ObjectStorage storage,
                             StoreEntitlements entitlements) {
        this.repository = repository;
        this.storage = storage;
        this.entitlements = entitlements;
    }

    @Transactional(rollbackFor = MediaStorageException.class)
    public MediaAssetView upload(StoreMerchantId store, String filename, InputStream input, long declaredSize)
            throws InvalidMediaException, MediaStorageException, EntitlementExceededException {
        if (declaredSize <= 0 || declaredSize > MAX_BYTES) {
            throw InvalidMediaException.because(FILE_SIZE);
        }
        Path temporary = null;
        try {
            temporary = Files.createTempFile("content-v2-media-", ".upload");
            Files.copy(input, temporary, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return storeUpload(store, filename, temporary);
        } catch (InvalidMediaException | EntitlementExceededException exception) {
            throw exception;
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw MediaStorageException.causedBy(exception);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Temporary cleanup failure must not hide the upload result.
                }
            }
        }
    }

    private MediaAssetView storeUpload(StoreMerchantId store, String filename, Path temporary)
            throws IOException, NoSuchAlgorithmException, InvalidMediaException, EntitlementExceededException {
        long size = Files.size(temporary);
        if (size <= 0 || size > MAX_BYTES) {
            throw InvalidMediaException.because(FILE_SIZE);
        }
        String checksum = checksum(temporary);
        Optional<MediaAsset> duplicate = repository
                .findByStoreMerchantIdAndChecksumAndDeletedAtIsNull(store, checksum);
        if (duplicate.isPresent()) {
            return toView(duplicate.orElseThrow());
        }
        enforceQuota(store, size);
        String mime = tika.detect(temporary);
        if (!ACCEPTED_MIME.contains(mime)) {
            throw InvalidMediaException.because("mime-type");
        }
        MediaAsset saved = repository.save(inspect(store, filename, temporary, mime, checksum, size));
        String key = "%s/assets/%s/%s/original".formatted(store.storeMerchantId(), saved.getId(), checksum);
        saved.setStorageKey(key);
        storage.put(key, temporary, mime);
        saved.setProcessingStatus(MediaProcessingStatus.READY);
        return toView(repository.save(saved));
    }

    @Transactional(readOnly = true)
    public List<MediaAssetView> list(StoreMerchantId store) {
        return repository.findAllByStoreMerchantIdAndDeletedAtIsNullOrderByAuditSectionDateCreatedDesc(store)
                .stream().map(this::toView).toList();
    }

    private void enforceQuota(StoreMerchantId store, long addedBytes) throws EntitlementExceededException {
        EntitlementValue limit = entitlements.snapshot(store).entitlement(EntitlementKey.MAX_STORAGE_MB);
        if (!limit.unlimited()) {
            long projectedBytes = repository.sumBytesByStore(store) + addedBytes;
            long projectedMegabytes = (projectedBytes + 1024L * 1024L - 1L) / (1024L * 1024L);
            if (projectedMegabytes > limit.limitValue()) {
                throw EntitlementExceededException.of(store, EntitlementKey.MAX_STORAGE_MB,
                        limit.limitValue(), Math.toIntExact(projectedMegabytes));
            }
        }
    }

    private MediaAsset inspect(StoreMerchantId store, String filename, Path file, String mime, String checksum,
                               long size) throws IOException, InvalidMediaException {
        MediaAsset asset = new MediaAsset();
        asset.setStoreMerchantId(store);
        asset.setOriginalFilename(filename);
        asset.setNormalizedFilename(normalizeFilename(filename));
        asset.setDetectedMime(mime);
        asset.setByteSize(size);
        asset.setChecksum(checksum);
        asset.setStorageKey("pending/%s".formatted(checksum));
        asset.setProcessingStatus(MediaProcessingStatus.PROCESSING);
        asset.getAuditSection().setDateCreated(clock.instant());
        asset.getAuditSection().setDateModified(clock.instant());
        if (PDF_MIME.equals(mime)) {
            inspectPdf(asset, file);
            return asset;
        }
        inspectImage(asset, file, mime);
        return asset;
    }

    private static void inspectPdf(MediaAsset asset, Path file) throws IOException {
        asset.setMediaKind(MediaKind.DOCUMENT);
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            asset.setPageCount(document.getNumberOfPages());
        }
    }

    private static void inspectImage(MediaAsset asset, Path file, String mime)
            throws IOException, InvalidMediaException {
        asset.setMediaKind(MediaKind.IMAGE);
        if (SVG_MIME.equals(mime)) {
            validateSvg(file);
            asset.setWidth(1);
            asset.setHeight(1);
            return;
        }
        BufferedImage image = ImageIO.read(file.toFile());
        if (image == null || (long) image.getWidth() * image.getHeight() > MAX_PIXELS) {
            throw InvalidMediaException.because("image-dimensions");
        }
        asset.setWidth(image.getWidth());
        asset.setHeight(image.getHeight());
    }

    private static void validateSvg(Path file) throws InvalidMediaException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.newDocumentBuilder().parse(file.toFile());
            String source = Files.readString(file).toLowerCase(Locale.ROOT);
            if (source.contains("<script") || source.contains("javascript:") || source.contains(" onload=")) {
                throw InvalidMediaException.because("unsafe-svg");
            }
        } catch (InvalidMediaException exception) {
            throw exception;
        } catch (Exception exception) {
            throw InvalidMediaException.because("invalid-svg");
        }
    }

    private static String normalizeFilename(String filename) {
        String leaf = Path.of(filename == null ? "upload" : filename).getFileName().toString();
        return leaf.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String checksum(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private MediaAssetView toView(MediaAsset asset) {
        List<MediaAssetView.MediaVariantView> variants = asset.getVariants().stream()
                .map(it -> new MediaAssetView.MediaVariantView(it.getVariantName(), it.getFormat(), it.getWidth(),
                        it.getHeight(), it.getByteSize()))
                .toList();
        return new MediaAssetView(asset.getId(), asset.getOriginalFilename(), asset.getDetectedMime(),
                asset.getMediaKind(), asset.getByteSize(), asset.getChecksum(), asset.getWidth(), asset.getHeight(),
                asset.getPageCount(), asset.getProcessingStatus(), variants);
    }
}
