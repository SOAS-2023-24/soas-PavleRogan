package soas.crypto_wallet.implementation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CryptoPairDto;
import api.dtos.CryptoWalletDto;
import api.feignProxies.UsersProxy;
import api.services.CryptoWalletService;
import soas.crypto_wallet.model.CryptoPairModel;
import soas.crypto_wallet.model.CryptoWalletModel;
import soas.crypto_wallet.repository.CryptoWalletRepositoory;
import util.exceptions.ForbiddenOperationException;
import util.exceptions.NoDataFoundException;

@RestController
public class CryptoWalletServiceImpl implements CryptoWalletService {

	@Autowired
	private CryptoWalletRepositoory repository;
	
	@Autowired
	private UsersProxy userProxy;
	
	@Override
	public ResponseEntity<List<CryptoWalletDto>> getAllWallets() {
		List<CryptoWalletModel> models = repository.findAll();
        List<CryptoWalletDto> dtos = models.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
	}

	@Override
	public CryptoWalletDto getWalletByEmail(String email) {
		CryptoWalletModel wallet = repository.findByEmail(email);
        if (wallet == null) {
            throw new NoDataFoundException("Crypto wallet with email " + email + " not found.");
           //return null;
        }
        return convertToDto(wallet);
	}

	

	@Override
	public void deleteWallet(String email) {
		CryptoWalletModel wallet = repository.findByEmail(email);
		if (wallet != null) {
			repository.delete(wallet);
		}
		
	}

