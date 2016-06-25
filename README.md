
## Project作成

新しいディレクトリを作成して、Empty Project作成

----

## Resource Server作成

* spring-boot-starter-data-jpa
* spring-boot-starter-data-rest
* h2
* dev-tools

----

### Messageクラス作成

``` java
package com.example;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Message {
    @Id
    @GeneratedValue
    public Integer id;
    public String text;
}
```

----

### MessageRepositoryクラス作成

``` java
package com.example;

import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Integer> {
}

```

----

### サーバーのポート変更

``` properties
server.port=18080
```

----


#### アプリケーション実行([`01-02`](https://github.com/making/jjug-night-seminar-spring-boot/tree/01-02))

http://localhost:18080/

* HAL
    * http://stateless.co/hal_specification.html 
    * https://tools.ietf.org/html/draft-kelly-json-hal-06
* ALPS
    * http://alps.io/spec/
    * https://spring.io/blog/2014/07/14/spring-data-rest-now-comes-with-alps-metadata
* サンプルデータをPOST

``` bash
curl http://localhost:18080/messages -H "Content-Type:application/json" -d '{"text" : "Hello World!"}'
```

http://localhost:18080/messages

----

### HAL Browser([`01-02`](https://github.com/making/jjug-night-seminar-spring-boot/tree/01-02))

``` xml
		<dependency>
			<groupId>org.springframework.data</groupId>
			<artifactId>spring-data-rest-hal-browser</artifactId>
		</dependency>
```

再起動して

http://localhost:18080/

----

### PagingRespoitory([`01-03`](https://github.com/making/jjug-night-seminar-spring-boot/tree/01-03))

``` java
package com.example;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface MessageRepository extends PagingAndSortingRepository<Message, Integer> {
}

```

ソートのデモ

----


### MessageEventHandlerクラスの作成([`01-04`](https://github.com/making/jjug-night-seminar-spring-boot/tree/01-04))


``` java
package com.example;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Message {
    @Id
    @GeneratedValue
    public Integer id;
    public String text;
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;
}

```

``` java
package com.example;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RepositoryEventHandler(Message.class)
public class MessageEventHandler {

    @HandleBeforeCreate
    public void beforeCreate(Message message) {
        message.createdAt = new Date();
    }
}
```

----

## Authorization Server作成

* cloud oauth2
* devtools

### `@EnableAuthorizationServer`


``` java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

@SpringBootApplication
@EnableAuthorizationServer
public class AuthorizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthorizationApplication.class, args);
	}
}

```

----

### 諸々設定

``` properties
security.oauth2.client.client-id=demo
security.oauth2.client.client-secret=demo
security.oauth2.client.scope=openid
security.oauth2.client.authorized-grant-types=password,authorization_code
security.user.password=password
server.port=18081
server.context-path=/uaa
``` 

----

### アプリケーション実行([`02-01`](https://github.com/making/jjug-night-seminar-spring-boot/tree/02-01))

``` bash
curl -u demo:demo http://localhost:18081/uaa/oauth/token -d grant_type=password -d username=user -d password=password
```

----

### `/userinfo`エンドポイント([`02-02`](https://github.com/making/jjug-night-seminar-spring-boot/tree/02-02))

``` java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAuthorizationServer
@EnableResourceServer
@RestController
public class AuthorizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthorizationApplication.class, args);
	}

	@RequestMapping(path = "/userinfo", method = RequestMethod.GET)
	Object userinfo(Authentication authentication) {
		return authentication;
	}
}
```

再起動して

``` bash
curl http://localhost:18081/uaa/userinfo -H 'Authorization: Bearer <token>'
```

----

### Resource Serverのセキュア化([`02-03`](https://github.com/making/jjug-night-seminar-spring-boot/tree/02-03))

``` xml
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>Brixton.SR1</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

``` xml
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-oauth2</artifactId>
		</dependency>
