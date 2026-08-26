package com.asrevo.cvhome.content.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.MediaAsset;
import com.asrevo.cvhome.content.entity.MediaFolder;
import com.asrevo.cvhome.content.entity.MediaQuota;
import com.asrevo.cvhome.content.entity.MediaUsageRow;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.errors.MediaLimitException;
import com.asrevo.cvhome.content.errors.MediaStorageException;
import com.asrevo.cvhome.content.model.MediaKind;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.model.media.MediaUsage;
import com.asrevo.cvhome.content.model.media.PersistableMediaAsset;
import com.asrevo.cvhome.content.model.media.ReadableMediaAsset;
import com.asrevo.cvhome.content.model.media.ReadableMediaAssetList;
import com.asrevo.cvhome.content.model.summary.ContentSummary;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.MediaAssetRepository;
import com.asrevo.cvhome.content.repository.MediaFolderRepository;
import com.asrevo.cvhome.content.repository.MediaQuotaRepository;
import com.asrevo.cvhome.content.repository.MediaUsageRepository;
import com.asrevo.cvhome.content.storage.ImageProbe;
import com.asrevo.cvhome.content.storage.MediaStorage;
import com.asrevo.cvhome.content.storage.SvgSanitizer;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.content.support.Strings;

import lombok.RequiredArgsConstructor;

/**
 * The media library: upload through the service to object storage (no presigned URLs on this platform),
 * sha-256 deduplication per store, a per-store byte quota, folders, editable metadata, and the usage index that
 * makes "used on 3 pages" / "unused" and the 409 on delete possible.
 */
@Service
@RequiredArgsConstructor
public class MediaService implements SummaryService.MediaFigures {

    private static final String JPEG = "image/jpeg";

    private static final String PNG = "image/png";

    private static final String WEBP = "image/webp";

    private static final String GIF = "image/gif";

    private static final String SVG = "image/svg+xml";

    private static final String MP4 = "video/mp4";

    private static final String WEBM = "video/webm";

    private static final String PDF = "application/pdf";

    private static final String ZIP = "application/zip";

    private static final String PENDING = "pending";

    private static final String DASH = "-";

    private static final java.util.regex.Pattern NON_SLUG = java.util.regex.Pattern.compile("[^a-z0-9]+");

    public static final Set<String> ACCEPTED = Set.of(JPEG, PNG, WEBP, GIF, SVG, MP4, WEBM, PDF, ZIP,
            "application/x-zip-compressed");

    /**
     * Extension → type, for browsers that send {@code application/octet-stream} or nothing.
     */
    private static final Map<String, String> BY_EXTENSION = Map.ofEntries(
            Map.entry(".jpg", JPEG), Map.entry(".jpeg", JPEG), Map.entry(".png", PNG), Map.entry(".webp", WEBP),
            Map.entry(".gif", GIF), Map.entry(".svg", SVG), Map.entry(".mp4", MP4), Map.entry(".webm", WEBM),
            Map.entry(".pdf", PDF), Map.entry(".zip", ZIP));

    private static final List<String[]> DEFAULT_FOLDERS = List.of(
            new String[] {"banners", "Banners"}, new String[] {"products", "Product shots"},
            new String[] {"brand", "Brand assets"}, new String[] {"video", "Video"}, new String[] {"docs", "Documents"});

    private static final String ID = "id";

    private final MediaAssetRepository assets;

    private final MediaFolderRepository folders;

    private final MediaUsageRepository usage;

    private final MediaQuotaRepository quotas;

    private final ContentRepository contents;

    private final MediaStorage storage;

    private final Clock clock;

    /**
     * An upload as the API hands it over: the original name, the declared type and the bytes.
     */
    public record Upload(String filename, String contentType, byte[] bytes) {
    }

    // ------------------------------------------------------------------------------------------------ uploads

