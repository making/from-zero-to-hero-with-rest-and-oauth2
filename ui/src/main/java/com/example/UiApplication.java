package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.hateoas.Resources;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Date;

@SpringBootApplication
@EnableOAuth2Sso
@Controller
public class UiApplication {
	@Autowired
	OAuth2RestTemplate restTemplate;
	@Value("${message.api:http://localhost:18080/messages}")
	String messageApi;

	@RequestMapping(path = "/", method = RequestMethod.GET)
	String home(Model model) {
		@SuppressWarnings("unchecked")
		Resources<Message> messages = restTemplate.getForObject(messageApi + "?sort=createdAt,DESC", Resources.class);
		model.addAttribute("messages", messages.getContent());
		return "index";
	}

	@RequestMapping(path = "/messages", method = RequestMethod.POST)
	String post(@RequestParam String text) {
		restTemplate.postForObject(messageApi, Collections.singletonMap("text", text), Void.class);
		return "redirect:/";
	}

	static class Message {
		public String text;
		public String username;
		public Date createdAt;
	}

	public static void main(String[] args) {
		SpringApplication.run(UiApplication.class, args);
	}
}
