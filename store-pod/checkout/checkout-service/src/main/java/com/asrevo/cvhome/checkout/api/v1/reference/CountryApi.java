package com.asrevo.cvhome.checkout.api.v1.reference;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.services.reference.CountryService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.model.references.ReadableCountry;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The ISO country list, named in the request language, for the address forms of both frontends.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reference")
@RequiredArgsConstructor
public class CountryApi {

    private final CountryService countries;

    @GetMapping("/country")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public List<ReadableCountry> countries(StoreMerchantId merchantStore, LanguageCode language) {
        return countries.all(language);
    }
}
