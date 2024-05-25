package com.asrevo.cvhome.store.controller.v1.content;


import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.content.ContentType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.content.*;
import com.asrevo.cvhome.store.core.model.content.box.PersistableContentBox;
import com.asrevo.cvhome.store.core.model.content.box.ReadableContentBox;
import com.asrevo.cvhome.store.core.model.content.page.PersistableContentPage;
import com.asrevo.cvhome.store.core.model.content.page.ReadableContentPage;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.service.facade.content.ContentFacade;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;


@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Content management resource (Content Management Api)")
@Slf4j
public class ContentApi {


    private static final String DEFAULT_PATH = "/";

    private final static String BOX = "BOX";
    private final static String PAGE = "PAGE";

    private final ContentFacade contentFacade;

    private final ImageFilePath imageUtils;

    public ContentApi(ContentFacade contentFacade, ImageFilePath imageUtils) {
        this.contentFacade = contentFacade;
        this.imageUtils = imageUtils;
    }

    /**
     * List content pages
     *
     * @param merchantStore
     * @param language
     * @param page
     * @param count
     * @return
     */
    @GetMapping(value = {"/private/content/pages", "/content/pages"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get page names created for a given MerchantStore", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = List.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableEntityList<ReadableContentPage> pages(
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            int page,
            int count) {
        return contentFacade
                .getContentPages(merchantStore, language, page, count);
    }

    @Deprecated
    @GetMapping(value = "/content/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get pages summary created for a given MerchantStore. Content summary is a content bux having code summary.", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public List<ReadableContentBox> pagesSummary(
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        //return contentFacade.getContentBoxes(ContentType.BOX, "summary_", merchantStore, language);
        return null;
    }

    /**
     * List all boxes
     *
     * @param merchantStore
     * @param language
     * @return
     */
    @GetMapping(value = {"/content/boxes", "/private/content/boxes"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get boxes for a given MerchantStore", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = List.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableEntityList<ReadableContentBox> boxes(
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            int page,
            int count
    ) {
        return contentFacade.getContentBoxes(ContentType.BOX, merchantStore, language, page, count);
    }

    /**
     * List specific content box
     *
     * @param code
     * @param merchantStore
     * @param language
     * @return
     */
    @GetMapping(value = "/content/pages/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get page content by code for a given MerchantStore", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ReadableContentPage.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableContentPage page(@PathVariable("code") String code, @Parameter(hidden = true) MerchantStore merchantStore,
                                    @Parameter(hidden = true) Language language) {

        return contentFacade.getContentPage(code, merchantStore, language);

    }

    /**
     * Get content page by name
     *
     * @param name
     * @param merchantStore
     * @param language
     * @return
     */
    @GetMapping(value = "/content/pages/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get page content by code for a given MerchantStore", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ReadableContentPage.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableContentPage pageByName(@PathVariable("name") String name, @Parameter(hidden = true) MerchantStore merchantStore,
                                          @Parameter(hidden = true) Language language) {

        return contentFacade.getContentPageByName(name, merchantStore, language);

    }

    /**
     * Create content box
     *
     * @param page
     * @param merchantStore
     * @param language
     * @param pageCode
     */
    @PostMapping(value = "/private/content/box")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(method = "POST", description = "Create content box", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Entity.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public Entity createBox(
            @RequestBody @Valid PersistableContentBox box,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        Long id = contentFacade.saveContentBox(box, merchantStore, language);
        Entity entity = new Entity();
        entity.setId(id);
        return entity;
    }

    @GetMapping(value = "/private/content/box/{code}/exists")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "GET", description = "Check unique content box", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public EntityExists boxExists(
            @PathVariable String code,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        boolean exists = contentFacade.codeExist(code, BOX, merchantStore);
        EntityExists entity = new EntityExists(exists);
        return entity;
    }

    @GetMapping(value = "/private/content/page/{code}/exists")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "GET", description = "Check unique content page", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ResponseEntity.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public EntityExists pageExists(
            @PathVariable String code,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        boolean exists = contentFacade.codeExist(code, PAGE, merchantStore);
        EntityExists entity = new EntityExists(exists);
        return entity;
    }

    /**
     * Create content page
     *
     * @param page
     * @param merchantStore
     * @param language
     */
    @PostMapping(value = "/private/content/page")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(method = "POST", description = "Create content page", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Entity.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public Entity createPage(
            @RequestBody @Valid PersistableContentPage page,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        Long id = contentFacade.saveContentPage(page, merchantStore, language);
        Entity entity = new Entity();
        entity.setId(id);
        return entity;
    }


    /**
     * Delete content page
     *
     * @param id
     * @param merchantStore
     * @param language
     */
    @DeleteMapping(value = "/private/content/page/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "DELETE", description = "Delete content page", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deletePage(
            @PathVariable Long id,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        contentFacade.delete(merchantStore, id);

    }

    /**
     * Delete content box
     *
     * @param id
     * @param merchantStore
     * @param language
     */
    @DeleteMapping(value = "/private/content/box/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "DELETE", description = "Delete content box", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deleteBox(
            @PathVariable Long id,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        contentFacade.delete(merchantStore, id);

    }

    @PutMapping(value = "/private/content/page/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "PUT", description = "Update content page", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updatePage(
            @RequestBody @Valid PersistableContentPage page,
            @PathVariable Long id,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        contentFacade.updateContentPage(id, page, merchantStore, language);
    }

    @PutMapping(value = "/private/content/box/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "PUT", description = "Update content box", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updateBox(
            @RequestBody @Valid PersistableContentBox box,
            @PathVariable Long id,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        contentFacade.updateContentBox(id, box, merchantStore, language);
    }

    @Deprecated
    @GetMapping(value = "/private/content/any/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get page content by code for a given MerchantStore", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ReadableContentPage.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableContentFull content(@PathVariable("code") String code, @Parameter(hidden = true) MerchantStore merchantStore,
                                       @Parameter(hidden = true) Language language) {

        return contentFacade.getContent(code, merchantStore, language);

    }

    @Deprecated
    @GetMapping(value = "/private/contents/any", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get contents (page and box) for a given MerchantStore", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ReadableContentPage.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public List<ReadableContentEntity> contents(@Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        Optional<String> op = Optional.empty();
        return contentFacade.getContents(op, merchantStore, language);

    }


    @GetMapping(value = "/private/content/boxes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Manage box content by code for a code and a given MerchantStore", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = List.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableContentBox manageBoxByCode(@PathVariable("code") String code, @Parameter(hidden = true) MerchantStore merchantStore,
                                              @Parameter(hidden = true) Language language) {
        return contentFacade.getContentBox(code, merchantStore, language);
    }

    @GetMapping(value = "/content/boxes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get box content by code for a code and a given MerchantStore", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = List.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableContentBox getBoxByCode(@PathVariable("code") String code, @Parameter(hidden = true) MerchantStore merchantStore,
                                           @Parameter(hidden = true) Language language) {
        return contentFacade.getContentBox(code, merchantStore, language);
    }


    /**
     * @param parent
     * @param folder
     * @param merchantStore
     * @param language
     */
    @DeleteMapping(value = "/content/folder", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void addFolder(@RequestParam String parent, @RequestParam String folder,
                          @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

    }

    /**
     * @param code
     * @param path
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/content/images", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get store content images", summary = "", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ContentFolder.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ContentFolder images(@Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language,
                                @RequestParam(value = "path", required = false) String path, HttpServletRequest request,
                                HttpServletResponse response) throws Exception {

        //String decodedPath = decodeContentPath(path);
        ContentFolder folder = contentFacade.getContentFolder(path, merchantStore);
        return folder;
    }


    /**
     * Need type, name and entity
     *
     * @param file
     */
    @PostMapping(value = "/private/file")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void upload(@RequestParam("file") MultipartFile file, @Parameter(hidden = true) MerchantStore merchantStore,
                       @Parameter(hidden = true) Language language) {

        ContentFile f = new ContentFile();
        f.setContentType(file.getContentType());
        f.setName(file.getOriginalFilename());
        try {
            f.setFile(file.getBytes());
        } catch (IOException e) {
            throw new ServiceRuntimeException("Error while getting file bytes");
        }

        contentFacade.addContentFile(f, merchantStore.getCode());

    }

    @PostMapping(value = "/private/files", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void uploadMultipleFiles(@RequestParam(value = "files") MultipartFile[] files,
                                    @Parameter(hidden = true) MerchantStore merchantStore,
                                    @Parameter(hidden = true) Language language) {

        for (MultipartFile f : files) {
            ContentFile cf = new ContentFile();
            cf.setContentType(f.getContentType());
            cf.setName(f.getOriginalFilename());
            try {
                cf.setFile(f.getBytes());
                contentFacade.addContentFile(cf, merchantStore.getCode());
            } catch (IOException e) {
                throw new ServiceRuntimeException("Error while getting file bytes");
            }
        }

    }


    @Deprecated
    @PutMapping(value = "/private/content/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "PUT", description = "Update content page", summary = "Updates a content page",

            responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updatePage(@PathVariable Long id, @RequestBody @Valid PersistableContentEntity page,
                           @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {
        page.setId(id);
        //contentFacade.saveContentPage(page, merchantStore, language);
    }

    /**
     * Deletes a content from CMS
     *
     * @param name
     */
    @Deprecated
    @DeleteMapping(value = "/private/content/{id}")
    @Operation(method = "DELETE", description = "Deletes a content from CMS", summary = "Delete a content box or page", responses = @ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
    })
    public void deleteContent(Long id, @Parameter(hidden = true) MerchantStore merchantStore) {
        contentFacade.delete(merchantStore, id);
    }

    /*  *//**
     * Deletes a content from CMS
     *
     * @param name
     *//*
     * @DeleteMapping(value = "/private/content/page/{id}")
     *
     * @Operation(method = "DELETE", value =
     * "Deletes a file from CMS", summary = "Delete a file from server",
     * responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Void.class))))
     *
     * @ApiImplicitParams({
     *
     * @ApiImplicitParam(name = "store", dataType = "String",
     * defaultValue = "DEFAULT")}) public void deleteFile( Long id,
     *
     * @Parameter(hidden = true) MerchantStore merchantStore) {
     * contentFacade.deletePage(merchantStore, id); }
     */

    /**
     * Deletes a file from CMS
     *
     * @param name
     */
    @DeleteMapping(value = "/private/content/")
    @Operation(method = "DELETE", description = "Deletes a file from CMS", summary = "Delete a file from server", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deleteFile(@Valid ContentName name, @Parameter(hidden = true) MerchantStore merchantStore,
                           @Parameter(hidden = true) Language language) {
        contentFacade.delete(merchantStore, name.getName(), name.getContentType());
    }


}