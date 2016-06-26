package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SpringBootApplication
@EnableOAuth2Sso
@EnableZuulProxy
@Controller
public class UiApplication {

	@RequestMapping(path = "/", method = RequestMethod.GET)
	String home(Model model) {
		return "index";
	}

	public static void main(String[] args) {
		SpringApplication.run(UiApplication.class, args);
	}
}
