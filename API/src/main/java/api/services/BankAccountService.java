package api.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import api.dtos.BankAccountDto;

public interface BankAccountService {
	
	@GetMapping("/bank-accounts")
	public ResponseEntity<List<BankAccountDto>> getAllAccounts();
	
	@GetMapping("/bank-accounts/{email}")
	BankAccountDto getBankAccountByEmail(@PathVariable("email") String email);
	
	@GetMapping("/bank-account/user")
	BankAccountDto getBankAccountForUser(@RequestHeader("Authorization") String authorizationHeader);

    @PostMapping("/bank-accounts")
    ResponseEntity<?> createBankAccount(@RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader);

    @PutMapping("/bank-accounts/{email}")
    ResponseEntity<?> updateBankAccount(@PathVariable String email, @RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader);

    @DeleteMapping("/bank-accounts/{email}")
    public void deleteBankAccount(@PathVariable("email") String email);
}
