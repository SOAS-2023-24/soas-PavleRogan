package currencyConversion;

import java.math.BigDecimal;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import api.dtos.CurrencyConversionDto;
import api.dtos.CurrencyExchangeDto;
import api.feignProxies.CurrencyExchangeProxy;
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
	
	private RestTemplate template = new RestTemplate();
	
	@Autowired
	private CurrencyExchangeProxy proxy;
	
	CurrencyExchangeDto response;
	Retry retry;
	
	public CurrencyConversionServiceImpl(RetryRegistry registry) {
		this.retry = registry.retry("default");
	}

	@Override
	public ResponseEntity<?> getConversion(String from, String to, BigDecimal quantity) {
		HashMap<String,String> uriVariables = new HashMap<String,String>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);
		
		CurrencyExchangeDto dto = null;
		
		try {
			ResponseEntity<CurrencyExchangeDto> response = template.getForEntity
					("http://localhost:8000/currency-exchange?from={from}&to={to}",
							CurrencyExchangeDto.class, uriVariables);
			dto = response.getBody();
		} catch (HttpClientErrorException e) {
			throw new NoDataFoundException(e.getMessage());
		}
		
		
		return ResponseEntity.ok(exchangeToConversion(dto,quantity));
	}
	
	@Override
	@CircuitBreaker(name = "cb", fallbackMethod = "fallback")
	public ResponseEntity<?> getConversionFeign(String from, String to, BigDecimal quantity) {
		try {
			retry.executeSupplier( () -> response = acquireExchange(from, to));
		} catch (FeignException e) {
			if(e.status() != 404) {
				throw new ServiceUnavailableException("Currency exchange service is unavailable");
			}
			throw new NoDataFoundException(e.getMessage());
		}
		
		return ResponseEntity.ok(exchangeToConversion(response, quantity));
	}
	
	public CurrencyExchangeDto acquireExchange(String from, String to) {
		return proxy.getExchange(from, to).getBody();
	}
	
	public ResponseEntity<?> fallback(CallNotPermittedException ex){
		return ResponseEntity.status(503).body("Currency conversion service is unavailable");
	}
	
	public CurrencyConversionDto exchangeToConversion(CurrencyExchangeDto exchange,
			BigDecimal quantity) {
		return new CurrencyConversionDto(exchange, quantity, exchange.getTo(),
				quantity.multiply(exchange.getExchangeValue()));
		
	}

	

}
