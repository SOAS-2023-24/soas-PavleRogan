package soas.bank_account.implementation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.BankAccountDto;
import api.dtos.FiatBalanceDto;
import api.feignProxies.UsersProxy;
import api.services.BankAccountService;
import soas.bank_account.model.BankAccountModel;
import soas.bank_account.model.FiatBalanceModel;
import soas.bank_account.repository.BankAccountRepository;

@RestController
public class BankAccountServiceImpl implements BankAccountService{

		private final BankAccountRepository repository;
		private final UsersProxy usersProxy;

	    @Autowired
	    public BankAccountServiceImpl(BankAccountRepository repository, UsersProxy usersProxy) {
	        this.repository = repository;
	        this.usersProxy = usersProxy;
	    }

	    @Override
	    public ResponseEntity<List<BankAccountDto>> getAllAccounts() {
	        List<BankAccountModel> models = repository.findAll();
	        List<BankAccountDto> dtos = models.stream().map(this::convertToDto).collect(Collectors.toList());
	        return ResponseEntity.ok(dtos);
	    }

	    
	    @Override
	    public ResponseEntity<?> createBankAccount(@RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader) {
	        String role = usersProxy.getCurrentUserRole(authorizationHeader);

	        try {
	        	
	        	  if ("OWNER".equals(role) || "USER".equals(role)) {
		                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "Only 'ADMIN' can create bank account."));
		            }
	        	
	            if ("ADMIN".equals(role)) {
	               
	            	// da li postoji sa tim mejlom
	                Boolean userExists = usersProxy.getUser(dto.getEmail());
	                if (!userExists) {
	                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with email " + dto.getEmail() + " does not exist.");
	                }

	                // da li ima acc
	                BankAccountModel existingAccount = repository.findByEmail(dto.getEmail());
	                if (existingAccount != null) {
	                    return ResponseEntity.badRequest().body("Bank account for user " + dto.getEmail() + " already in use.");
	                }

	                BankAccountModel bankAccount = new BankAccountModel(dto.getEmail());
	                List<FiatBalanceModel> initialBalances = createInitialBalances(bankAccount);
	                bankAccount.setFiatBalances(initialBalances);
	                repository.save(bankAccount);

	                return ResponseEntity.ok(Collections.singletonMap("message", "Account created successfully for user: " + dto.getEmail()));
	            }


