package com.pragma.webflux.client.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class Product {

    private String id;
    @NotEmpty
    private String name;
    @NotNull(message = "can´t be null")
    private Double price;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;
    @Valid
    @NotNull
    private Category category;
    private Set<Image> images = new HashSet<>();

    public Product(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public Product(String name, Double price, Category category) {
        this(name, price);
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price + '\'' +
                ", " + category +
                ", " + images +
                '}';
    }
}
