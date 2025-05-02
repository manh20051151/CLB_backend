package iuh.fit.backend.Event.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.repository.EventRepository;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public String generateAndSaveQrCode(String userId) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Đảm bảo dữ liệu QR code không null
        String qrCodeData = generateQrCodeData(user);

        // Tạo QR code
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 300, 300);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        } catch (WriterException | IOException e) {
            throw new AppException(ErrorCode.QR_CODE_GENERATION_FAILED);
        }

        byte[] qrCodeImage = outputStream.toByteArray();

        // Upload lên Cloudinary
        String qrCodeUrl = cloudinaryService.uploadImage(qrCodeImage, "qr_codes");

        // Lưu vào user
        user.setQrCodeData(qrCodeData);
        user.setQrCodeUrl(qrCodeUrl);
        userRepository.save(user);

        return qrCodeUrl;
    }

    public String regenerateQrCode(String userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String qrCodeUrl = generateAndSaveQrCode(user.getId());
        return qrCodeUrl;
    }

    private String generateQrCodeData(User user) {
        // Tạo dữ liệu QR code với đầy đủ thông tin
        return String.format("USER:%s|NAME:%s|TIME:%d",
                user.getId(),
                user.getLastName(),
                System.currentTimeMillis());
    }

    public String generateAndSaveQrCodeEvent(String eventId) throws IOException {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Đảm bảo dữ liệu QR code không null
        String qrCodeData = generateQrCodeDataEvent(event);

        // Tạo QR code
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 300, 300);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        } catch (WriterException | IOException e) {
            throw new AppException(ErrorCode.QR_CODE_GENERATION_FAILED);
        }

        byte[] qrCodeImage = outputStream.toByteArray();

        // Upload lên Cloudinary
        String qrCodeUrl = cloudinaryService.uploadImage(qrCodeImage, "qr_codes");

        // Lưu vào user
        event.setQrCodeData(qrCodeData);
        event.setQrCodeUrl(qrCodeUrl);
        eventRepository.save(event);

        return qrCodeUrl;
    }

    public String regenerateQrCodeEvent(String eventId) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        String qrCodeUrl = generateAndSaveQrCodeEvent(event.getId());
        return qrCodeUrl;
    }

    private String generateQrCodeDataEvent(Event event) {
        // Tạo dữ liệu QR code với đầy đủ thông tin
        return String.format("USER:%s|NAME:%s|TIME:%d",
                event.getId(),
                event.getName(),
                System.currentTimeMillis());
    }
}
