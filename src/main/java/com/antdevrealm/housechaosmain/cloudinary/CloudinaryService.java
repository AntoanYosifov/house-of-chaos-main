package com.antdevrealm.housechaosmain.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadSeedImage(byte[] bytes, String folder, String publicId) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "image",
                "transformation", new Transformation<>()
                        .width(2000).height(2000).crop("limit")
                        .quality("auto").fetchFormat("auto")
        ));

        return (String) result.get("public_id");
    }

    public String buildThumbUrl(String publicId) {
        return cloudinary.url()
                .secure(true)
                .transformation(new Transformation<>()
                        .width(400).height(400).crop("fill")
                        .quality("auto").fetchFormat("auto"))
                .generate(publicId);
    }

    public String buildLargeUrl(String publicId) {
        return cloudinary.url()
                .secure(true)
                .transformation(new Transformation<>()
                        .width(1200).crop("limit")
                        .quality("auto").fetchFormat("auto"))
                .generate(publicId);
    }
}
