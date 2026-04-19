package com.asrevo.cvhome.catalog.api.v1.product;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

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
    @PostMapping(value = "/private/manufacturer")
    @ResponseStatus(HttpStatus.CREATED)


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public PersistableManufacturer create(@Valid @RequestBody PersistableManufacturer manufacturer,
                                          StoreMerchantId merchantStore, LanguageCode language, HttpServletResponse response)
            throws Exception {

        manufacturerFacade.saveOrUpdateManufacturer(manufacturer, merchantStore, language);

        return manufacturer;

    }

    @GetMapping(value = "/manufacturer/{id}")
    @ResponseStatus(HttpStatus.OK)


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    public ReadableManufacturer get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language,
                                    HttpServletResponse response) {

        return manufacturerFacade.getManufacturer(id, merchantStore, language);
    }

    @GetMapping(value = "/private/manufacturer/{id}")
    @ResponseStatus(HttpStatus.OK)


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableManufacturer getBrand(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language,
                                         HttpServletResponse response) {

        return manufacturerFacade.getManufacturer(id, merchantStore, LanguageCode.allLanguage());
    }

    @GetMapping(value = "/private/manufacturers")
    @ResponseStatus(HttpStatus.OK)


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
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

    @GetMapping(value = "/manufacturers")
    @ResponseStatus(HttpStatus.OK)


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
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
    @GetMapping(value = {"/private/manufacturer/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "GET", description = "Check if manufacturer code already exists",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code, StoreMerchantId merchantStore,
                                               LanguageCode language) {

        boolean exists = manufacturerFacade.manufacturerExist(merchantStore, code);
        return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);
    }

    @PutMapping(value = "/private/manufacturer/{id}")
    @ResponseStatus(HttpStatus.OK)


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void update(@PathVariable Long id, @Valid @RequestBody PersistableManufacturer manufacturer,
                       StoreMerchantId merchantStore, LanguageCode language) throws Exception {
        manufacturer.setId(id);
        manufacturerFacade.saveOrUpdateManufacturer(manufacturer, merchantStore, language);
    }

    @DeleteMapping(value = "/private/manufacturer/{id}")
    @ResponseStatus(HttpStatus.OK)

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language,
                       HttpServletResponse response) throws Exception {

        Manufacturer manufacturer = manufacturerService.getById(id);

        manufacturerFacade.deleteManufacturer(manufacturer);
    }

    @GetMapping(value = "/category/{id}/manufacturer")
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "GET", description = "Get all manufacturers for all items in a given category",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    public List<ReadableManufacturer> list(@PathVariable final Long id, // category
                                           // id
                                           StoreMerchantId merchantStore, LanguageCode language) {
        return manufacturerFacade.getByProductInCategory(merchantStore, language, id);
    }

}
