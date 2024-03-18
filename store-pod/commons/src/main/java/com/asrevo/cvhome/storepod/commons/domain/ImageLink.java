package com.asrevo.cvhome.storepod.commons.domain;

import com.asrevo.cvhome.commons.utils.ErrorCodes;
import com.asrevo.cvhome.commons.utils.OperationExecution;

import java.net.MalformedURLException;
import java.net.URL;

public record ImageLink(String imageLink) {
    public ImageLink {
        if (imageLink == null || imageLink.isEmpty())
            throw new OperationExecution(ErrorCodes.image_link_should_be_valid);
        try {
            new URL(imageLink);
        } catch (MalformedURLException e) {
            throw new OperationExecution(ErrorCodes.image_link_should_be_valid);
        }
    }
}
