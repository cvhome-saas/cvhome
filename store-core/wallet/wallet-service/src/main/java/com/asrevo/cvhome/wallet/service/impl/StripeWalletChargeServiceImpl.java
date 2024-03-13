package com.asrevo.cvhome.wallet.service.impl;

import com.asrevo.cvhome.wallet.entity.WalletId;
import com.stripe.Stripe;
import com.stripe.model.PaymentLink;
import com.stripe.param.PaymentLinkCreateParams;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class StripeWalletChargeServiceImpl implements WalletChargeService {
    @SneakyThrows
    static void charge() {
        Stripe.apiKey = "rk_test_51OsTud2M1P3uzrFiWJuN8ImfMv2eH7akmI37gWBWd8Pk7f2SwSevdPZbRwZ7611XPxjonfjBs1RoPzOXLzjWDskZ00ivN909uD";

/*
    Price usd = Price.create(PriceCreateParams.builder()
      .setCurrency("USD")
//      .setUnitAmount(2000L)
        .setCustomUnitAmount(PriceCreateParams.CustomUnitAmount.builder()
          .setMinimum(500L)
          .setMaximum(2000L)
          .setEnabled(Boolean.TRUE)
          .build())
        .setProductData(PriceCreateParams.ProductData
          .builder()
          .setId("charge-3-usd")
          .setName("charge-3-usd")
          .build())
      .build());


    System.out.println("price "+usd.getId());
*/


        PaymentLinkCreateParams linkCreateParams = PaymentLinkCreateParams.builder()
                .setCurrency("USD")
                .addLineItem(PaymentLinkCreateParams.LineItem.builder()
                        .setPrice(/*usd.getId()*//*"price_1OsWxX2M1P3uzrFicOD6Qqz2"*/"price_1OshVz2M1P3uzrFid07fH8BZ")
                        .setQuantity(1L)
                        .build())
                .setAfterCompletion(PaymentLinkCreateParams.AfterCompletion.builder()
                        .setType(PaymentLinkCreateParams.AfterCompletion.Type.REDIRECT)
                        .setRedirect(PaymentLinkCreateParams.AfterCompletion.Redirect.builder()
                                .setUrl("https://localhost.lcl:443/wallet")
                                .build())
                        .build())
                .build();


        PaymentLink paymentLink = PaymentLink.create(linkCreateParams);

        System.out.println(paymentLink.getUrl());

    }

    public static void main(String[] args) {
        charge();
    }

    @Override
    public void charge(WalletId walletId) {

    }
}
