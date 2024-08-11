package soas.crypto_exchange.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CryptoExchangeDto;
import api.services.CryptoExchangeService;
import soas.crypto_exchange.model.CryptoExchangeModel;
import soas.crypto_exchange.repository.CryptoExchangeRepository;
import util.exceptions.NoDataFoundException;

@RestController
public class CryptoExchangeImpl implements CryptoExchangeService {

	@Autowired
	private CryptoExchangeRepository repo;
	
	@Override
	public ResponseEntity<CryptoExchangeDto> cryptoExchange(String from, String to) {
		CryptoExchangeModel model = repo.findByFromAndTo(from, to);
		if (model == null) {
	        throw new NoDataFoundException("Crypto exchange rate not found for: " + from + " to " + to);
            //return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(convertModelToDto(model));
	}

	public CryptoExchangeDto convertModelToDto(CryptoExchangeModel model) {
		 CryptoExchangeDto dto = 
	                new CryptoExchangeDto(model.getFrom(), model.getTo(), model.getExchangeValue());
	        return dto;		
	}

}
