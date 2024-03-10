package com.asrevo.cvhome.wallet.service.impl;

import com.asrevo.cvhome.wallet.entity.WalletId;

public interface WalletChargeService {
  void charge(WalletId walletId);
}
