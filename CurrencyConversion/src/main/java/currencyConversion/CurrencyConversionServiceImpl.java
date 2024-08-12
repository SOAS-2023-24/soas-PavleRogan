package currencyConversion;

import java.math.BigDecimal;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import api.dtos.BankAccountDto;
import api.dtos.CurrencyConversionDto;
import api.dtos.CurrencyExchangeDto;
import api.feignProxies.BankAccountProxy;
import api.feignProxies.CurrencyExchangeProxy;
import api.feignProxies.UsersProxy;
import api.services.CurrencyConversionService;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import util.exceptions.NoDataFoundException;
import util.exceptions.ServiceUnavailableException;

@RestController
public class CurrencyConversionServiceImpl implements CurrencyConversionService {
	
	//private RestTemplate template = new RestTemplate();
	

	@Autowired
	private CurrencyExchangeProxy exchangeProxy;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;

	@Autowired
	private UsersProxy usersProxy;
	
	CurrencyExchangeDto response;
	Retry retry;
	
	public CurrencyConversionServiceImpl(RetryRegistry registry) {
		this.retry = registry.retry("default");
	}

	
	@Override
	public ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal quantity, @RequestHeader("Authorization") String authorizationHeader) {
	    try {
	        String user = usersProxy.getCurrentUserRole(authorizationHeader);

	        if (!"USER".equals(user)) {
	        	throw new NoDataFoundException("Not allowed to perform exchanging since you are not 'USER'.");
	        }

	        String userEmail = usersProxy.getCurrentUserEmail(authorizationHeader);
	        BankAccountDto bankAccount = bankAccountProxy.getBankAccountByEmail(userEmail);

	        if (bankAccount == null) {
	        	throw new NoDataFoundException("Bank account not found for user.");
	        }

	        BigDecimal accountCurrencyAmount = bankAccountProxy.getUserCurrencyAmount(userEmail, from);
	        if (accountCurrencyAmount.compareTo(quantity) < 0) {
	        	throw new NoDataFoundException("User doesn't have enough amount in the bank account for exchanging.");
	        }

	        ResponseEntity<CurrencyExchangeDto> response = exchangeProxy.getExchange(from, to);
	        
	        if (response == null || response.getBody() == null) {
	        	 throw new ServiceUnavailableException("No CurrencyExchange service response.");
	        }

	        CurrencyExchangeDto responseBody = response.getBody();
	        BigDecimal exchangeValue = responseBody.getExchangeValue();
	        BigDecimal totalExchanged = exchangeValue.multiply(quantity);

	        ResponseEntity<?> updatedBalances = bankAccountProxy.updateBalances(userEmail, from, to, quantity, totalExchanged);
	        
	        if (!updatedBalances.getStatusCode().is2xxSuccessful()) {
	            return ResponseEntity.status(updatedBalances.getStatusCode()).body("Failed to update balances.");
	        }

	        String message = "Successfull conversion! " + quantity + " "+ from + " exchanged for "+ totalExchanged + " " + to;
	        
	        
	        return ResponseEntity.ok().body(new Object() {
				public Object getBody() {
					return updatedBalances.getBody();
				}
				public String getMessage() {
					return message;
				}
			});

	    } catch (FeignException ex) {
	        ex.printStackTrace();

	        return ResponseEntity.status(ex.status()).body(ex.getMessage());
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + ex.getMessage());
	    }
	}
	
	

}
