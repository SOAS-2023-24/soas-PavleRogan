package currencyExchange.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CurrencyExchangeDto;
import api.services.CurrencyExchangeService;
import currencyExchange.model.CurrencyExchangeModel;
import currencyExchange.repository.CurrencyExchangeRepository;
import util.exceptions.NoDataFoundException;

@RestController
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {
	
	@Autowired
	private CurrencyExchangeRepository repo;
	
	@Autowired
	private Environment environment;

	@Override
	public ResponseEntity<CurrencyExchangeDto> getExchange(String from, String to) {
		if (!isSupportedFiatCurrency(from) || !isSupportedFiatCurrency(to)) {
			throw new NoDataFoundException("Fiat currency from request not found.");
        }
		
		CurrencyExchangeModel model = repo.findByFromAndTo(from, to);
		if(model == null) {
			return ResponseEntity.status(404).body(null);
		}
		return ResponseEntity.ok(convertModelToDto(model));
	}
	
	public CurrencyExchangeDto convertModelToDto(CurrencyExchangeModel model) {
		CurrencyExchangeDto dto = 
				new CurrencyExchangeDto(model.getFrom(), model.getTo(), model.getExchangeValue());
		dto.setInstancePort(environment.getProperty("local.server.port"));
		return dto;
	}
	
	  private boolean isSupportedFiatCurrency(String currency) {
	        return "EUR".equals(currency) || 
	               "USD".equals(currency) || 
	               "RSD".equals(currency) || 
	               "CHF".equals(currency) || 
	               "CAD".equals(currency) || 
	               "GBP".equals(currency);
	    }

}
