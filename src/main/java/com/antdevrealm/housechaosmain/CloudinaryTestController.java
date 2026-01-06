package com.antdevrealm.housechaosmain;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test/images")
public class CloudinaryTestController {
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryTestController(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestPart("file") MultipartFile file) throws IOException {
        System.out.println();
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "house-of-chaos",
                        "resource_type", "image"
                )
        );

        return Map.of(
                "public_id", uploadResult.get("public_id"),
                "secure_url", uploadResult.get("secure_url"),
                "bytes", uploadResult.get("bytes"),
                "format", uploadResult.get("format"),
                "width", uploadResult.get("width"),
                "height", uploadResult.get("height")
        );
    }
}
