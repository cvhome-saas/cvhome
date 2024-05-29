package com.asrevo.cvhome.store.controller.v1.system;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.shop.ContactForm;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

@RestController
@RequestMapping("/api/v1")

@Tag(name = "Contact store resource")
public class ContactApi {


    private final LanguageService languageService;

    public ContactApi(LanguageService languageService) {
        this.languageService = languageService;
    }

    /*  @Autowired private EmailTemplatesUtils emailTemplatesUtils;*/

    @PostMapping("/contact")
    @Operation(
            method = "POST",
            description = "Sends an email contact us to store owner"
    )
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ResponseEntity<Void> contact(
            @Valid @RequestBody ContactForm contact,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletRequest request) {
        Locale locale = languageService.toLocale(language, merchantStore);
        // @TODO ASHRAF
//    emailTemplatesUtils.sendContactEmail(contact, merchantStore, locale, request.getContextPath());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
