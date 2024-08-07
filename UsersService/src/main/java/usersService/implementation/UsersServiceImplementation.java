package usersService.implementation;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.UserDto;
import api.services.UsersService;
import usersService.model.UserModel;
import usersService.repository.UsersServiceRepository;

@RestController
public class UsersServiceImplementation implements UsersService {

	@Autowired
	private UsersServiceRepository repo;
	
	@Override
	public List<UserDto> getUsers() {
		List<UserModel> listOfModels = repo.findAll();
		ArrayList<UserDto> listOfDtos = new ArrayList<UserDto>();
		for(UserModel model: listOfModels) {
			listOfDtos.add(convertModelToDto(model));
		}
		return listOfDtos;
	}

	
	@Override
	public ResponseEntity<?> createUser(UserDto dto, @RequestHeader("Authorization") String authorizationHeader) {
	    String role = extractRoleFromAuthorizationHeader(authorizationHeader);

	    if ("USER".equals(role)) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this service.");
	    }

	    if (!isRoleAllowedToCreateUser(role, dto.getRole())) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(getUnauthorizedMessage(role));
	    }

	    UserModel user = convertDtoToModel(dto);

	    if (repo.existsById(user.getId())) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body("User with ID " + user.getId() + " already exists.");
	    }

	    try {
	        UserModel createdUser = repo.save(user);

	        if ("OWNER".equals(user.getRole()) && repo.existsByRole("OWNER")) {
	            repo.delete(createdUser);
	            return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with role 'OWNER' already exists.");
	        }

	        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user: " + e.getMessage());
	    }
	}

	private boolean isRoleAllowedToCreateUser(String role, String targetRole) {
	    switch (role) {
	        case "ADMIN":
	            return "USER".equals(targetRole);
	        case "OWNER":
	            return "USER".equals(targetRole) || "ADMIN".equals(targetRole);
	        default:
	            return false;
	    }
	}
	
	

	@Override
	public ResponseEntity<?> deleteUser(int id, String authorizationHeader) {
		// TODO Auto-generated method stub
		 String role = extractRoleFromAuthorizationHeader(authorizationHeader);
	        
	        UserModel user = repo.findById(id).orElse(null);

	        if (user == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + id + " is not found.");
	        }

	        if ("USER".equals(role) || "ADMIN".equals(role) ) {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No access to this service.");
	        } else if ("ADMIN".equals(role)) {
	            if ("USER".equals(user.getRole())) {
	                repo.deleteById(id);
	               
	                return ResponseEntity.ok("User with ID " + id + " has been deleted.");
	            } else {
	                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin can only delete users with role 'USER'.");
	            }
	        } else if ("OWNER".equals(role)) {
	            repo.deleteById(id);
	            
	        
	            return ResponseEntity.ok("User with ID " + id + " has been deleted.");
	        } else {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized role.");
	        }
	}


	private String getUnauthorizedMessage(String role) {
	    if ("ADMIN".equals(role)) {
	        return "Admin can only create users with role 'USER'.";
	    } else if ("OWNER".equals(role)) {
	        return "Owner can only create users with role 'USER' or 'ADMIN'.";
	    }
	    return "Unauthorized action.";
	}

	
	public UserModel convertDtoToModel(UserDto dto) {
		return new UserModel(dto.getEmail(), dto.getPassword(), dto.getRole());
	}
	
	public UserDto convertModelToDto(UserModel model) {
		return new UserDto(model.getEmail(), model.getPassword(), model.getRole());
	}

	@Override
	public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody UserDto dto, @RequestHeader("Authorization") String authorizationHeader) {
	    String role = extractRoleFromAuthorizationHeader(authorizationHeader);

	    if ("USER".equals(role)) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User does not have access to this service.");
	    }

	    UserModel user = repo.findById(id).orElse(null);

	    if (user == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + id + " not found.");
	    }

	    switch (role) {
	        case "ADMIN":
	            if ("USER".equals(user.getRole())) {
	                if ("OWNER".equals(dto.getRole()) && repo.existsByRole("OWNER")) {
	                    return ResponseEntity.status(HttpStatus.CONFLICT).body("An 'OWNER' already exists.");
	                }
	                updateUserAndSave(user, dto);
	                return ResponseEntity.ok(convertModelToDto(user));
	            } else {
	                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin can only update 'USER'.");
	            }

	        case "OWNER":
	            if ("OWNER".equals(dto.getRole()) && repo.existsByRole("OWNER") && !user.getRole().equals("OWNER")) {
	                return ResponseEntity.status(HttpStatus.CONFLICT).body("An 'OWNER' already exists.");
	            }
	            updateUserAndSave(user, dto);
	            return ResponseEntity.ok(convertModelToDto(user));

	        default:
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized role.");
	    }
	}

	private void updateUserAndSave(UserModel user, UserDto dto) {
	    user.setEmail(dto.getEmail());
	    user.setPassword(dto.getPassword());
	    user.setRole(dto.getRole());
	    repo.save(user);
	}

	

	@Override
	public String getCurrentUserRole(String authorizationHeader) {
        String role = extractRoleFromAuthorizationHeader(authorizationHeader);
        if (role != null) {
            return role;
        } else {
            return "Unauthorized";
        }
    }
	


	@Override
	public String getCurrentUserEmail(String authorizationHeader) {
        String email = extractEmailFromAuthorizationHeader(authorizationHeader);
        if (email != null) {
            return email;
        } else {
            return "Unauthorized";
        }
    }

	public String extractRoleFromAuthorizationHeader(String authorizationHeader) {
	    try {
	        String encodedCredentials = authorizationHeader.replaceFirst("Basic ", "");
	        byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials.getBytes());
	        String decodedCredentials = new String(decodedBytes);
	        String[] credentials = decodedCredentials.split(":");
	        String email = credentials[0]; 
	        UserModel user = repo.findByEmail(email);
	        if (user != null) {
	            return user.getRole();
	        } else {
	            System.out.println("User not found for email: " + email);
	            return null; 
	        }
	    } catch (Exception e) {
	        System.out.println("Error extracting role: " + e.getMessage());
	        return null;
	    }
	}

	
	public String extractEmailFromAuthorizationHeader(String authorizationHeader) {
		String encodedCredentials = authorizationHeader.replaceFirst("Basic ", "");
		byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials.getBytes());
		String decodedCredentials = new String(decodedBytes);
		String[] credentials = decodedCredentials.split(":");
		String role = credentials[0]; 
		return role;
	}


	@Override
	 public Boolean getUser(String email) {
       UserModel user = repo.findByEmail(email);
       return user != null;
   }


}
