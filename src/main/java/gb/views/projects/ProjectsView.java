package gb.views.projects;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gb.data.SamplePerson;
import gb.data.User;
import gb.security.AuthenticatedUser;
import gb.services.SamplePersonService;
import gb.views.MainLayout;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@PageTitle("Projects")
@Route(value = "projects-view", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ProjectsView extends Composite<VerticalLayout> implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;
    private TextField firstNameFilter = new TextField();
    private TextField emailFilter = new TextField();
    private Grid<SamplePerson> grid = new Grid<>(SamplePerson.class); // Объявите grid как поле класса

    @Autowired
    private SamplePersonService samplePersonService;

    public ProjectsView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setWidth("100%");
        grid.getStyle().set("flex-grow", "0");

        setGridSampleData(); // Изменил вызов метода, убрав параметр
        getContent().add(grid);

        firstNameFilter.setPlaceholder("Filter by name...");
        emailFilter.setPlaceholder("Filter by email...");

        firstNameFilter.addValueChangeListener(e -> updateList());
        emailFilter.addValueChangeListener(e -> updateList());

        getContent().add(firstNameFilter, emailFilter, grid); // Здесь grid уже объявлен как поле класса
        updateList(); // Load initial data
    }

    private void setGridSampleData() {
        grid.setItems(query -> samplePersonService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
    }

    private void updateList() {
        grid.setItems(query -> {
            String nameValue = firstNameFilter.getValue();
            String emailValue = emailFilter.getValue();
            Page<SamplePerson> page = samplePersonService.findByFirstNameAndEmail(
                    nameValue, emailValue,
                    PageRequest.of(query.getPage(), query.getPageSize(),
                            VaadinSpringDataHelpers.toSpringDataSort(query))
            );
            return page.stream(); // Убедитесь, что page имеет тип Page<SamplePerson>
        });
    }

    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            User user = authenticatedUser.get().get();
            if (user.isBanned()) {
                event.forwardTo("banned-view");
            }
        }
    }

    // ... остальная часть класса ...
}
