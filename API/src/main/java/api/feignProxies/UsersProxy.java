package api.feignProxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("users-service")
public interface UsersProxy {

	@GetMapping("/users/by-email/{email}")
	public Boolean getUser(@PathVariable("email") String email);
	
	
}
