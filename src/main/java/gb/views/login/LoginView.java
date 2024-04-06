package gb.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gb.security.AuthenticatedUser;


@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
@RouteAlias(value = "")

public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;

    private final LoginForm login = new LoginForm();

    public LoginView(AuthenticatedUser authenticatedUser){
        this.authenticatedUser = authenticatedUser;
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setAction("login");

        Button registerButton = new Button("Register", e -> {
            UI currentUI = UI.getCurrent();
            if (currentUI != null) {
                currentUI.navigate("register");
            }
        });

        add(new H1("Welcome to Parser!"), login, registerButton);
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            event.forwardTo("about-view");
        }
    }
}