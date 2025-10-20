package com.asrevo.cvhome.order.api.order.v2.statistic;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.repositories.order.OrderRepository;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2")
@AllArgsConstructor
@Tag(name = "Customer statistic resource", description = "Customer statistic")
@Slf4j
public class CustomerStatisticApi {

	private final OrderRepository orderRepository;

	@RequestMapping(value = { "/private/customer-statistic" }, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public StatisticList customerStatistic(@Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language, @RequestBody StatisticRange range) {
		List<StatisticEntry> entries = orderRepository.customerStatistic(Date.from(range.fromDate().toInstant()),
				Date.from(range.toDate().toInstant()), merchantStore);
		return new StatisticList(entries);
	}

}
