package com.pragma.webflux.client.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterFunctionConfig {

    private static final String PATH = "/api/client/";
    private static final String PATH_VARIABLE="{id}";

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler) {
        return route(GET(PATH),serverRequest->handler.list())
                .andRoute(GET(PATH + PATH_VARIABLE), handler::getProduct)
                .andRoute(POST(PATH), handler::createProduct)
                .andRoute(PUT(PATH + PATH_VARIABLE), handler::editProduct2)
                .andRoute(DELETE(PATH + PATH_VARIABLE), handler::deleteProduct)
                .andRoute(POST(PATH+ "upload/"+PATH_VARIABLE), handler::uploadImage)
                ;
    }
}
