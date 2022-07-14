package com.pragma.webflux.client.controller;

import com.pragma.webflux.client.dtos.Image;
import com.pragma.webflux.client.dtos.Product;
import com.pragma.webflux.client.services.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.*;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductHandler {

    @Autowired
    private IProductService productService;

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> list() {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> getProduct(ServerRequest request) {
        return productService
                .findById(request.pathVariable("id"))
                .flatMap(p ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(p)))
                .switchIfEmpty(
                        ServerResponse
                                .notFound()
                                .build());
    }

    public Mono<ServerResponse> createProduct(ServerRequest request) {
        return request
                .bodyToMono(Product.class)
                .flatMap(p -> {
                    if (p.getCreateAt() == null) {
                        p.setCreateAt(new Date());
                    }
                    return productService.save(p);
                })
                .flatMap(p -> ServerResponse
                        .created(URI.create(p.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p)))
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse
                                .badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(errorResponse.getResponseBodyAsString()));
                    }
                    return Mono.error(errorResponse);
                })
                ;
    }

    public Mono<ServerResponse> editProduct(ServerRequest request) {
        return productService
                .findById(request.pathVariable("id"))
                .flatMap(p ->
                        request.bodyToMono(Product.class)
                                .flatMap(product -> {
                                    p.setName(product.getName());
                                    p.setPrice(product.getPrice());
                                    p.setCategory(product.getCategory());
                                    return productService.save(p);
                                })
                )
                .flatMap(p ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(p)))
                .switchIfEmpty(
                        ServerResponse
                                .notFound()
                                .build());
    }

    public Mono<ServerResponse> editProduct2(ServerRequest request) {
        return productService
                .findById(request.pathVariable("id"))
                .zipWith(request.bodyToMono(Product.class), (db, req) ->
                {
                    db.setName(req.getName());
                    db.setPrice(req.getPrice());
                    db.setCategory(req.getCategory());
                    return db;
                })
                .flatMap(p ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(productService.save(p), Product.class))
                .switchIfEmpty(
                        ServerResponse
                                .notFound()
                                .build());
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        return productService
                .findById(request.pathVariable("id"))
                .flatMap(p -> productService.delete(p.getId())
                        .then(ServerResponse
                                .noContent()
                                .build()))
                .switchIfEmpty(
                        ServerResponse
                                .notFound()
                                .build());
    }

    public Mono<ServerResponse> uploadImage(ServerRequest request) {

        return errorHandler(request.multipartData()
                .map(m -> m.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file ->productService
                                .upload(file,request.pathVariable("id")))
                .flatMap(p ->
                        ServerResponse
                                .created(URI.create("images"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(p))));
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response){
        return response.onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if(errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "Product not found: ".concat(Objects.requireNonNull(errorResponse.getMessage())));
                body.put("timestamp", new Date());
                body.put("status", errorResponse.getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND)
                        .body(fromValue(body));
            }
            return Mono.error(errorResponse);
        });
    }
}