```

``` java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
public class ResourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceApplication.class, args);
	}
}
```

``` properties
server.port=18080
security.oauth2.resource.user-info-uri=http://localhost:18081/uaa/userinfo
logging.level.org.springframework.web.client.RestTemplate=DEBUG
```

``` bash
curl http://localhost:18080/messages 
# 401 error
```

``` bash
curl http://localhost:18080/messages -H 'Authorization: Bearer <token>'
# 200
```

----


### `username`フィールド追加([`02-04`](https://github.com/making/jjug-night-seminar-spring-boot/tree/02-04))

``` java
package com.example;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Message {
    @Id
    @GeneratedValue
    public Integer id;
    public String text;
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;
    public String username;
}
```

``` java
package com.example;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RepositoryEventHandler(Message.class)
public class MessageEventHandler {

    @HandleBeforeCreate
    public void beforeCreate(Message message) {
        message.createdAt = new Date();
        message.username = SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
```

``` bash
curl http://localhost:18080/messages -H "Content-Type:application/json" -d '{"text" : "Hello World!"}' -H 'Authorization: Bearer <token>' 
```

----

## UIサーバー作成

* web
* thymeleaf
* hateoas
* cloud oauth2
* devtools

----

### UIサーバー作成([`03-01`](https://github.com/making/jjug-night-seminar-spring-boot/tree/03-01))

``` java
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

```

``` html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title>OAuth2 SSO Demo</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/uikit/2.26.3/css/uikit.gradient.min.css"/>
</head>

<body>

<div class="uk-grid">
    <div class="uk-width-1-5"></div>
    <div class="uk-width-3-5">
        <h1>Demo</h1>
        <p>Logged in as: <span th:text="${#httpServletRequest.remoteUser}">demo</span></p>
        <form th:action="@{/messages}" class="uk-panel uk-panel-box uk-form" method="post">
            <input class="uk-form-large" type="text" name="text" placeholder="Message"/>
            <button class="uk-button uk-button-primary uk-button-large">Send</button>
        </form>
        <h2>Messages</h2>
        <div class="uk-panel uk-panel-box" th:each="message : ${messages}">
            <h3 class="uk-panel-title"><span th:text="${message.username}"></span> @ <span
                    th:text="${message.createdAt}"></span></h3>
            <span th:text="${message.text}"></span>
        </div>
    </div>
    <div class="uk-width-1-5"></div>
</div>
</body>
</html>
```

``` properties
auth-server=http://localhost:18081/uaa
security.oauth2.client.client-id=demo
security.oauth2.client.client-secret=demo
security.oauth2.client.scope=openid
security.oauth2.client.access-token-uri=${auth-server}/oauth/token
security.oauth2.client.user-authorization-uri=${auth-server}/oauth/authorize
security.oauth2.resource.user-info-uri=${auth-server}/userinfo
```

http://localhost:8080

----

### Authorization Serverにログインフォーム作成([`03-02`](https://github.com/making/jjug-night-seminar-spring-boot/tree/03-02))

``` java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@EnableAuthorizationServer
@EnableResourceServer
@RestController
public class AuthorizationApplication {

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
}
```

``` html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" class="uk-height-1-1">
<head>
    <meta charset="UTF-8"/>
    <title>OAuth2 SSO Demo</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/uikit/2.26.3/css/uikit.gradient.min.css"/>
</head>

<body class="uk-height-1-1">

<div class="uk-vertical-align uk-text-center uk-height-1-1">
    <div class="uk-vertical-align-middle" style="width: 250px;">
        <h1>Login Form</h1>

        <p class="uk-text-danger" th:if="${param.error}">
            Login failed ...
        </p>

        <p class="uk-text-success" th:if="${param.logout}">
            Logout succeeded.
        </p>

        <form class="uk-panel uk-panel-box uk-form" method="post" th:action="@{/login}">
            <div class="uk-form-row">
                <input class="uk-width-1-1 uk-form-large" type="text" placeholder="Username" name="username"
                       value="user"/>
            </div>
            <div class="uk-form-row">
                <input class="uk-width-1-1 uk-form-large" type="password" placeholder="Password" name="password"
                       value="password"/>
            </div>
            <div class="uk-form-row">
                <button class="uk-width-1-1 uk-button uk-button-primary uk-button-large">Login</button>
            </div>
        </form>

    </div>
</div>
</body>
</html>
```

``` xml
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-thymeleaf</artifactId>
		</dependency>
```

----

## Authorization ServerとしてGitHub APIを使う([`04-01`](https://github.com/making/jjug-night-seminar-spring-boot/tree/04-01))

``` properties
auth-server=https://github.com/login
security.oauth2.client.client-id=cab5411d0c4276b6f1f7
security.oauth2.client.client-secret=c692102464002afefe7d2881cca4f2322350eb5d
security.oauth2.client.scope=user
security.oauth2.client.access-token-uri=${auth-server}/oauth/access_token
security.oauth2.client.user-authorization-uri=${auth-server}/oauth/authorize
security.oauth2.resource.user-info-uri=https://api.github.com/user
```

``` properties
server.port=18080
security.oauth2.resource.user-info-uri=https://api.github.com/user
```

----

## JWT対応

----

### Autorization ServerのJWT対応([`05-01`](https://github.com/making/jjug-night-seminar-spring-boot/tree/05-01))

``` java
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
```

`application.properties`を削除して`application.yml`を作成

``` yaml
security.user.password: password
server.port: 18081
server.context-path: /uaa
# openssl genrsa -out private.pem 2048
# openssl rsa -in private.pem -outform PEM -pubout -out public.pem
jwt:
  verifier-key: |
    -----BEGIN PUBLIC KEY-----
    MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw8+80taiPB0yk/Q34mWB
    fCktmlRZTSTaMBVToeJtbeMK2LAt0Ykf8nM7QmNVORDGzB7lRIYKPaF1l5uhIdqw
    P6G2Th0nWX0aLOaDOx9Z+Ll+E9H7tvBKo1dudmCtJyXWvEJAt30Re6DQz2XFaLDY
    4BdBoCfFQs73KxNLMtHi7MA1kzPzBmOlwA0iu9b0NoQd28ks5vRBFduF9e5M0Z34
    3UVIXUn8L4XNOkex6LWs970pfcMS4iw3u9uog3arKLdJgW0+1c54VDsF3athoilU
    eGBHyw3IYbzV8cZcbyvN6mbHgcNJdTZ0YEwFOPuz9Lt1DrSGWrcXdpdVmRbWl6iv
    +wIDAQAB
    -----END PUBLIC KEY-----
  signing-key: |
    -----BEGIN RSA PRIVATE KEY-----
    MIIEngIBAAKCAQEAw8+80taiPB0yk/Q34mWBfCktmlRZTSTaMBVToeJtbeMK2LAt
    0Ykf8nM7QmNVORDGzB7lRIYKPaF1l5uhIdqwP6G2Th0nWX0aLOaDOx9Z+Ll+E9H7
    tvBKo1dudmCtJyXWvEJAt30Re6DQz2XFaLDY4BdBoCfFQs73KxNLMtHi7MA1kzPz
    BmOlwA0iu9b0NoQd28ks5vRBFduF9e5M0Z343UVIXUn8L4XNOkex6LWs970pfcMS
    4iw3u9uog3arKLdJgW0+1c54VDsF3athoilUeGBHyw3IYbzV8cZcbyvN6mbHgcNJ
    dTZ0YEwFOPuz9Lt1DrSGWrcXdpdVmRbWl6iv+wIDAQABAoIBAGbGPFd1bEWdFZTu
    k/5yRJpEirj8GLsS4qpmJzVKwSDyEBlXr1TRYkFBFhNsS6jeE1nDxZZHvExI2I2w
    k8xPGj7rw+IQDz07GmgCvVJkzDZuNax1hGaqjKJbG5FqgLeRdrntFEa6kFiROcgB
    pLGkUNCdpEZJWZINS1ICmG+eFjsoYrm/PlbaMVPpPBbWyT6zP8Gtl/d1h8QqLvMH
    rAZQLSLI1meP8fPVRfiMTz/9XL2p2k2Fwl0Cjy2PdvztxjAcf9KanWZVlYgjFUKu
    nH/joPZ47c8rCjXjGxnYzx/5r/bgjMiN5ON4PlWoHbb7t+LoDNQS+684JY5UF+yD
    a6QSCJECgYEA/I2wQlX59afxZel2hHW2UfCgvM0SKP+WXjZvOyXAf+9aSnRq34C0
    K9IhPV0S8wG//MnjpkuvdiesYgSwRc2M7eJQBCh4onyRtHvzmp56xGBaV3Mcmr7U
    bI3CXBOh3XiCrQTWiAp4O1fw7xRM4k+xQxnBIia8QydQiHuXBglq0iUCgYEAxnvR
    PP2SEYNVLV0UY/u2oftXo7pXv2Ra5LLmPbrWaANpVpbLmhogq0qhylpVVgSlV0b2
    NpxQskbPT3PrH23eyhJkXNSE4vFka/AD9npENMax0u5MxItV9HNF42LmkQP+ioBM
    cBPS0xLvdfJ7PMOwCoqhrUUc+9MgeLcRhMwAD58CfxetMNkHNmfRVtA3EuVYI7+K
    z4bjstAlJfOvJr5ky6cyyUQxpdNOPZXeHT+jeiNOBrGsO6tbbRemoIKP7fadTpj+
    noHFSYnyI7bH6A6WzOucHNwV7piTS1bh81augH5Czf1qdrJKSPtHQ08mmA6faf7g
    azrIZt3k0af57i6is2ECgYAtR7vxRQvYsPRyl+R59sewZm2U7YLtJ9DQkbuS45fc
    PMMAPQCP1FwVl74XMsp7CC5MHeU3iiuisCLFRnWiQudrhAyfXOBAAb/eG/Bbf4Ml
    4xpYO4MJ55uhW9bazAo7lQSxxhkqDGuxOYWli77vmtkDXqMJq1W0YXLr9x+pAv0U
    kwJ/Y29rEhpdYoR+QIa0H4HRqAtFUeH4LN/i+DzrzUYQAm8BYsP7jHwPTWft6E/1
    9pXrVamMk6mrZRW530AcH3yhNqcGvFMdVkR/rSAZNtZRr1bHpseyB+HDPsl3CfIg
    aS4FcEV2ayC1VVrlcNsPWvmG6Wry0I3jrSJt1vRoh9ZJDQ==
    -----END RSA PRIVATE KEY-----
```

----

### ClientのJWT対応([`05-02`](https://github.com/making/jjug-night-seminar-spring-boot/tree/05-02))

``` properties
auth-server=http://localhost:18081/uaa
security.oauth2.client.client-id=demo
security.oauth2.client.client-secret=demo
security.oauth2.client.scope=openid
security.oauth2.client.access-token-uri=${auth-server}/oauth/token
security.oauth2.client.user-authorization-uri=${auth-server}/oauth/authorize
security.oauth2.resource.jwt.key-uri=${auth-server}/oauth/token_key
```

``` properties
server.port=18080
security.oauth2.resource.jwt.key-uri=http://localhost:18081/uaa/oauth/token_key
```

## Maki-UAA

``` bash
git clone https://github.com/maki-home/uaa.git
cd uaa
mvn package -DskipTests=true
java -jar target/uaa-0.0.1-SNAPSHOT.jar --server.port=18081
```

http://localhost:18081/uaa/

`maki@example.com` / `demo`