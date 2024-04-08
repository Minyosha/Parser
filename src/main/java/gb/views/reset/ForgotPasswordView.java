package gb.views.reset;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gb.data.User;
import gb.data.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;

@Route("forgot-password")
@AnonymousAllowed
@JsModule("./scripts/copytoclipboard.js")
public class ForgotPasswordView extends VerticalLayout {
    private EmailField emailField = new EmailField("Email");
    private Button resetButton = new Button("Reset password");
    private UserRepository userRepository;


    public ForgotPasswordView(UserRepository userRepository) {
        this.userRepository = userRepository;
        String resetText = ("Generate new password");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        Button loginButton = new Button("Go to Login page");
        loginButton.addClickListener(e -> {
            UI currentUI = UI.getCurrent();
            if (currentUI != null) {
                currentUI.navigate("login");
            }
        });
        resetButton.addClickListener(e -> {
            resetPassword(userRepository);
        });
        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new H1(resetText), emailField, resetButton, loginButton);
    }

    @Transactional
    private void resetPassword(UserRepository userRepository) {

        String email = emailField.getValue();
        if (!userRepository.existsByEmail(email)) {
            Notification notification = new Notification("User with this email is not registered", 3000);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
            return;
        }
//         ads@123.ru
        if (userRepository.existsByEmail(email)) {
            PasswordEncoder passwordEncoder;
            passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            String username = userRepository.findUsernameByEmail(email);
            String newPassword = generateRandomPassword();
            String newHashedPassword = passwordEncoder.encode(newPassword);
            User userToUpdate = userRepository.findByUsername(username);
            userToUpdate.setHashedPassword(newHashedPassword);
            userRepository.save(userToUpdate);
            Notification notification = new Notification("New password for " + username + " is copied to clipboard! Use Ctrl+V for pasting!", 5000);
            UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)", newPassword);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();


        }
    }

    public String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{}|;:,.<>?";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

}
