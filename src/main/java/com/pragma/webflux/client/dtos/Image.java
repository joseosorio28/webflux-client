package com.pragma.webflux.client.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Image {


    private String id;
    private String base64;

    public Image(String base64) {
        this.base64 = base64;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id='" + id + '\'' +
                ", base64='" + base64 + '\'' +
                '}';
    }
}
