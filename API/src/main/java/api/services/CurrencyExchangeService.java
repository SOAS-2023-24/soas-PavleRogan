package api.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface CurrencyExchangeService {

	@GetMapping("/currency-exchange")
	ResponseEntity<?> getExchange(@RequestParam String from, @RequestParam String to);
}
