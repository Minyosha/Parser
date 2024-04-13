package gb.views.register;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gb.data.Role;
import gb.data.User;
import gb.data.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Route("register")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private TextField nameField = new TextField("Name", "Ivan Petrov");
    private TextField usernameField = new TextField("Username", "Your login");
    private PasswordField passwordField = new PasswordField("Password", "Your password");
    private EmailField emailField = new EmailField("Email", "Your email");
    private Button registerButton = new Button("Register account");
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public RegisterView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        String registerText = ("Enter your details below:");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        registerButton.addClickListener(e -> register());

        Button loginButton = new Button("Go to Login page");
        loginButton.addClickListener(e -> {
            UI currentUI = UI.getCurrent();
            if (currentUI != null) {
                currentUI.navigate("login");
            }
        });
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new H1(registerText),usernameField, nameField, emailField, passwordField, registerButton, loginButton);
    }

    @Transactional
    private void register() {

        String name = nameField.getValue();
        String username = usernameField.getValue();
        String password = passwordField.getValue();
        String email = emailField.getValue();

        if (userRepository.existsByUsername(username)) {
            Notification notification = new Notification("User with this username already registered", 3000);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
            return;
        }

        if (userRepository.existsByEmail(email)) {
            Notification notification = new Notification("User with this email already registered", 3000);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setHashedPassword(passwordEncoder.encode(password));
        newUser.setRoles(Set.of(Role.USER)); // или любую другую роль по умолчанию
        newUser.setBanned(false);
        userRepository.save(newUser);

        // После успешной регистрации перенаправляем на страницу успеха
        UI currentUI = UI.getCurrent();
        if (currentUI != null) {
            currentUI.navigate("regsuccess");
        }
    }
}
