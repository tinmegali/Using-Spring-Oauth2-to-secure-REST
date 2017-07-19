package com.tinmegali.oauth2.controllers;

import com.tinmegali.oauth2.models.Account;
import com.tinmegali.oauth2.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class AccountController {

    @Autowired
    AccountService accountService;

    @PreAuthorize("hasRole('REGISTER')")
    @PostMapping("/api/account/register")
    public ResponseEntity<Account> registerAccount(@RequestBody Account account) {
        account = accountService.registerUser(account);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @PreAuthorize("isFullyAuthenticated()")
    @DeleteMapping("/api/account/remove")
    public ResponseEntity<GeneralController.RestMsg> removeAccount(Principal principal){
        boolean isDeleted = accountService.removeAuthenticatedAccount();
        if ( isDeleted ) {
            return new ResponseEntity<>(
                    new GeneralController.RestMsg(String.format("[%s] removed.", principal.getName())),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<GeneralController.RestMsg>(
                    new GeneralController.RestMsg(String.format("An error ocurred while delete [%s]", principal.getName())),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

}
