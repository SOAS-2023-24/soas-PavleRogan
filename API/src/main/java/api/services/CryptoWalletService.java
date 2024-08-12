package api.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CryptoWalletDto;

public interface CryptoWalletService {

	@GetMapping("/crypto-wallet")
	public ResponseEntity<List<CryptoWalletDto>> getAllWallets();
	
	@GetMapping("/crypto-wallet/{email}")
	CryptoWalletDto getWalletByEmail(@PathVariable String email);

	@DeleteMapping("/crypto-wallet/{email}")
	public void deleteWallet(@PathVariable("email") String email);
	
	@GetMapping("/crypto-wallet/{email}/{cryptoFrom}")
	public BigDecimal getUserCryptoState(@PathVariable("email") String email, @PathVariable("cryptoFrom") String cryptoFrom);
	
	@PostMapping("/crypto-wallet")
	ResponseEntity<?> createWallet(@RequestBody CryptoWalletDto dto, @RequestHeader("Authorization") String authorizationHeader);
	
	@PutMapping("/crypto-wallet/{email}")
    ResponseEntity<?> updateWallet(@PathVariable String email, @RequestBody CryptoWalletDto dto, @RequestHeader("Authorization") String authorizationHeader);

	@GetMapping("/crypto-wallet/user")
	CryptoWalletDto getUsersWallet(@RequestHeader("Authorization") String authorizationHeader);	
	

    @PutMapping("/crypto-wallet/wallet")
    ResponseEntity<?> updateWalletState(@RequestParam(value = "email") String email,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "quantity", required = false) BigDecimal quantity,
            @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount);
	
}
