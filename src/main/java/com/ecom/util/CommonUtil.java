package com.ecom.util;

import com.ecom.model.AssetOrder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${aws.s3.bucket.category}")
    private String categoryBucket;

    @Value("${aws.s3.bucket.asset}")
    private String assetBucket;

    // Activate Email
    public Boolean sendActivationMail(String recipientEmail, String url, String userName)
            throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("zedaxisio@gmail.com", "ZedAxis");
        helper.setTo(recipientEmail);

        String content = "<p>Welcome to ZedAxis!</p><br>" +
                "<p>Thank you for registering with us. To complete your registration and activate your account, " +
                "please click the activation link below:</p><br>" +
                "<a href=\"" + url + "\">Activate My Account</a><br><br>" +
                "<p>Best regards,<br>ZedAxis</p>";

        helper.setSubject("Activate Your ZedAxis Account");
        helper.setText(content, true);

        mailSender.send(message);

        return true;
    }

    public Boolean sendMail(String recipientEmail, String url) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("zedaxisio@gmail.com", "ZedAxis");
        helper.setTo(recipientEmail);

        String content = "<p>Hello, </p>" + "<p>You have requested to reset your password.</p>" +
                "<p>Click the link below to reset your password: </p>" + "<p><a href=\"" + url
                + "\">Reset my password</a></p>";

        helper.setSubject("Password Reset");
        helper.setText(content, true);

        mailSender.send(message);

        return true;
    }

    public static String generateUrl(HttpServletRequest request) {

        // http://localhost:8080/forgot-password
        String siteURL = request.getRequestURL().toString();

        return siteURL.replace(request.getServletPath(), "");

    }

    public Boolean sendMailForModelPurchase(AssetOrder order, String status) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("zedaxisio@gmail.com", "ZedAxis");
        helper.setTo(order.getUser().getEmail());

        String content =
                "<p>Hello, </p>" +
                "<p>Your purchase is " + status +".</p><br>" +

                "<p>Model Details: </p>" +
                "<p>Name: " + order.getAsset().getTitle() + "</p>" +
                "<p>Price: ₹" + order.getAsset().getPrice() + "</p>" +
                "<p>Payment Type: " + order.getPaymentType() + "</p><br>" +

                "<a href='http://localhost:8081/user/order'>View your orders</a><br>" +

                "<p>ZedAxis</p>";

        helper.setSubject("Order Status");
        helper.setText(content, true);

        mailSender.send(message);

        return true;

    }

    public String getImageUrl(MultipartFile file, Integer bucketType) {

        String bucketName = null;

        if (bucketType == 1) {
            bucketName = categoryBucket;
        } else if (bucketType == 2) {
            bucketName = assetBucket;
        }

        String fileName = file.isEmpty() ? "nofile.jpg" : file.getOriginalFilename();

        // https://zedaxis-asset.s3.ap-south-1.amazonaws.com/table.jpg
        String url = "https://" +  bucketName + ".s3.ap-south-1.amazonaws.com/" + fileName;

        return url;
    }

}
