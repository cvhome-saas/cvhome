package com.asrevo.cvhome.checkout.api.order.v1.reference;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.service.facade.country.CountryFacade;
import com.asrevo.cvhome.checkout.service.facade.zone.ZoneFacade;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableCountry;
import com.asrevo.cvhome.store.model.references.ReadableZone;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Get system Language, Country and Currency objects
 *
 * @author c.samson
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class ReferencesApi {

	private final ZoneFacade zoneFacade;

	private final CountryFacade countryFacade;

	public ReferencesApi(ZoneFacade zoneFacade, CountryFacade countryFacade) {
		this.zoneFacade = zoneFacade;
		this.countryFacade = countryFacade;
	}

	@GetMapping("/zones")
	@ConditionalOnApiStatus
	public List<ReadableZone> getZones(@RequestParam("code") String code, LanguageCode language,
			HttpServletRequest request) {
		StoreMerchantId merchantId = getByMerchantStoreId(request);
		return zoneFacade.getZones(new CountryIsoCode(code), language, merchantId);
	}

	@GetMapping("/country")
	public List<ReadableCountry> getCountry(LanguageCode language, HttpServletRequest request) {
		StoreMerchantId merchantId = getByMerchantStoreId(request);
		return countryFacade.getListCountryZones(language, merchantId);
	}

	private StoreMerchantId getByMerchantStoreId(HttpServletRequest request) {
		String merchantStoreId = request.getParameter("store");
		if (StringUtils.isEmpty(merchantStoreId)) {
			return DEFAULT_ORG1_STORE1;
		}
		return new StoreMerchantId(merchantStoreId);
	}

}
