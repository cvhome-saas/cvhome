package com.asrevo.cvhome.merchant.api.v1;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.commons.annotation.ApiUsage;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.ReadableSliderImage;
import com.asrevo.cvhome.commons.domain.SliderImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;
import com.asrevo.cvhome.store.controller.exception.RestApiException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Merchant and store management resource (Merchant - Store Management Api)")
@Slf4j
@AllArgsConstructor
public class MerchantStoreApi {

	private final StoreFacade storeFacade;

	private final ImageFilePath imageFilePath;

	@GetMapping(value = { "/store/{code}" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get merchant store",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
	@Parameters({ @Parameter(name = "lang",
			schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableMerchantStore store(@PathVariable String code,
			@RequestParam(value = "lang", required = false) String lang) {
		return storeFacade.getByMerchantStoreId(new StoreMerchantId(code), new LanguageCode(lang));
	}

	@GetMapping(value = { "/private/store" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get merchant store full details",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
	@Parameters({ @Parameter(name = "lang",
			schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableMerchantStore storeFull(@SecuredResource StoreMerchantId merchantStore, LanguageCode language) {
		return storeFacade.getByMerchantStoreId(merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/store/languages" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "GET", description = "Get list of store supported languages.",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
	@ConditionalOnApiStatus
	public List<LanguageCode> supportedLanguages(StoreMerchantId merchantStore) {

		return storeFacade.supportedLanguages(merchantStore);
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping(value = { "/private/store" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "POST", description = "Creates a new store",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
	@ConditionalOnApiStatus
	@PreAuthorize("hasPermission(#store.org,'ManagerOrgId','STORE.CREATE')")
	public void create(@Valid @RequestBody PersistableMerchantStore store) {
		storeFacade.create(store);
	}

	@ResponseStatus(HttpStatus.OK)
	@PutMapping(value = { "/private/store" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "PUT", description = "Updates a store",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus(usage = ApiUsage.USED)
	public void update(@SecuredResource StoreMerchantId merchantStore,
			@Valid @RequestBody PersistableMerchantStore store) {
		storeFacade.update(store);
	}

	@ResponseStatus(HttpStatus.OK)
	@PutMapping(value = { "/private/store/social-links" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(method = "PUT", description = "Updates store social links",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus(usage = ApiUsage.USED)
	public void updateSocialLinks(@SecuredResource StoreMerchantId merchantStore,
			@RequestBody PersistableMerchantStore store) {
		storeFacade.updateSocialLinks(merchantStore, store.getSocialLinks());
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value = { "/private/store/marketing/logo" })
	@Operation(method = "POST", description = "Add store logo")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus
	public void addLogo(@SecuredResource StoreMerchantId merchantStore,
			@RequestParam("file") MultipartFile uploadfile) {

		InputContentFile cmsContentImage = createInputContentFile(uploadfile, FileContentType.LOGO);
		storeFacade.addStoreLogo(merchantStore, cmsContentImage);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value = { "/private/store/marketing/banner" })
	@Operation(method = "POST", description = "Add store banner")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus
	public void addBanner(@SecuredResource StoreMerchantId merchantStore,
			@RequestParam("file") MultipartFile uploadfile) {

		InputContentFile cmsContentImage = createInputContentFile(uploadfile, FileContentType.BANNER);
		storeFacade.addStoreBanner(merchantStore, cmsContentImage);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value = { "/private/store/marketing/add-slider-image" })
	@Operation(method = "POST", description = "Add image")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus
	public ReadableSliderImage addSliderImage(@SecuredResource StoreMerchantId merchantStore,
			@RequestParam("file") MultipartFile file) {

		InputContentFile cmsContentImage = createInputContentFile(file, FileContentType.SLIDER);
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		String newFileName = UUID.randomUUID() + "." + extension;
		cmsContentImage.setFileName(newFileName);
		SliderImage sliderImage = storeFacade.addStoreSliderImage(merchantStore, cmsContentImage);
		return new ReadableSliderImage(sliderImage.priority(), sliderImage.name(),
				imageFilePath.buildStoreSliderFilePath(merchantStore, newFileName));
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PutMapping(value = { "/private/store/marketing/slider-images" })
	@Operation(method = "PUT", description = "Save slider images with its order")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus
	public void sliderImages(@SecuredResource StoreMerchantId merchantStore,
			@RequestBody PersistableMerchantStore store) {
		storeFacade.updateSliderImages(merchantStore, store.getSliderImages());
	}

	private InputContentFile createInputContentFile(MultipartFile image, FileContentType contentType) {

		InputContentFile cmsContentImage;

		try {

			InputStream input = new ByteArrayInputStream(image.getBytes());
			cmsContentImage = new InputContentFile();
			cmsContentImage.setFileName(image.getOriginalFilename());
			cmsContentImage.setMimeType(image.getContentType());
			cmsContentImage.setFileContentType(contentType);
			cmsContentImage.setFile(input);

		}
		catch (IOException ioe) {
			throw new RestApiException(ioe);
		}

		return cmsContentImage;
	}

	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping(value = { "/private/store" })
	@Operation(method = "DELETE", description = "Deletes a store",
			responses = @ApiResponse(content = @Content(schema = @Schema())))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus
	public void delete(@SecuredResource StoreMerchantId merchantStore) {
		storeFacade.delete(merchantStore);
	}

}
