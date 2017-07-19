package com.tinmegali.oauth2.repositories;


import com.tinmegali.oauth2.models.Account;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.Optional;

public interface AccountRepo extends Repository<Account, Long> {
    Optional<Account> findByUsername(String username);
    Account save(Account account);
    int deleteAccountById(Long id);
}
