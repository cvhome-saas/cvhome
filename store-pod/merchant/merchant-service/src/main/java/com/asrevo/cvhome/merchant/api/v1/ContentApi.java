package com.asrevo.cvhome.merchant.api.v1;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.model.content.ContentFile;
import com.asrevo.cvhome.merchant.content.model.content.ContentFolder;
import com.asrevo.cvhome.merchant.content.model.content.ContentName;
import com.asrevo.cvhome.merchant.content.model.content.PersistableContentEntity;
import com.asrevo.cvhome.merchant.content.model.content.box.PersistableContentBox;
import com.asrevo.cvhome.merchant.content.model.content.box.ReadableContentBox;
import com.asrevo.cvhome.merchant.content.model.content.box.ReadableContentBoxList;
import com.asrevo.cvhome.merchant.content.model.content.page.PersistableContentPage;
import com.asrevo.cvhome.merchant.content.model.content.page.ReadableContentPage;
import com.asrevo.cvhome.merchant.content.model.content.page.ReadableContentPageList;
import com.asrevo.cvhome.merchant.content.service.facade.content.ContentFacade;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Content management resource (Content Management Api)")
@Slf4j
public class ContentApi {

	private static final String BOX = "BOX";

	private static final String PAGE = "PAGE";

	private final ContentFacade contentFacade;

	public ContentApi(ContentFacade contentFacade) {
		this.contentFacade = contentFacade;
	}

