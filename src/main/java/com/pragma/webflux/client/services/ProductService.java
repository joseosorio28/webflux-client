package com.pragma.webflux.client.services;

import com.pragma.webflux.client.dtos.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Service
public class ProductService implements IProductService {

    @Autowired
    private WebClient client;

    @Override
    public Flux<Product> findAll() {
        return client
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Product.class)
                ;
    }

    @Override
    public Mono<Product> findById(String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        return client
                .get()
                .uri("/{id}", params)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .bodyToMono(Product.class)
                ;
    }

    @Override
    public Mono<Product> save(Product product) {
        return client
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(fromValue(product))
                .retrieve()
                .bodyToMono(Product.class)
                ;
    }

    @Override
    public Mono<Product> edit(String id, Product product) {
        return client
                .put()
                .uri("/{id}", Collections.singletonMap("id", id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(fromValue(product))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .bodyToMono(Product.class)
                ;
    }

    @Override
    public Mono<Void> delete(String id) {
        return client
                .delete()
                .uri("/{id}", Collections.singletonMap("id", id))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .toBodilessEntity()
                .then()
                ;
    }

    @Override
    public Mono<Product> upload(FilePart file, String id) {
        MultipartBodyBuilder part = new MultipartBodyBuilder();
        part
                .asyncPart("file",file.content(), DataBuffer.class)
                .headers(h->{
                    h.setContentDispositionFormData("file", file.filename());
                });
        return client
                .post()
                .uri("/upload/{id}", Collections.singletonMap("id", id))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(fromValue(part.build()))
                .retrieve()
                .bodyToMono(Product.class)
                ;
    }
}
