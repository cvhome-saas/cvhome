package com.asrevo.cvhome.catalog.api.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturerList;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.catalog.services.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.catalog.service.facade.manufacturer.ManufacturerFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

/**
 * Manufacturer management Collection, Manufacturer ...
 *
 * @author c.samson
 */
@Controller
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
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public PersistableManufacturer create(@Valid @RequestBody PersistableManufacturer manufacturer,
                                          @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language, HttpServletResponse response) {

        try {
            manufacturerFacade.saveOrUpdateManufacturer(manufacturer, merchantStore, language);

            return manufacturer;

        } catch (Exception e) {
            log.error("Error while creating manufacturer", e);
            try {
                response.sendError(503, "Error while creating manufacturer " + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
    }

    @RequestMapping(value = "/manufacturer/{id}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableManufacturer get(@PathVariable Long id, @Parameter(hidden = true) StoreMerchantId merchantStore,
                                    @Parameter(hidden = true) LanguageCode language, HttpServletResponse response) {

        try {
            ReadableManufacturer manufacturer = manufacturerFacade.getManufacturer(id, merchantStore, language);

            if (manufacturer == null) {
                response.sendError(404, "No Manufacturer found for ID : " + id);
            }

            return manufacturer;

        } catch (Exception e) {
            log.error("Error while getting manufacturer", e);
            try {
                response.sendError(503, "Error while getting manufacturer " + e.getMessage());
            } catch (Exception ignore) {
            }
        }

        return null;
    }


    @RequestMapping(value = "/private/manufacturers", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "List manufacturers by store", summary = "This request supports paging or not. Paging supports page number and request count",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableManufacturerList.class)))
    )
    public ReadableManufacturerList listByStore(
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        ListCriteria listCriteria = new ListCriteria();
        listCriteria.setName(name);
        return manufacturerFacade.listByStore(merchantStore, language, listCriteria, page, count);
    }


    @RequestMapping(value = "/manufacturers", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "List manufacturers by store", summary = "This request supports paging or not. Paging supports page number and request count",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableManufacturerList.class)))
    )
    public ReadableManufacturerList list(@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language,
                                         @RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                         @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        ListCriteria listCriteria = new ListCriteria();
        listCriteria.setName(name);
        return manufacturerFacade.getAllManufacturers(merchantStore, language, listCriteria, page, count);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/manufacturer/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Check if manufacturer code already exists",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code,
                                               @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

        boolean exists = manufacturerFacade.manufacturerExist(merchantStore, code);
        return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);

    }

    @RequestMapping(value = "/private/manufacturer/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void update(@PathVariable Long id,
                       @Valid @RequestBody PersistableManufacturer manufacturer, @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
                       @Parameter(hidden = true) LanguageCode language, HttpServletResponse response) {

        try {
            manufacturer.setId(id);
            manufacturerFacade.saveOrUpdateManufacturer(manufacturer, merchantStore, language);
        } catch (Exception e) {
            log.error("Error while creating manufacturer", e);
            try {
                response.sendError(503, "Error while creating manufacturer " + e.getMessage());
            } catch (Exception ignore) {
            }
        }
    }

    @RequestMapping(value = "/private/manufacturer/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(@PathVariable Long id, @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language,
                       HttpServletResponse response) {

        try {
            Manufacturer manufacturer = manufacturerService.getById(id);

            if (manufacturer != null) {
                manufacturerFacade.deleteManufacturer(manufacturer);
            } else {
                response.sendError(404, "No Manufacturer found for ID : " + id);
            }

        } catch (Exception e) {
            log.error("Error while deleting manufacturer id {}", id, e);
            try {
                response.sendError(503, "Error while deleting manufacturer id " + id + " - " + e.getMessage());
            } catch (Exception ignore) {
            }
        }
    }

    @RequestMapping(value = "/category/{id}/manufacturer", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Operation(method = "GET", description = "Get all manufacturers for all items in a given category",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public List<ReadableManufacturer> list(@PathVariable final Long id, // category
                                           // id
                                           @Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {
        return manufacturerFacade.getByProductInCategory(merchantStore, language, id);

    }

}
