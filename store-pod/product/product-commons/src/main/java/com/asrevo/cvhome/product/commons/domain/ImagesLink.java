package com.asrevo.cvhome.product.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Iterator;
import java.util.List;

public record ImagesLink(List<ImageLink> imagesLinks) implements Iterable<ImageLink> {

    public static ImagesLink empty() {
        return new ImagesLink(List.of());
    }

    @Override
    public Iterator<ImageLink> iterator() {
        return imagesLinks.iterator();
    }

   @JsonIgnore
   public int size() {
        return imagesLinks.size();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return imagesLinks.isEmpty();
    }
}
