package soas.crypto_conversion.implementation;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.BankAccountDto;
import api.dtos.CryptoExchangeDto;
import api.dtos.CryptoWalletDto;
import api.feignProxies.BankAccountProxy;
import api.feignProxies.CryptoExchangeProxy;
import api.feignProxies.CryptoWalletProxy;
import api.feignProxies.UsersProxy;
import api.services.CryptoConversionService;
import feign.FeignException;
import util.exceptions.NoDataFoundException;
import util.exceptions.ServiceUnavailableException;

@RestController
public class CryptoConversionImpl implements CryptoConversionService {

	@Autowired
	private CryptoExchangeProxy cryptoExchangeProxy;
	
	@Autowired
	private CryptoWalletProxy walletProxy;

	@Autowired
	private UsersProxy usersProxy;

	

	@Override
	public ResponseEntity<?> getConversion(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal quantity, @RequestHeader("Authorization") String authorizationHeader) {
	    try {
	        String user = usersProxy.getCurrentUserRole(authorizationHeader);

	        if (!"USER".equals(user)) {
	            return ResponseEntity.status(HttpStatus.CONFLICT).body("Not allowed if you are not 'USER'.");
	        }

	        String userEmail = usersProxy.getCurrentUserEmail(authorizationHeader);
	        CryptoWalletDto wallet = walletProxy.getWalletByEmail(userEmail);

	        if (wallet == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wallet not found.");
	        }

	        BigDecimal walletCurrencyAmount = walletProxy.getUserCryptoState(userEmail, from);
	        if (walletCurrencyAmount.compareTo(quantity) < 0) {
	            return ResponseEntity.status(HttpStatus.CONFLICT).body("No enough amount in wallet.");
	        }

	        ResponseEntity<CryptoExchangeDto> response = cryptoExchangeProxy.cryptoExchange(from, to);
	        
	        if (response == null || response.getBody() == null) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("CryptoExchange service doesnt respond.");
	        }

	        CryptoExchangeDto responseBody = response.getBody();
	        BigDecimal exchangeValue = responseBody.getExchangeValue();
	        BigDecimal totalExchanged = exchangeValue.multiply(quantity);

	        ResponseEntity<?> updatedWallet = walletProxy.updateWalletState(userEmail, from, to, quantity, totalExchanged);
	        
	        if (!updatedWallet.getStatusCode().is2xxSuccessful()) {
	            return ResponseEntity.status(updatedWallet.getStatusCode()).body("Failed to update.");
	        }

	        String message = "Successfull conversion! " + quantity + " " + from + " exchanged for " + totalExchanged + to;
	        
	        
	        return ResponseEntity.ok().body(new Object() {
				public Object getBody() {
					return updatedWallet.getBody();
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
