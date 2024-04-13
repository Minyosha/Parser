package gb.views.banned;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gb.security.AuthenticatedUser;

@PageTitle("Banned")
@Route(value = "banned-view")
@AnonymousAllowed
public class BannedView extends VerticalLayout {
    private AuthenticatedUser authenticatedUser;

    public BannedView(AuthenticatedUser authenticatedUser) {
        String bannedText = "You are banned.";
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Button signOutButton = new Button("Sign out");
        signOutButton.addClickListener(e -> authenticatedUser.logout());

        add(new H1(bannedText), signOutButton);
    }
}