package api.feignProxies;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import api.dtos.BankAccountDto;

@FeignClient("bank-account")
public interface BankAccountProxy {
	
	@GetMapping("/bank-accounts/{email}")
	BankAccountDto getBankAccountByEmail(@PathVariable("email") String email);
	
	 @DeleteMapping("/bank-accounts/{email}")
	 public void deleteBankAccount(@PathVariable("email") String email);

    @PostMapping("/bank-accounts")
    ResponseEntity<?> createBankAccount(@RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader);

    @PutMapping("/bank-accounts/{email}")
    ResponseEntity<?> updateBankAccount(@PathVariable("email") String email, @RequestBody Map<String, BigDecimal> fiatBalances, @RequestHeader("Authorization") String authorizationHeader);
    
    

}
