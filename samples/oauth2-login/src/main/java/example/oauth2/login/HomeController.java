package example.oauth2.login;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class HomeController {
	@GetMapping("/")
	String home() {
		return "index";
	}
}