	@Override
	public ResponseEntity<?> createWallet(CryptoWalletDto dto, String authorizationHeader) {
		String role = userProxy.getCurrentUserRole(authorizationHeader);

		 try {
			 
	            if ("OWNER".equals(role) || "USER".equals(role)) {
	            	
	                throw new ForbiddenOperationException("Only users with role 'ADMIN' can create a crypto wallet.");
	                //return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "Only 'ADMIN' can create wallets."));
	            }

	            if ("ADMIN".equals(role)) {
	                Boolean userExists = userProxy.getUser(dto.getEmail());
	                if (!userExists) {
	                    throw new NoDataFoundException("User " + dto.getEmail() + " does not exist. Satus " + HttpStatus.BAD_REQUEST);
	                    //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email " + dto.getEmail() + " does not exist.");
	                }

	                CryptoWalletModel existingWallet = repository.findByEmail(dto.getEmail());
	                if (existingWallet != null) {
	                    return ResponseEntity.badRequest().body("Wallet for user " + dto.getEmail() + " already exists.");
	                }

	                CryptoWalletModel wallet = new CryptoWalletModel(dto.getEmail());
	                List<CryptoPairModel> initialValues = generateInitialValues(wallet);
	                wallet.setValues(initialValues);
	                repository.save(wallet);

	                return ResponseEntity.ok(Collections.singletonMap("message", "Wallet created successfully for user: " + dto.getEmail()));
	            }

	          
	            Boolean userExists = userProxy.getUser(dto.getEmail());
	            if (!userExists) {
	                throw new NoDataFoundException("User with email " + dto.getEmail() + " does not exist.");
	               // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "User with email " + dto.getEmail() + " does not exist."));
	            }

	            CryptoWalletModel existingWallet = repository.findByEmail(dto.getEmail());
	            if (existingWallet != null) {
	                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Crypto wallet for user with email " + dto.getEmail() + " already exists."));
	            }

	            CryptoWalletModel wallet = new CryptoWalletModel(dto.getEmail());
	            List<CryptoPairModel> initialValues = generateInitialValues(wallet);
	            wallet.setValues(initialValues);
	            repository.save(wallet);

	            return ResponseEntity.ok(Collections.singletonMap("message", "Crypto wallet created successfully for user: " + dto.getEmail()));
	        }
		 catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error creating crypto wallet: " + e.getMessage()));
	        }
	}

	@Override
	public ResponseEntity<?> updateWallet(String email, CryptoWalletDto dto, String authorizationHeader) {
		String role = userProxy.getCurrentUserRole(authorizationHeader);
	    String currentUserEmail = userProxy.getCurrentUserEmail(authorizationHeader);

	    if (!"ADMIN".equals(role)) {
	        return ResponseEntity.status(403).body("Only ADMIN is authorized to access this service.");
	    }
	    
//	    if ("USER".equals(role) && !currentUserEmail.equals(email)) {
//	        return ResponseEntity.status(403).body("User can only update their own account.");
//	    }

	    CryptoWalletModel wallet = repository.findByEmail(email);
	    if (wallet == null) {
	    	 throw new NoDataFoundException("Wallet of user " + dto.getEmail() + " does not exist.");
	        //return ResponseEntity.notFound().build();
	    }
	    
	    // Validation:  BTC, ETH, SOL 
	    boolean hasBTC = false;
	    boolean hasETH = false;
	    boolean hasSOL = false;

	    for (CryptoPairDto cryptoPairDto : dto.getPairs()) {
	        String crypto = cryptoPairDto.getCrypto().toUpperCase();
	        switch (crypto) {
	            case "BTC":
	                hasBTC = true;
	                break;
	            case "ETH":
	                hasETH = true;
	                break;
	            case "SOL":
	                hasSOL = true;
	                break;
	        }
	    }

	    if (!hasBTC || !hasETH || !hasSOL) {
	        return ResponseEntity.badRequest().body("Wallet must include BTC, ETH, and SOL.");
	    }

	    
	    
	    wallet.getValues().clear();
	    for (CryptoPairDto cryptoPairDto: dto.getPairs()) {
	    	CryptoPairModel cryptoPairModel = new CryptoPairModel(cryptoPairDto.getCrypto(), cryptoPairDto.getAmount());
	    	cryptoPairModel.setCryptoWallet(wallet);
	    	wallet.getValues().add(cryptoPairModel);
	    }

	    repository.save(wallet);

	    return ResponseEntity.ok("Crypto wallett updated successfully");
	}
	
	
	@Override
	public CryptoWalletDto getUsersWallet(String authorizationHeader) {
	    String role = userProxy.getCurrentUserRole(authorizationHeader);
	    String currentUserEmail = userProxy.getCurrentUserEmail(authorizationHeader);
	    
	    if ("USER".equals(role)) {
	        return getWalletByEmail(currentUserEmail);
	    }

        throw new ForbiddenOperationException("'USER' can see his acc only");

	    //return null;
	}
	
	private CryptoWalletDto convertToDto(CryptoWalletModel model) {
		CryptoWalletDto dto = new CryptoWalletDto();
	    dto.setEmail(model.getEmail());

	    if (model.getValues() != null) {
	        List<CryptoPairDto> cryptoPairDtos = model.getValues().stream()
	                .map(cryptoValuesModel -> {
	                	CryptoPairDto cryptoValuesDto = new CryptoPairDto();
	                	cryptoValuesDto.setCrypto(cryptoValuesModel.getCrypto());
	                	cryptoValuesDto.setAmount(cryptoValuesModel.getAmount());
	                    return cryptoValuesDto;
	                })
	                .collect(Collectors.toList());
	        dto.setPairs(cryptoPairDtos);
	    }

	    return dto;
	}
	
	private List<CryptoPairModel> generateInitialValues(CryptoWalletModel wallet) {
	    List<CryptoPairModel> pairs = new ArrayList<>();
	    pairs.add(new CryptoPairModel("BTC", BigDecimal.ZERO));
	    pairs.add(new CryptoPairModel("ETH", BigDecimal.ZERO));
	    pairs.add(new CryptoPairModel("SOL", BigDecimal.ZERO));
	    for (CryptoPairModel pair : pairs) {
	    	pair.setCryptoWallet(wallet);
	    }
	    return pairs;
	}

}
