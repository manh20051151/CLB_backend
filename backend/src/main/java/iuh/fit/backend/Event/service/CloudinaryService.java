package iuh.fit.backend.Event.service;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("resource_type", "auto");

        File uploadedFile = convertMultiPartToFile(file);
        Map<?, ?> uploadResult = cloudinary.uploader().upload(uploadedFile, params);
        uploadedFile.delete(); // Xóa file tạm sau khi upload

        return (String) uploadResult.get("secure_url");
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}