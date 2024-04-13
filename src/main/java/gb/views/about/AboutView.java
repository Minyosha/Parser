package gb.views.about;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gb.data.Role;
import gb.data.User;
import gb.security.AuthenticatedUser;
import gb.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("About")
@Route(value = "about-view", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class AboutView extends Composite<VerticalLayout> implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;

    public AboutView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
    }

    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            User user = authenticatedUser.get().get();
            if (user.isBanned()) {
                event.forwardTo("banned-view");
            }
        }
    }
}