	/**
	 * List content pages
	 */
	@GetMapping(value = "/private/content/pages", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get page names created for a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentPageList pages(@Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language, Pageable pageable) {
		return contentFacade.getContentPages(merchantStore, LanguageCode.nonLanguage(), pageable);
	}

	/**
	 * List content pages
	 */
	@GetMapping(value = "/content/pages", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get page names created for a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentPageList getPages(@Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language, Pageable pageable) {
		return contentFacade.getContentPages(merchantStore, language, pageable);
	}

	/**
	 * List all boxes
	 */
	@GetMapping(value = "/private/content/boxes", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get boxes for a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentBoxList boxes(@Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language, Pageable pageable) {
		return contentFacade.getContentBoxes(merchantStore, LanguageCode.nonLanguage(), pageable);
	}

	/**
	 * List all boxes
	 */
	@GetMapping(value = "/content/boxes", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get boxes for a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentBoxList getBoxes(@Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language, Pageable pageable) {
		return contentFacade.getContentBoxes(merchantStore, language, pageable);
	}

	/**
	 * List specific content page
	 */
	@GetMapping(value = "/private/content/pages/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get page content by code for a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableContentPage.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentPage page(@PathVariable("code") String code,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		return contentFacade.getContentPage(code, merchantStore, LanguageCode.allLanguage());
	}

	/**
	 * List specific content page
	 */
	@GetMapping(value = "/content/pages/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get page content by code for a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableContentPage.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentPage getPage(@PathVariable("code") String code,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		return contentFacade.getContentPage(code, merchantStore, language);
	}

	@GetMapping(value = "/private/content/boxes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Manage box content by code for a code and a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentBox box(@PathVariable("code") String code,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {
		return contentFacade.getContentBox(code, merchantStore, LanguageCode.allLanguage());
	}

	@GetMapping(value = "/content/boxes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get box content by code for a code and a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentBox getBox(@PathVariable("code") String code,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {
		return contentFacade.getContentBox(code, merchantStore, language);
	}

	/**
	 * Get content page by name
	 */
	@GetMapping(value = "/content/pages/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get page content by code for a given MerchantStore",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableContentPage.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableContentPage pageByName(@PathVariable("name") String name,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		return contentFacade.getContentPageByName(name, merchantStore, language);
	}

	/**
	 * Create content box
	 */
	@PostMapping(value = "/private/content/box")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(method = "POST", description = "Create content box",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Entity.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public Entity createBox(@RequestBody @Valid PersistableContentBox box,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		Long id = contentFacade.saveContentBox(box, merchantStore, language);
		Entity entity = new Entity();
		entity.setId(id);
		return entity;
	}

	@GetMapping(value = "/private/content/box/{code}/exists")
	@ResponseStatus(HttpStatus.OK)
	@Operation(method = "GET", description = "Check unique content box",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public EntityExists boxExists(@PathVariable String code, @Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {

		boolean exists = contentFacade.codeExist(code, BOX, merchantStore);
		return new EntityExists(exists);
	}

	@GetMapping(value = "/private/content/page/{code}/exists")
	@ResponseStatus(HttpStatus.OK)
	@Operation(method = "GET", description = "Check unique content page",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ResponseEntity.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public EntityExists pageExists(@PathVariable String code, @Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {

		boolean exists = contentFacade.codeExist(code, PAGE, merchantStore);
		return new EntityExists(exists);
	}

	/**
	 * Create content page
	 */
	@PostMapping(value = "/private/content/page")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(method = "POST", description = "Create content page",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Entity.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public Entity createPage(@RequestBody @Valid PersistableContentPage page,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		Long id = contentFacade.saveContentPage(page, merchantStore, language);
		Entity entity = new Entity();
		entity.setId(id);
		return entity;
	}

	@PutMapping(value = "/private/content/page/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(method = "PUT", description = "Update content page",
			responses = @ApiResponse(content = @Content(schema = @Schema())))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public void updatePage(@RequestBody @Valid PersistableContentPage page, @PathVariable Long id,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		contentFacade.updateContentPage(id, page, merchantStore, language);
	}

	@PutMapping(value = "/private/content/box/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(method = "PUT", description = "Update content box",
			responses = @ApiResponse(content = @Content(schema = @Schema())))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public void updateBox(@RequestBody @Valid PersistableContentBox box, @PathVariable Long id,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		contentFacade.updateContentBox(id, box, merchantStore, language);
	}

	/**
	 *
	 */
	@GetMapping(value = "/content/images", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get store content images",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ContentFolder.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	// @TODO create another one for private so seller-ui call it in private
	public ContentFolder images(@Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language,
			@RequestParam(value = "path", required = false) String path) throws Exception {

		return contentFacade.getContentFolder(path, merchantStore);
	}

	@PostMapping(value = "/private/files", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public void uploadMultipleFiles(@RequestParam(value = "files") MultipartFile[] files,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		for (MultipartFile f : files) {
			ContentFile cf = new ContentFile();
			cf.setContentType(f.getContentType());
			cf.setName(f.getOriginalFilename());
			try {
				cf.setFile(f.getBytes());
				contentFacade.addContentFile(cf, merchantStore.getId());
			}
			catch (IOException e) {
				throw new ServiceRuntimeException("Error while getting file bytes");
			}
		}
	}

	@Deprecated
	@PutMapping(value = "/private/content/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(method = "PUT", description = "Update content page", summary = "Updates a content page",
			responses = @ApiResponse(content = @Content(schema = @Schema())))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	// @TODO please check why this is used and not have body
	public void updatePage(@PathVariable Long id, @RequestBody @Valid PersistableContentEntity page,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {
		page.setId(id);
		// contentFacade.saveContentPage(page, merchantStore, language);
	}

	/**
	 * Deletes a content from CMS
	 */
	@Deprecated
	@DeleteMapping(value = "/private/content/{id}")
	@Operation(method = "DELETE", description = "Deletes a content from CMS", summary = "Delete a content box or page",
			responses = @ApiResponse(content = @Content(schema = @Schema())))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus
	public void deleteContent(Long id, @Parameter(hidden = true) StoreMerchantId merchantStore) {
		contentFacade.delete(merchantStore, id);
	}

	@DeleteMapping(value = "/private/content/")
	@Operation(method = "DELETE", description = "Deletes a file from CMS", summary = "Delete a file from server",
			responses = @ApiResponse(content = @Content(schema = @Schema())))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@ConditionalOnApiStatus
	public void deleteFile(@Valid ContentName name, @Parameter(hidden = true) StoreMerchantId merchantStore) {
		contentFacade.delete(merchantStore, name.getName(), name.getContentType());
	}

}
