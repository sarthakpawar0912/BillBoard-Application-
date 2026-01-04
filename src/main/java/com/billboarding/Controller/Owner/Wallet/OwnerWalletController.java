package com.billboarding.Controller.Owner.Wallet;

import com.billboarding.Entity.OWNER.wallet.OwnerWallet;
import com.billboarding.Entity.OWNER.wallet.WalletTransaction;
import com.billboarding.Entity.User;
import com.billboarding.Services.Owner.Wallet.OwnerWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/wallet")
@RequiredArgsConstructor
public class OwnerWalletController {

    private final OwnerWalletService walletService;

    @GetMapping
    public OwnerWallet getWallet(Authentication auth) {
        return walletService.getWallet((User) auth.getPrincipal());
    }

    @GetMapping("/transactions")
    public List<WalletTransaction> getTransactions(Authentication auth) {
        return walletService.getTransactions((User) auth.getPrincipal());
    }
}
