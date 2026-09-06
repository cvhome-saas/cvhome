package com.asrevo.cvhome.checkout.services.money;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.checkout.errors.PriceNotFormattableException;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.errors.UncheckedBaseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyFormatterTest {

    private static final String LIT_1234_5 = "1234.5";

    private static final String USD_2 = "USD";

    @Test
    void formatsInTheStoreCurrencyForTheLocale() {
        assertThat(MoneyFormatter.format(new BigDecimal(LIT_1234_5), new CurrencyCode(USD_2), Locale.US))
                .isEqualTo("$1,234.50");
        assertThat(MoneyFormatter.format(new BigDecimal(LIT_1234_5), new CurrencyCode("EUR"), Locale.GERMANY))
                .contains("1.234,50");
        assertThat(MoneyFormatter.format(null, new CurrencyCode(USD_2), Locale.US)).isEqualTo("$0.00");
    }

    @Test
    void aBadCurrencyIsOurFault() {
        assertThatThrownBy(() -> MoneyFormatter.format(BigDecimal.ONE, new CurrencyCode("NOPE"), Locale.US))
                .isInstanceOf(UncheckedBaseException.class)
                .satisfies(e -> assertThat(((UncheckedBaseException) e).getCause())
                        .isInstanceOf(PriceNotFormattableException.class));
    }
}
