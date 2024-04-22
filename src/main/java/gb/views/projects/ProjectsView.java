package gb.views.projects;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.model.Label;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
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
import com.vaadin.flow.component.tabs.TabSheet;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@PageTitle("Projects")
@Route(value = "projects-view", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ProjectsView extends Composite<VerticalLayout> implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;
    private TextField titleTextField = new TextField();
    private TextField descriptionTextField = new TextField();
    private Anchor downloadButton;
    Projects selectedProject = (Projects) new Projects();

    @Autowired
    private ProjectsService sampleProjectService;

    public ProjectsView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        getContent().setWidth("100%");
        getContent().setHeight("100%"); // Установите высоту в 100%
        getContent().getStyle().set("flex-grow", "1");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull(); // Установите размер во весь доступный размер
        tabSheet.getStyle().set("flex-grow", "1");
        setTabSheetSampleData(tabSheet);
        getContent().add(tabSheet);
        tabSheet.addSelectedChangeListener(event -> {
            System.out.println(selectedProject.getTitle());
        });
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
        Button openProjectButton = new Button("Open project");
        openProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        openProjectButton.setWidth("192px");

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


        verticalLayoutForCreatingProject.add(titleTextField, descriptionTextField, createProjectButton, updateProjectButton, openProjectButton, deleteProjectButton);
        verticalLayoutForCreatingProject.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        verticalLayoutForCreatingProject.setMaxWidth("220px");
        horizontalLayoutForCreateAndSelect.add(verticalLayoutForCreatingProject, stripedGridUsers);


        // Create a download client layout
        VerticalLayout downloadClientLayout = new VerticalLayout();
        // Download client button
        try {
            // Создать временную папку
            Path tempDirectory = Files.createTempDirectory("parser");
            Path parserDirectory = tempDirectory.resolve("Parser");
            Files.createDirectories(parserDirectory);

            // Add a cleanup mechanism to delete the temporary directory when the user navigates away from the page
            addDetachListener(event -> {
                try {
                    Files.deleteIfExists(tempDirectory);
                } catch (IOException e) {
                    // Handle the exception
                }
            });

            // Копировать файл data.sql в папку Parser
            Path sqlFile = parserDirectory.resolve("data.sql");
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data.sql");
            Files.copy(inputStream, sqlFile, StandardCopyOption.REPLACE_EXISTING);

            // Создать файл client.txt с текстом и текущим временем в папке Parser
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String clientText = "Hello, world\n" + currentTime;
            Path clientFile = parserDirectory.resolve("client.txt");
            Files.write(clientFile, clientText.getBytes());

            // Заархивировать папку Parser в файл Parser.zip
            Path zipPath = tempDirectory.resolve("Parser.zip");
            try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                // Функция для добавления файла в zip
                java.util.function.BiConsumer<Path, String> addToZip = (filePath, zipEntryName) -> {
                    try (InputStream fis = Files.newInputStream(filePath)) {
                        ZipEntry zipEntry = new ZipEntry(zipEntryName);
                        zs.putNextEntry(zipEntry);
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = fis.read(bytes)) >= 0) {
                            zs.write(bytes, 0, length);
                        }
                        zs.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

                // Добавить файлы в zip
                addToZip.accept(sqlFile, "Parser/data.sql");
                addToZip.accept(clientFile, "Parser/client.txt");
            }

            // Подготовить StreamResource для скачивания
            StreamResource resource = new StreamResource("Parser.zip", () -> {
                try {
                    return Files.newInputStream(zipPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    return new ByteArrayInputStream(new byte[0]);
                }
            });

            // Инициализировать кнопку скачивания
            downloadButton = new Anchor(resource, "Download Parser.zip");
            downloadButton.getElement().setAttribute("download", true);
            downloadButton.add(new Button(new Icon(VaadinIcon.DOWNLOAD)));

            downloadClientLayout.add(new Text("On this page you can download desktop client with configuration data"));
            downloadClientLayout.add(downloadButton);
        } catch (IOException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Unable to create the archive");
            getContent().add(String.valueOf(errorLabel));
        }


        // Create a modify project layout
        VerticalLayout modifyProjectLayout = new VerticalLayout();

        tabSheet.addSelectedChangeListener(event -> {
            System.out.println(selectedProject.getTitle());
            modifyProjectLayout.removeAll(); // Очистить содержимое layout
            String text = new String("Select a project to modify from the \"Create and select project\" tab");
            if (selectedProject == null) {
                modifyProjectLayout.add(text);
                return;
            } else if (selectedProject.getTitle() != null) {
                text = ("On this page you can modify project " + selectedProject.getTitle() + ": " + selectedProject.getDescription());
            }
            modifyProjectLayout.add(text);
        });




        // Now add the VerticalLayout to the "Create and select project" tab.
        tabSheet.add("Download desktop client", downloadClientLayout);
        tabSheet.add("Create and select project", createSelectProjectLayout);
        tabSheet.add("Modify project", modifyProjectLayout);

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

        // Add an event listener to the "Open project" button.
        openProjectButton.addClickListener(e -> {
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
            selectedProject = (Projects) stripedGridUsers.asSingleSelect().getValue();
            if (selectedProject != null) {
                Projects projectToUpdate = sampleProjectService.findById(selectedProject.getId());
                if (projectToUpdate != null) {
                    projectToUpdate.setTitle(titleTextField.getValue());
                    projectToUpdate.setDescription(descriptionTextField.getValue());
                    sampleProjectService.update(projectToUpdate);
                    setGridProjectData(stripedGridUsers);
                    stripedGridUsers.getDataProvider().refreshItem(projectToUpdate);
                    // Выбираем обновленный проект
                    stripedGridUsers.asSingleSelect().setValue(projectToUpdate);
                    Notification notification = new Notification("Project " + projectToUpdate.getTitle() + " updated", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.open();
                } else {
                    Notification.show("Project not found", 3000, Notification.Position.MIDDLE);
                }
            }

        });


        stripedGridUsers.asSingleSelect().addValueChangeListener(event -> {
            selectedProject = (Projects) stripedGridUsers.asSingleSelect().getValue();
            if (selectedProject != null) {
                titleTextField.setValue(selectedProject.getTitle().toString());
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

    public void setSelectedProject(Projects project) {
        if (project == null) {
//            throw new IllegalArgumentException("Cannot set selectedProject to null");
            System.out.println("Cannot set selectedProject to null");
        }
        this.selectedProject = project;
    }

}