    @Transactional(rollbackFor = Exception.class)
    public List<ReadableMediaAsset> upload(StoreMerchantId store, List<Upload> uploads, Long folderId,
                                           long maxFileBytes, long quotaBytes, String actor)
            throws InvalidContentRequestException, MediaLimitException, MediaStorageException,
            ContentNotFoundException {
        if (folderId != null) {
            folders.findByIdAndStoreMerchantId(folderId, store.getId())
                    .orElseThrow(() -> ContentNotFoundException.mediaFolder(folderId, store));
        }
        MediaQuota quota = quota(store);
        List<ReadableMediaAsset> out = new ArrayList<>();
        for (Upload u : uploads) {
            String mime = normaliseMime(u.filename(), u.contentType());
            if (!ACCEPTED.contains(mime)) {
                throw InvalidContentRequestException.mediaTypeNotAllowed(u.filename(), mime);
            }
            byte[] bytes = SVG.equals(mime) ? SvgSanitizer.clean(u.bytes()) : u.bytes();
            if (bytes.length > maxFileBytes) {
                throw MediaLimitException.tooLarge(u.filename(), bytes.length, maxFileBytes);
            }
            String checksum = sha256(bytes);
            Optional<MediaAsset> existing = assets.findByStoreMerchantIdAndChecksum(store.getId(), checksum);
            if (existing.isPresent()) {
                out.add(toReadable(existing.get(), (int) usage.countByAssetId(existing.get().getId()), null));
                continue;
            }
            if (quota.getBytesUsed() + bytes.length > quotaBytes) {
                throw MediaLimitException.quotaExceeded(quota.getBytesUsed(), quotaBytes, bytes.length);
            }
            MediaAsset a = new MediaAsset();
            a.setStoreMerchantId(store.getId());
            a.setFolderId(folderId);
            a.setOriginalFilename(u.filename());
            a.setFilename(safeFilename(u.filename()));
            a.setMimeType(mime);
            a.setKind(MediaKind.ofMimeType(mime));
            a.setBytes(bytes.length);
            a.setChecksum(checksum);
            a.setUploadedBy(actor);
            a.setUploadedAt(clock.instant());
            if (a.getKind() == MediaKind.IMAGE) {
                ImageProbe.dimensions(bytes).ifPresent(d -> {
                    a.setWidth(d.width());
                    a.setHeight(d.height());
                });
            }
            // key needs the id, so save a placeholder key first
            a.setStorageKey(PENDING);
            a.setPublicUrl(PENDING);
            MediaAsset saved = assets.saveAndFlush(a);
            String key = MediaStorage.key(store.getId(), saved.getId(), saved.getFilename());
            storage.put(key, bytes, mime);
            saved.setStorageKey(key);
            saved.setPublicUrl(storage.url(key));
            saved = assets.saveAndFlush(saved);
            quota.setBytesUsed(quota.getBytesUsed() + bytes.length);
            quota.setFileCount(quota.getFileCount() + 1);
            out.add(toReadable(saved, 0, null));
        }
        quotas.save(quota);
        return out;
    }

    // -------------------------------------------------------------------------------------------------- reads

