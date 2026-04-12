package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturerList;
import com.asrevo.cvhome.catalog.service.facade.manufacturer.ManufacturerFacade;
import com.asrevo.cvhome.catalog.services.product.manufacturer.ManufacturerService;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Manufacturer management Collection, Manufacturer ...
 *
 * @author c.samson
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Manufacturer / Brand management resource (Manufacturer / Brand Management Api)")
@Slf4j
public class ProductManufacturerApi {

	private final ManufacturerService manufacturerService;

	private final ManufacturerFacade manufacturerFacade;

	public ProductManufacturerApi(ManufacturerService manufacturerService, ManufacturerFacade manufacturerFacade) {
		this.manufacturerService = manufacturerService;
		this.manufacturerFacade = manufacturerFacade;
	}

	/**
	 * Method for creating a manufacturer
	 */
	@RequestMapping(value = "/private/manufacturer", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)

	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public PersistableManufacturer create(@Valid @RequestBody PersistableManufacturer manufacturer,
			StoreMerchantId merchantStore, LanguageCode language, HttpServletResponse response) {

		try {
			manufacturerFacade.saveOrUpdateManufacturer(manufacturer, merchantStore, language);

			return manufacturer;

		}
		catch (Exception e) {
			log.error("Error while creating manufacturer", e);
			try {
				response.sendError(503, "Error while creating manufacturer " + e.getMessage());
			}
			catch (Exception ignore) {
			}

			return null;
		}
	}

	@RequestMapping(value = "/manufacturer/{id}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)

	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public ReadableManufacturer get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language,
			HttpServletResponse response) {

		try {
			ReadableManufacturer manufacturer = manufacturerFacade.getManufacturer(id, merchantStore, language);

			if (manufacturer == null) {
				response.sendError(404, "No Manufacturer found for ID : " + id);
			}

			return manufacturer;

		}
		catch (Exception e) {
			log.error("Error while getting manufacturer", e);
			try {
				response.sendError(503, "Error while getting manufacturer " + e.getMessage());
			}
			catch (Exception ignore) {
			}
		}

		return null;
	}

	@RequestMapping(value = "/private/manufacturer/{id}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)

	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableManufacturer getBrand(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language,
			HttpServletResponse response) {

		try {
			ReadableManufacturer manufacturer = manufacturerFacade.getManufacturer(id, merchantStore,
					LanguageCode.allLanguage());

			if (manufacturer == null) {
				response.sendError(404, "No Manufacturer found for ID : " + id);
			}

			return manufacturer;

		}
		catch (Exception e) {
			log.error("Error while getting manufacturer", e);
			try {
				response.sendError(503, "Error while getting manufacturer " + e.getMessage());
			}
			catch (Exception ignore) {
			}
		}

		return null;
	}

	@RequestMapping(value = "/private/manufacturers", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)

	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "List manufacturers by store",
			summary = "This request supports paging or not. Paging supports page number and request" + " count",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableManufacturerList.class))))
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableManufacturerList listByStore(StoreMerchantId merchantStore, LanguageCode language,
			@RequestParam(value = "name", required = false) String name, Pageable pageable) {

		ListCriteria listCriteria = new ListCriteria();
		listCriteria.setName(name);
		return manufacturerFacade.listByStore(merchantStore, LanguageCode.nonLanguage(), listCriteria, pageable);
	}

	@RequestMapping(value = "/manufacturers", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)

	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "List manufacturers by store",
			summary = "This request supports paging or not. Paging supports page number and request" + " count",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableManufacturerList.class))))
	public ReadableManufacturerList list(StoreMerchantId merchantStore, LanguageCode language,
			@RequestParam(value = "name", required = false) String name, Pageable pageable) {

		ListCriteria listCriteria = new ListCriteria();
		listCriteria.setName(name);
		return manufacturerFacade.listByStore(merchantStore, language, listCriteria, pageable);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/manufacturer/unique" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "Check if manufacturer code already exists",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code, StoreMerchantId merchantStore,
			LanguageCode language) {

		boolean exists = manufacturerFacade.manufacturerExist(merchantStore, code);
		return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);
	}

	@RequestMapping(value = "/private/manufacturer/{id}", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.OK)

	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void update(@PathVariable Long id, @Valid @RequestBody PersistableManufacturer manufacturer,
			StoreMerchantId merchantStore, LanguageCode language, HttpServletResponse response) {

		try {
			manufacturer.setId(id);
			manufacturerFacade.saveOrUpdateManufacturer(manufacturer, merchantStore, language);
		}
		catch (Exception e) {
			log.error("Error while creating manufacturer", e);
			try {
				response.sendError(503, "Error while creating manufacturer " + e.getMessage());
			}
			catch (Exception ignore) {
			}
		}
	}

	@RequestMapping(value = "/private/manufacturer/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void delete(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language,
			HttpServletResponse response) {

		try {
			Manufacturer manufacturer = manufacturerService.getById(id);

			if (manufacturer != null) {
				manufacturerFacade.deleteManufacturer(manufacturer);
			}
			else {
				response.sendError(404, "No Manufacturer found for ID : " + id);
			}

		}
		catch (Exception e) {
			log.error("Error while deleting manufacturer id {}", id, e);
			try {
				response.sendError(503, "Error while deleting manufacturer id " + id + " - " + e.getMessage());
			}
			catch (Exception ignore) {
			}
		}
	}

	@RequestMapping(value = "/category/{id}/manufacturer", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@Operation(method = "GET", description = "Get all manufacturers for all items in a given category",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))

	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public List<ReadableManufacturer> list(@PathVariable final Long id, // category
			// id
			StoreMerchantId merchantStore, LanguageCode language) {
		return manufacturerFacade.getByProductInCategory(merchantStore, language, id);
	}

}
