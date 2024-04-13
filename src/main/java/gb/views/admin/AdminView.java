package gb.views.admin;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import gb.data.Role;
import gb.data.User;
import gb.security.AuthenticatedUser;
import gb.services.UserService;
import gb.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Administration")
@Route(value = "admin-view", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class AdminView extends Composite<VerticalLayout> implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;

    public AdminView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        Grid multiSelectGrid = new Grid(User.class);
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        multiSelectGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        multiSelectGrid.setWidth("100%");
        multiSelectGrid.getStyle().set("flex-grow", "0");
        setGridSampleData(multiSelectGrid);
        getContent().add(multiSelectGrid);
    }

    private void setGridSampleData(Grid grid) {
        grid.setItems(query -> userService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());

        grid.setColumns("id", "banned", "username", "name", "roles", "email");
    }

    @Autowired()
    private UserService userService;

    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            User user = authenticatedUser.get().get();
            if (user.isBanned()) {
                event.forwardTo("banned-view");
            }
        }
    }
}