    @Transactional(readOnly = true)
    public ReadableMediaAssetList list(StoreMerchantId store, Long folderId, MediaKind kind, String q, Boolean used,
                                       Pageable pageable) {
        Specification<MediaAsset> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("storeMerchantId"), store.getId()));
            if (folderId != null) {
                p.add(cb.equal(root.get("folderId"), folderId));
            }
            if (kind != null) {
                p.add(cb.equal(root.get("kind"), kind));
            }
            if (!Strings.blank(q)) {
                String like = String.format("%%%s%%", q.trim().toLowerCase(Locale.ROOT));
                p.add(cb.or(cb.like(cb.lower(root.get("filename")), like),
                        cb.like(cb.lower(root.get("originalFilename")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("title"), "")), like)));
            }
            if (used != null) {
                Subquery<Long> sub = query.subquery(Long.class);
                var u = sub.from(MediaUsageRow.class);
                sub.select(u.get(ID)).where(cb.equal(u.get("assetId"), root.get(ID)));
                p.add(used ? cb.exists(sub) : cb.not(cb.exists(sub)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        Page<MediaAsset> page = assets.findAll(spec, pageable.getSort().isSorted() ? pageable
                : org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                                "uploadedAt")));
        Map<Long, Integer> counts = usageCounts(page.getContent().stream().map(MediaAsset::getId).toList());
        ReadableMediaAssetList out = new ReadableMediaAssetList();
        out.setTotalPages(page.getTotalPages());
        out.setSize(page.getNumberOfElements());
        out.setTotalElements(page.getTotalElements());
        out.setRecordsFiltered(page.getNumberOfElements());
        out.setPageNumber(page.getNumber());
        List<ReadableMediaAsset> rows = new ArrayList<>();
        for (MediaAsset a : page.getContent()) {
            rows.add(toReadable(a, counts.getOrDefault(a.getId(), 0), null));
        }
        out.setContent(rows);
        return out;
    }

    @Transactional(readOnly = true)
    public ReadableMediaAsset get(StoreMerchantId store, Long id) throws ContentNotFoundException {
        MediaAsset a = load(store, id);
        List<MediaUsage> uses = usageOf(a.getId());
        return toReadable(a, uses.size(), uses);
    }

    @Transactional(readOnly = true)
    public List<MediaUsage> usage(StoreMerchantId store, Long id) throws ContentNotFoundException {
        return usageOf(load(store, id).getId());
    }

    /**
     * The assets of this store among {@code ids}, for a caller that holds ids and needs the whole record. An id
     * belonging to another store is simply absent, which is what lets a caller use this as its ownership check.
     */
    @Transactional(readOnly = true)
    public List<ReadableMediaAsset> assets(StoreMerchantId store, List<Long> ids) {
        List<Long> wanted = ids == null ? List.of()
                : ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (wanted.isEmpty()) {
            return List.of();
        }
        return assets.findByStoreMerchantIdAndIdIn(store.getId(), wanted).stream()
                .map(a -> toReadable(a, 0, List.of()))
                .toList();
    }

    /**
     * Public URLs for a set of asset ids of this store — what bindings use to resolve artwork and hero images.
     */
    @Transactional(readOnly = true)
    public Map<Long, String> urls(StoreMerchantId store, List<Long> ids) {
        List<Long> wanted = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, String> out = new HashMap<>();
        if (wanted.isEmpty()) {
            return out;
        }
        for (MediaAsset a : assets.findByStoreMerchantIdAndIdIn(store.getId(), wanted)) {
            out.put(a.getId(), a.getPublicUrl());
        }
        return out;
    }

    public Optional<String> url(StoreMerchantId store, Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return assets.findByIdAndStoreMerchantId(id, store.getId()).map(MediaAsset::getPublicUrl);
    }

    // ------------------------------------------------------------------------------------------------- writes

    @Transactional(rollbackFor = Exception.class)
    public ReadableMediaAsset patch(StoreMerchantId store, Long id, PersistableMediaAsset body)
            throws ContentNotFoundException {
        MediaAsset a = load(store, id);
        if (body.getFolderId() != null) {
            folders.findByIdAndStoreMerchantId(body.getFolderId(), store.getId())
                    .orElseThrow(() -> ContentNotFoundException.mediaFolder(body.getFolderId(), store));
            a.setFolderId(body.getFolderId());
        }
        if (body.getAltTexts() != null) {
            a.setAltTexts(JsonCodec.write(body.getAltTexts()));
        }
        if (body.getTitle() != null) {
            a.setTitle(Strings.trimToNull(body.getTitle()));
        }
        if (body.getTags() != null) {
            a.setTags(JsonCodec.write(body.getTags()));
        }
        a = assets.saveAndFlush(a);
        List<MediaUsage> uses = usageOf(a.getId());
        return toReadable(a, uses.size(), uses);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(StoreMerchantId store, Long id, boolean force)
            throws ContentNotFoundException, ContentConflictException, MediaStorageException {
        MediaAsset a = load(store, id);
        List<MediaUsage> uses = usageOf(a.getId());
        if (!uses.isEmpty() && !force) {
            throw ContentConflictException.mediaReferenced(id, uses);
        }
        usage.deleteByAssetId(a.getId());
        storage.delete(a.getStorageKey());
        assets.delete(a);
        MediaQuota quota = quota(store);
        quota.setBytesUsed(Math.max(0, quota.getBytesUsed() - a.getBytes()));
        quota.setFileCount(Math.max(0, quota.getFileCount() - 1));
        quotas.save(quota);
    }

    // ------------------------------------------------------------------------------------------------ folders

    @Transactional(rollbackFor = Exception.class)
    public List<com.asrevo.cvhome.content.model.media.MediaFolder> folders(StoreMerchantId store) {
        ensureDefaultFolders(store);
        List<com.asrevo.cvhome.content.model.media.MediaFolder> out = new ArrayList<>();
        for (MediaFolder f : folders.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId())) {
            out.add(toFolder(f, assets.countByStoreMerchantIdAndFolderId(store.getId(), f.getId())));
        }
        return out;
    }

    @Transactional(rollbackFor = Exception.class)
    public com.asrevo.cvhome.content.model.media.MediaFolder createFolder(
            StoreMerchantId store, com.asrevo.cvhome.content.model.media.MediaFolder body) {
        String key = Strings.blank(body.getKey()) ? slugify(body.getName()) : body.getKey();
        MediaFolder f = folders.findByStoreMerchantIdAndKey(store.getId(), key).orElseGet(MediaFolder::new);
        f.setStoreMerchantId(store.getId());
        f.setKey(key);
        f.setName(body.getName().trim());
        f.setPosition(body.getPosition() != null ? body.getPosition() : 100);
        f = folders.saveAndFlush(f);
        return toFolder(f, assets.countByStoreMerchantIdAndFolderId(store.getId(), f.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public com.asrevo.cvhome.content.model.media.MediaFolder renameFolder(
            StoreMerchantId store, Long id, com.asrevo.cvhome.content.model.media.MediaFolder body)
            throws ContentNotFoundException {
        MediaFolder f = folders.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.mediaFolder(id, store));
        f.setName(body.getName().trim());
        if (body.getPosition() != null) {
            f.setPosition(body.getPosition());
        }
        f = folders.saveAndFlush(f);
        return toFolder(f, assets.countByStoreMerchantIdAndFolderId(store.getId(), f.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(StoreMerchantId store, Long id, Long moveTo)
            throws ContentNotFoundException, ContentConflictException {
        MediaFolder f = folders.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.mediaFolder(id, store));
        long count = assets.countByStoreMerchantIdAndFolderId(store.getId(), id);
        if (count > 0) {
            if (moveTo == null) {
                throw ContentConflictException.folderNotEmpty(id, count);
            }
            folders.findByIdAndStoreMerchantId(moveTo, store.getId())
                    .orElseThrow(() -> ContentNotFoundException.mediaFolder(moveTo, store));
            assets.moveFolder(store.getId(), id, moveTo);
        }
        folders.delete(f);
    }

    // ------------------------------------------------------------------------------------------------ summary

    @Override
    @Transactional(readOnly = true)
    public void contribute(StoreMerchantId store, ContentSummary summary, Map<String, Long> counts) {
        MediaQuota q = quotas.findById(store.getId()).orElse(null);
        if (q != null) {
            summary.getMedia().setBytesUsed(q.getBytesUsed());
            summary.getMedia().setFileCount(q.getFileCount());
            counts.put("media", q.getFileCount());
        }
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private MediaAsset load(StoreMerchantId store, Long id) throws ContentNotFoundException {
        return assets.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.media(id, store));
    }

    private MediaQuota quota(StoreMerchantId store) {
        return quotas.findById(store.getId()).orElseGet(() -> {
            MediaQuota q = new MediaQuota();
            q.setStoreMerchantId(store.getId());
            return q;
        });
    }

    private void ensureDefaultFolders(StoreMerchantId store) {
        if (!folders.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId()).isEmpty()) {
            return;
        }
        int position = 0;
        for (String[] def : DEFAULT_FOLDERS) {
            MediaFolder f = new MediaFolder();
            f.setStoreMerchantId(store.getId());
            f.setKey(def[0]);
            f.setName(def[1]);
            f.setPosition(position++);
            f.setSystem(true);
            folders.save(f);
        }
    }

    private Map<Long, Integer> usageCounts(List<Long> ids) {
        Map<Long, Integer> out = new HashMap<>();
        if (ids.isEmpty()) {
            return out;
        }
        for (Object[] row : usage.countByAssetIds(ids)) {
            out.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return out;
    }

    /**
     * The owners of an asset. A content-owned row resolves its title from the item; every other kind uses the
     * title its owner supplied when it registered, so this never calls out to another service.
     */
    private List<MediaUsage> usageOf(Long assetId) {
        List<MediaUsage> out = new ArrayList<>();
        for (MediaUsageRow r : usage.findByAssetId(assetId)) {
            // A null kind is a row written before the column existed; the DDL defaults those to CONTENT.
            MediaOwnerKind kind = r.getOwnerKind() == null ? MediaOwnerKind.CONTENT : r.getOwnerKind();
            String title = kind.local() && r.getContentId() != null
                    ? contents.findById(r.getContentId()).map(c -> ContentMapper.title(c, null)).orElse(null)
                    : r.getOwnerTitle();
            out.add(new MediaUsage(kind, r.getOwnerRef(), r.getContentType(), r.getContentId(), title,
                    r.getField()));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    static ReadableMediaAsset toReadable(MediaAsset a, int usageCount, List<MediaUsage> uses) {
        ReadableMediaAsset r = new ReadableMediaAsset();
        r.setId(a.getId());
        r.setFilename(a.getFilename());
        r.setOriginalFilename(a.getOriginalFilename());
        r.setMimeType(a.getMimeType());
        r.setKind(a.getKind());
        r.setBytes(a.getBytes());
        r.setWidth(a.getWidth());
        r.setHeight(a.getHeight());
        r.setUrl(a.getPublicUrl());
        r.setFolderId(a.getFolderId());
        r.setAltTexts(a.getAltTexts() == null ? Map.of() : JsonCodec.read(a.getAltTexts(), LinkedHashMap.class));
        r.setTitle(a.getTitle());
        r.setTags(a.getTags() == null ? List.of() : JsonCodec.read(a.getTags(), List.class));
        r.setUploadedAt(a.getUploadedAt());
        r.setUploadedBy(a.getUploadedBy());
        r.setUsageCount(usageCount);
        r.setUsage(uses);
        return r;
    }

    static com.asrevo.cvhome.content.model.media.MediaFolder toFolder(MediaFolder f, long count) {
        var out = new com.asrevo.cvhome.content.model.media.MediaFolder();
        out.setId(f.getId());
        out.setName(f.getName());
        out.setKey(f.getKey());
        out.setPosition(f.getPosition());
        out.setSystem(f.isSystem());
        out.setFileCount(count);
        return out;
    }

    static String normaliseMime(String filename, String declared) {
        String mime = declared == null ? "" : declared.toLowerCase(Locale.ROOT).split(";")[0].trim();
        if (ACCEPTED.contains(mime)) {
            return mime;
        }
        String name = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        String byExtension = dot < 0 ? null : BY_EXTENSION.get(name.substring(dot));
        if (byExtension != null) {
            return byExtension;
        }
        return mime.isEmpty() ? "application/octet-stream" : mime;
    }

    static String safeFilename(String original) {
        String name = original == null ? "file" : original;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[^A-Za-z0-9._-]", DASH).replaceAll("-{2,}", DASH);
        if (name.isBlank() || name.startsWith(".")) {
            name = String.format("file%s", name);
        }
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }

    static String slugify(String s) {
        String out = s == null ? "" : NON_SLUG.matcher(s.trim().toLowerCase(Locale.ROOT)).replaceAll(DASH)
                .replaceAll("^-|-$", "");
        return out.isEmpty() ? "folder" : out;
    }

    static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
