package com.asrevo.cvhome.uaa.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StaticController {

	@Bean
	RouterFunction<ServerResponse> indexRouter() {
		return route(request -> {
			if (request.path().startsWith("/api/"))
				return false;
			if (request.path().startsWith("/oauth2/"))
				return false;
			return !request.path().contains(".");
		}, request -> ServerResponse.ok().body(new ClassPathResource("static/index.html")));
	}

}
