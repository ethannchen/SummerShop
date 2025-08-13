package com.summershop.account.service;

import com.summershop.account.dto.AccountRequest;
import com.summershop.account.dto.AccountResponse;
import com.summershop.account.entity.Account;
import com.summershop.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creating account for email: {}", request.getEmail());

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Account with email already exists: " + request.getEmail());
        }

        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setPhoneNumber(request.getPhoneNumber());
        account.setAddress(request.getAddress());
        account.setCity(request.getCity());
        account.setState(request.getState());
        account.setZipCode(request.getZipCode());
        account.setCountry(request.getCountry());
        account.setActive(true);

        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Transactional
    public AccountResponse updateAccount(Long id, AccountRequest request) {
        log.info("Updating account with id: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setPhoneNumber(request.getPhoneNumber());
        account.setAddress(request.getAddress());
        account.setCity(request.getCity());
        account.setState(request.getState());
        account.setZipCode(request.getZipCode());
        account.setCountry(request.getCountry());

        Account updatedAccount = accountRepository.save(account);
        return mapToResponse(updatedAccount);
    }

    public AccountResponse getAccount(Long id) {
        log.info("Fetching account with id: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        return mapToResponse(account);
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setEmail(account.getEmail());
        response.setFirstName(account.getFirstName());
        response.setLastName(account.getLastName());
        response.setPhoneNumber(account.getPhoneNumber());
        response.setAddress(account.getAddress());
        response.setCity(account.getCity());
        response.setState(account.getState());
        response.setZipCode(account.getZipCode());
        response.setCountry(account.getCountry());
        response.setActive(account.getActive());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }
}