package io.swkoreatech.kosp.domain.upload.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.domain.upload.model.ImageUploadDomain;

@Component
public class ImageUploadDomainConverter implements Converter<String, ImageUploadDomain> {

    @Override
    public ImageUploadDomain convert(String source) {
        return ImageUploadDomain.from(source);
    }
}
