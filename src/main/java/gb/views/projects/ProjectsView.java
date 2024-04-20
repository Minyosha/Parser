package gb.views.projects;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import gb.data.Projects;
import gb.data.User;
import gb.security.AuthenticatedUser;
import gb.services.ProjectsService;
import gb.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.grid.Grid;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.TabSheet;
import org.springframework.data.domain.PageRequest;

@PageTitle("Projects")
@Route(value = "projects-view", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ProjectsView extends Composite<VerticalLayout> implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;
    private TextField titleTextField = new TextField();
    private TextField descriptionTextField = new TextField();

    @Autowired
    private ProjectsService sampleProjectService;

    public ProjectsView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        getContent().setWidth("100%");
        getContent().setHeight("100%"); // Установите высоту в 100%
        getContent().getStyle().set("flex-grow", "1");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull(); // Установите размер во весь доступный размер
        setTabSheetSampleData(tabSheet);
        getContent().add(tabSheet);
    }


    private void setTabSheetSampleData(TabSheet tabSheet) {
        titleTextField.setLabel("Title");
        descriptionTextField.setLabel("Description");

        // Create button
        Button createProjectButton = new Button("Create project");
        createProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createProjectButton.setWidth("192px");

        // Update button
        Button updateProjectButton = new Button("Update project");
        updateProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateProjectButton.setWidth("192px");

        // Delete button
        Button deleteProjectButton = new Button("Delete project");
        deleteProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteProjectButton.setWidth("192px");

        // Select project and go to modify page
        Button selectProjectButton = new Button("Modify project");
        selectProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectProjectButton.setWidth("192px");

        // Create a Grid.
        Grid stripedGridUsers = new Grid(Projects.class);
        stripedGridUsers.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        stripedGridUsers.setHeightFull();
        stripedGridUsers.setWidthFull();
        stripedGridUsers.getStyle().set("flex-grow", "1");

        // Create a VerticalLayout to stack components vertically.
        VerticalLayout createSelectProjectLayout = new VerticalLayout();
//        createSelectProjectLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        createSelectProjectLayout.setHeightFull();
        createSelectProjectLayout.setWidthFull();
        createSelectProjectLayout.add(new Text("On this page you can create, delete and select project to modify"));
//        createSelectProjectLayout.getStyle().set("flex-grow", "1");
//        createSelectProjectLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);

        // Create a HorizontalLayout to stack components horizontally.
        HorizontalLayout horizontalLayoutForCreateAndSelect = new HorizontalLayout();
        horizontalLayoutForCreateAndSelect.setWidthFull(); // Set width to full
        horizontalLayoutForCreateAndSelect.setHeightFull();
        horizontalLayoutForCreateAndSelect.getStyle().set("flex-grow", "1");

        createSelectProjectLayout.add(horizontalLayoutForCreateAndSelect);

        VerticalLayout verticalLayoutForCreatingProject = new VerticalLayout();
        verticalLayoutForCreatingProject.setWidthFull(); // Set width to full
        verticalLayoutForCreatingProject.setHeightFull();
        verticalLayoutForCreatingProject.getStyle().set("flex-grow", "1");


        verticalLayoutForCreatingProject.add(titleTextField, descriptionTextField, createProjectButton, updateProjectButton, selectProjectButton, deleteProjectButton);
        verticalLayoutForCreatingProject.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        verticalLayoutForCreatingProject.setMaxWidth("220px");
        horizontalLayoutForCreateAndSelect.add(verticalLayoutForCreatingProject, stripedGridUsers);



        VerticalLayout downloadClientLayout = new VerticalLayout();


        // Now add the VerticalLayout to the "Create and select project" tab.
        tabSheet.add("Download desktop client", downloadClientLayout);
        tabSheet.add("Create and select project", createSelectProjectLayout);
        tabSheet.add("Modify project", new Div(new Text("On this page you can modify selected project")));

        setGridProjectData(stripedGridUsers);

        // Add an event listener to the "Create project" button.
        createProjectButton.addClickListener(e -> {
            if (titleTextField.isEmpty()) {
                Notification.show("Title cannot be empty", 3000, Notification.Position.MIDDLE);
            } else {
                Projects project = new Projects();
                project.setTitle(titleTextField.getValue());
                project.setDescription(descriptionTextField.getValue());
                sampleProjectService.createProject(project);
                setGridProjectData(stripedGridUsers);
            }
        });

        // Add an event listener to the "Select project" button.
        selectProjectButton.addClickListener(e -> {
            if (stripedGridUsers.getSelectedItems().isEmpty()) {
                Notification.show("No project selected", 3000, Notification.Position.MIDDLE);
            } else {
                tabSheet.setSelectedIndex(2); // Index of the third tab (0-based)
            }

        });

        deleteProjectButton.addClickListener(e -> {
            Projects selectedProject = (Projects) stripedGridUsers.asSingleSelect().getValue();
            if (stripedGridUsers.getSelectedItems().isEmpty()) {
                Notification.show("No project selected", 3000, Notification.Position.MIDDLE);
            } else {

                sampleProjectService.deleteProject(selectedProject.getId());

                setGridProjectData(stripedGridUsers);
            }
        });

        updateProjectButton.addClickListener(e -> {
            Projects selectedProject = (Projects) stripedGridUsers.asSingleSelect().getValue();
            if (selectedProject != null) {
                Projects projectToUpdate = sampleProjectService.findById(selectedProject.getId());
                if (projectToUpdate != null) {
                    projectToUpdate.setTitle(titleTextField.getValue());
                    projectToUpdate.setDescription(descriptionTextField.getValue());
                    sampleProjectService.update(projectToUpdate);
                    setGridProjectData(stripedGridUsers);
                    stripedGridUsers.getDataProvider().refreshItem(projectToUpdate);
                    Notification notification = new Notification("Project " + projectToUpdate.getTitle() + " updated", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.open();
                } else {
                    Notification.show("Project not found", 3000, Notification.Position.MIDDLE);
                }
            }

        });


        stripedGridUsers.asSingleSelect().addValueChangeListener(event -> {
            Projects selectedProject = (Projects) stripedGridUsers.asSingleSelect().getValue();
            if (selectedProject != null) {
                titleTextField.setValue(selectedProject.getId().toString());
                descriptionTextField.setValue(selectedProject.getDescription());

            }
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

    private void setGridProjectData(Grid grid) {
        grid.setItems(query -> sampleProjectService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.setColumns("title", "description");

        grid.getColumnByKey("title")
                .setWidth("25%"); // Set the width of the "title" column to 25%
    }

}