	            Boolean userExists = usersProxy.getUser(dto.getEmail());
	            if (!userExists) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "User with email " + dto.getEmail() + " doesnt exist."));
	            }

	            BankAccountModel existingAccount = repository.findByEmail(dto.getEmail());
	            if (existingAccount != null) {
	                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Bank account for user with email " + dto.getEmail() + " already exists."));
	            }

	            BankAccountModel bankAccount = new BankAccountModel(dto.getEmail());
	            List<FiatBalanceModel> initialBalances = createInitialBalances(bankAccount);
	            bankAccount.setFiatBalances(initialBalances);
	            repository.save(bankAccount);

	            return ResponseEntity.ok(Collections.singletonMap("message", "Bank account created successfully for user: " + dto.getEmail()));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("message", "Error creating bank account: " + e.getMessage()));
	        }
	    }

	    @Override
	    public ResponseEntity<?> updateBankAccount(@PathVariable String email, @RequestBody BankAccountDto dto, @RequestHeader("Authorization") String authorizationHeader) {
	        
	        List<String> requiredCurrencies = Arrays.asList("EUR", "USD", "CHF", "GBP", "CAD", "RSD");

	        
	        String role = usersProxy.getCurrentUserRole(authorizationHeader);
	        String currentUserEmail = usersProxy.getCurrentUserEmail(authorizationHeader);

	       
	        if ("OWNER".equals(role) || "USER".equals(role)) {
	            return ResponseEntity.status(403).body("Not authorized to access this service.");
	        }

	        
	        BankAccountModel bankAccount = repository.findByEmail(email);
	        if (bankAccount == null) {
	            return ResponseEntity.notFound().build();
	        }

	        // Validate input currencies
	        Set<String> inputCurrencies = dto.getFiatBalances().stream()
	                                         .map(FiatBalanceDto::getCurrency)
	                                         .collect(Collectors.toSet());

	        // Find missing currencies, kreira set od requered i izbaci input
	        
	        Set<String> missingCurrencies = new HashSet<>(requiredCurrencies);
	        missingCurrencies.removeAll(inputCurrencies);

	        if (!missingCurrencies.isEmpty()) {
	            String missingCurrenciesList = String.join(", ", missingCurrencies);
	            return ResponseEntity.badRequest().body("Missing required currencies: " + missingCurrenciesList);
	        }

	        // Clear existing balances and update with new balances
	        bankAccount.getFiatBalances().clear();

	        for (FiatBalanceDto fiatBalanceDto : dto.getFiatBalances()) {
	            FiatBalanceModel fiatBalanceModel = new FiatBalanceModel(fiatBalanceDto.getCurrency(), fiatBalanceDto.getBalance());
	            fiatBalanceModel.setBankAccount(bankAccount); 
	            bankAccount.getFiatBalances().add(fiatBalanceModel);
	        }

	        repository.save(bankAccount);

	        return ResponseEntity.ok("Account updated successfully");
	    }



		@Override
		public void deleteBankAccount(@PathVariable String email) {
			BankAccountModel bankAccount = repository.findByEmail(email);
			if (bankAccount != null) {
				repository.delete(bankAccount);
			}
		}
		

		private BankAccountDto convertToDto(BankAccountModel model) {
		    BankAccountDto dto = new BankAccountDto();
		    dto.setEmail(model.getEmail());

		    if (model.getFiatBalances() != null) {
		        List<FiatBalanceDto> fiatBalanceDtos = model.getFiatBalances().stream()
		                .map(fiatBalanceModel -> {
		                    FiatBalanceDto fiatBalanceDto = new FiatBalanceDto();
		                    fiatBalanceDto.setCurrency(fiatBalanceModel.getCurrency());
		                    fiatBalanceDto.setBalance(fiatBalanceModel.getBalance());
		                    return fiatBalanceDto;
		                })
		                .collect(Collectors.toList());
		        dto.setFiatBalances(fiatBalanceDtos);
		    }

		    return dto;
		}


		private List<FiatBalanceModel> createInitialBalances(BankAccountModel bankAccount) {
		    List<FiatBalanceModel> balances = new ArrayList<>();
		    balances.add(new FiatBalanceModel("EUR", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("USD", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("GBP", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("CHF", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("CAD", BigDecimal.ZERO));
		    balances.add(new FiatBalanceModel("RSD", BigDecimal.ZERO));
		    for (FiatBalanceModel balance : balances) {
		        balance.setBankAccount(bankAccount);
		    }
		    return balances;
		}

		 @Override
		    public BankAccountDto getBankAccountByEmail(String email) {
		        BankAccountModel bankAccount = repository.findByEmail(email);
		        if (bankAccount == null) {
		        	
		            return null; 
		        }
		        
		        return convertToDto(bankAccount);
		    }

		
		@Override
		public BankAccountDto getBankAccountForUser(String authorizationHeader) {
			
		    String role = usersProxy.getCurrentUserRole(authorizationHeader);
		    String currentUserEmail = usersProxy.getCurrentUserEmail(authorizationHeader);
		    
		    if ("USER".equals(role) ) {
		        return getBankAccountByEmail(currentUserEmail);
		    }
		    return null;
		}
		
		@Override
		public ResponseEntity<?> updateBalances(@RequestParam("email") String email,
		                                        @RequestParam(value = "from", required = false) String from,
		                                        @RequestParam(value = "to", required = false) String to,
		                                        @RequestParam(value = "quantity", required = false) BigDecimal quantity,
		                                        @RequestParam(value = "totalAmount", required = false) BigDecimal totalAmount) {

		    BankAccountModel bankAccount = repository.findByEmail(email);

		    if (bankAccount == null) {
		        return ResponseEntity.notFound().build();
		    }

		    List<FiatBalanceModel> balances = bankAccount.getFiatBalances();

		    if (from != null) {
		        
		        FiatBalanceModel fromBalance = findFiatBalance(balances, from);
		        if (fromBalance == null) {
		            return ResponseEntity.badRequest().body("Currency '" + from + "' not found.");
		        }
		        BigDecimal newFromBalance = fromBalance.getBalance().subtract(quantity);
		        if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
		            return ResponseEntity.badRequest().body("Not enough " + from);
		        }
		        fromBalance.setBalance(newFromBalance);
		    }

		    if (to != null && totalAmount != null) {
		        
		        FiatBalanceModel toBalance = findFiatBalance(balances, to);
		        if (toBalance == null) {
		            return ResponseEntity.badRequest().body("Currency '" + to + "' not found.");
		        }
		        BigDecimal newToBalance = toBalance.getBalance().add(totalAmount);
		        toBalance.setBalance(newToBalance);
		    }
		  
		    BankAccountModel updatedAccount = repository.save(bankAccount);
		    BankAccountDto updatedDto = convertToDto(updatedAccount);
		    return ResponseEntity.ok(updatedDto);
		}

		private FiatBalanceModel findFiatBalance(List<FiatBalanceModel> balances, String currency) {
		    return balances.stream()
		            .filter(balance -> balance.getCurrency().equalsIgnoreCase(currency))
		            .findFirst()
		            .orElse(null);
		}

		@Override
		public BigDecimal getUserCurrencyAmount(String email, String currencyFrom) {
			BankAccountModel userAccount = repository.findByEmail(email);
			List<FiatBalanceModel> balances = userAccount.getFiatBalances();
			for (FiatBalanceModel accountCurrency : balances) {
				if (accountCurrency.getCurrency().equals(currencyFrom)) {
					return accountCurrency.getBalance();
				}
			}
			return null;
		}



	    
}
