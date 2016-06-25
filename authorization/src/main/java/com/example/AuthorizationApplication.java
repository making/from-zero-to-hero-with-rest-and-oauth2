package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@EnableAuthorizationServer
@EnableResourceServer
@RestController
public class AuthorizationApplication extends AuthorizationServerConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(AuthorizationApplication.class, args);
	}

	@RequestMapping(path = "/userinfo", method = RequestMethod.GET)
	Object userinfo(Authentication authentication) {
		return authentication;
	}

	@Configuration
	static class MvcConfig extends WebMvcConfigurerAdapter {
		@Override
		public void addViewControllers(ViewControllerRegistry registry) {
			registry.addViewController("login").setViewName("login");
		}
	}

	@Configuration
	@Order(-5) // prior to AuthorizationServerSecurityConfiguration (order = 0)
	static class LoginConfig extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.requestMatchers()
					.antMatchers("/login", "/oauth/authorize", "/oauth/confirm_access")
					.and()
					.authorizeRequests()
					.anyRequest().authenticated()
					.and()
					.formLogin().loginPage("/login").permitAll();
		}
	}

	@Autowired
	AuthenticationManager authenticationManager;

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory().withClient("demo")
				.secret("demo")
				.scopes("openid")
				.authorizedGrantTypes("password", "authorization_code");
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()");
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(authenticationManager)
				.accessTokenConverter(jwtAccessTokenConverter());
	}

	@ConfigurationProperties("jwt")
	@Bean
	JwtAccessTokenConverter jwtAccessTokenConverter() {
		return new JwtAccessTokenConverter();
	}
}
