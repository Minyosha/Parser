package gb.views.projects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.shaded.gson.Gson;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.model.Label;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import gb.data.*;
import gb.security.AuthenticatedUser;
import gb.services.ArticleService;
import gb.services.ProjectsService;
import gb.services.VariantsService;
import gb.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.grid.Grid;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.tabs.TabSheet;
import org.springframework.data.domain.PageRequest;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@PageTitle("Projects")
@Route(value = "projects-view", layout = MainLayout.class)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ProjectsView extends Composite<VerticalLayout> implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;
    private TextArea titleTextField = new TextArea();
    private TextArea descriptionTextField = new TextArea();
    private Anchor downloadButton;
    Project selectedProject = (Project) new Project();
    private int port = 8082;
    private String ipForREST;

    @Autowired
    private ProjectsService sampleProjectService;
    private ArticleService articleService;
    private VariantsService variantsService;

    @Autowired
    private void setArticleService(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Autowired
    private void setVariantsService(VariantsService variantsService) {
        this.variantsService = variantsService;
    }

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
//        tabSheet.addSelectedChangeListener(event -> {
//            System.out.println(selectedProject.getTitle());
//        });
    }


    private void setTabSheetSampleData(TabSheet tabSheet) {
        titleTextField.setLabel("Title");
        titleTextField.setMaxHeight("200px");
        descriptionTextField.setLabel("Description");
        descriptionTextField.setMaxHeight("300px");

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
        Grid stripedGridProjects = new Grid(Project.class);
        stripedGridProjects.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        stripedGridProjects.setHeightFull();
        stripedGridProjects.setWidthFull();
        stripedGridProjects.getStyle().set("flex-grow", "1");





        Grid<String> strippedGridArticles = new Grid<>(String.class);
        strippedGridArticles.removeAllColumns();
        strippedGridArticles.addColumn(str -> str).setHeader("Articles");
        strippedGridArticles.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        strippedGridArticles.setHeightFull();
        strippedGridArticles.setWidthFull();
        strippedGridArticles.getStyle().set("flex-grow", "1");

        TextField pasteArticlesTextField = new TextField();
        pasteArticlesTextField.setWidth("300px");
        pasteArticlesTextField.getStyle().set("flex-grow", "1");
        pasteArticlesTextField.setLabel("Type/Paste articles separated by spaces");

        Button pasteArticlesButton = new Button("Paste articles");
        pasteArticlesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        pasteArticlesButton.setEnabled(false);
//        pasteArticlesButton.setWidth("192px");

        pasteArticlesButton.addClickListener(e -> {
            if (selectedProject != null) {
                String text = pasteArticlesTextField.getValue();
                String[] articles = text.trim().split("\\s+"); // добавили .trim()
                for (String article : articles) {
                    if (!isDuplicateArticle(article)) {
                        articleService.save(new Article(selectedProject, article));
                    }
                }
                selectedProject.getArticles().clear();
                selectedProject.getArticles().addAll(articleService.findByProject(selectedProject));
                strippedGridArticles.setItems(selectedProject.getArticles().stream()
                        .map(Article::getArticle_content)
                        .collect(Collectors.toList()));
            }
            pasteArticlesTextField.clear();
        });

        Button deleteArticlesButton = new Button("Delete all articles"); // Add a button to delete an article
        deleteArticlesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteArticlesButton.setWidth("192px");
        deleteArticlesButton.setEnabled(false);

        deleteArticlesButton.addClickListener(e -> {
            if (selectedProject != null) {
                articleService.deleteByProjectId(selectedProject.getId());
                selectedProject.getArticles().clear();
                strippedGridArticles.setItems(selectedProject.getArticles().stream()
                        .map(Article::getArticle_content)
                        .collect(Collectors.toList()));
            }

        });

        VerticalLayout verticalLayoutForArticles = new VerticalLayout();
        verticalLayoutForArticles.setWidthFull();
        verticalLayoutForArticles.setHeightFull();
        verticalLayoutForArticles.setPadding(false);
        verticalLayoutForArticles.getStyle().set("flex-grow", "1");
        verticalLayoutForArticles.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        verticalLayoutForArticles.add(strippedGridArticles, pasteArticlesTextField, pasteArticlesButton, deleteArticlesButton);




        Grid<String> strippedGridVariants = new Grid<>(String.class);
        strippedGridVariants.removeAllColumns();
        strippedGridVariants.addColumn(str -> str).setHeader("Site Variants");
        strippedGridVariants.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        strippedGridVariants.setHeightFull();
        strippedGridVariants.setWidthFull();
        strippedGridVariants.getStyle().set("flex-grow", "1");

        TextField pasteVariantsTextField = new TextField();
        pasteVariantsTextField.setWidth("300px");
        pasteVariantsTextField.getStyle().set("flex-grow", "1");
        pasteVariantsTextField.setLabel("Type/Paste variants separated by spaces");


        Button pasteVariantsButton = new Button("Paste site variants"); // Add a button to delete an article
        pasteVariantsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        pasteVariantsButton.setEnabled(false);
//        pasteVariantsButton.setWidth("192px");




        pasteVariantsButton.addClickListener(e -> {
            if (selectedProject != null) {
                String text = pasteVariantsTextField.getValue();
                String[] variants = text.trim().split("\\s+"); // добавили .trim()
                for (String variant : variants) {
                    if (!isDuplicateVariant(variant)) {
                        variantsService.save(new Variants(selectedProject, variant));
                    }
                }
                selectedProject.getVariants().clear();
                selectedProject.getVariants().addAll(variantsService.findByProject(selectedProject));
                strippedGridVariants.setItems(selectedProject.getVariants().stream()
                        .map(Variants::getVariant_content)
                        .collect(Collectors.toList()));
            }
            pasteVariantsTextField.clear();
        });


        Button deleteVariantsButton = new Button("Delete all site variants"); // Add a button to delete an article
        deleteVariantsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteVariantsButton.setEnabled(false);
        deleteVariantsButton.setWidth("192px");

        deleteVariantsButton.addClickListener(e -> {
            if (selectedProject != null) {
                variantsService.deleteByProjectId(selectedProject.getId());
                selectedProject.getVariants().clear();
                strippedGridVariants.setItems(selectedProject.getVariants().stream()
                        .map(Variants::getVariant_content)
                        .collect(Collectors.toList()));
            }

        });



        VerticalLayout verticalLayoutForVariants = new VerticalLayout();
        verticalLayoutForVariants.setWidthFull();
        verticalLayoutForVariants.setHeightFull();
        verticalLayoutForVariants.setPadding(false);
        verticalLayoutForVariants.getStyle().set("flex-grow", "1");
        verticalLayoutForVariants.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        verticalLayoutForVariants.add(strippedGridVariants, pasteVariantsTextField, pasteVariantsButton, deleteVariantsButton);


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
        horizontalLayoutForCreateAndSelect.add(verticalLayoutForCreatingProject, stripedGridProjects, verticalLayoutForArticles, verticalLayoutForVariants);


        // Create a download client layout
        VerticalLayout downloadClientLayout = new VerticalLayout();
        TextField portTextField = new TextField();
        portTextField.setValue(String.valueOf(port));
        portTextField.setLabel("Enter port");
        portTextField.addValueChangeListener(event -> {
            String text = event.getValue();
            if (!text.matches("[0-9]*")) {
                // Если введено не число, очищаем поле или возвращаем предыдущее допустимое значение
                portTextField.setValue(event.getOldValue());
            }
        });
        // Download client button
        try {
            // Create a ByteArrayOutputStream to store the file content in memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Create a ZipOutputStream to create the zip file
            ZipOutputStream zos = new ZipOutputStream(baos);

            // Add a cleanup mechanism to close the ZipOutputStream when the user navigates away from the page
            addDetachListener(event -> {
                try {
                    zos.close();
                } catch (IOException e) {
                    // Handle the exception
                }
            });

            // Create the "Parser" directory in memory
            zos.putNextEntry(new ZipEntry("Parser/"));
            zos.closeEntry();

            // Create the "data.sql" file in memory
            String sqlContent = "Your SQL content here";
            zos.putNextEntry(new ZipEntry("Parser/data.sql"));
            zos.write(sqlContent.getBytes());
            zos.closeEntry();

            // Create the "client.txt" file in memory
            String clientText = this.authenticatedUser.get().get().getUsername() + "\n" + this.authenticatedUser.get().get().getHashedPassword() + "\n" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "\n" + getPublicIp() + ":" + port;
            zos.putNextEntry(new ZipEntry("Parser/client.txt"));
            zos.write(clientText.getBytes());
            zos.closeEntry();

            // Close the ZipOutputStream
            zos.close();

            // Get the byte array containing the zip file content
            byte[] zipContent = baos.toByteArray();

            // Prepare the StreamResource for download
            StreamResource resource = new StreamResource("Parser.zip", () -> {
                return new ByteArrayInputStream(zipContent);
            });

            // Initialize the download button
            downloadButton = new Anchor(resource, "Download Parser.zip");
            downloadButton.getElement().setAttribute("download", true);
            downloadButton.add(new Button(new Icon(VaadinIcon.DOWNLOAD)));

            downloadClientLayout.add(new H5("Here you can download desktop client"));
            downloadClientLayout.add(downloadButton);

            String localIp = InetAddress.getLocalHost().getHostAddress();
            String publicIp = getPublicIp();

            Anchor localIpLink = new Anchor("http://" + localIp, "Local IP: " + localIp);
            localIpLink.setTarget("_blank"); // Открытие ссылки в новом окне

            Anchor publicIpLink = new Anchor("http://" + publicIp, "Public IP: " + publicIp);
            publicIpLink.setTarget("_blank"); // Открытие ссылки в новом окне

            downloadClientLayout.add(new H5("Here is you local and public IP's:"));

            downloadClientLayout.add(localIpLink);
            downloadClientLayout.add(publicIpLink);


            RadioButtonGroup chooseIP = new RadioButtonGroup();
            chooseIP.setItems("Local IP: " + localIp, "Public IP: " + publicIp);
            chooseIP.setValue("Local IP: " + localIp);

            ipForREST = "http://" + localIp + ":" + portTextField.getValue();

            chooseIP.addValueChangeListener(event -> {
                String selectedValue = event.getValue().toString();
                if (selectedValue.equals("Local IP: " + localIp)) {
                    ipForREST = "http://" + localIp + ":" + portTextField.getValue();
                    System.out.println(ipForREST);
                } else if (selectedValue.equals("Public IP: " + publicIp)) {
                    ipForREST = "http://" + publicIp + ":" + portTextField.getValue();
                    System.out.println(ipForREST);
                }
            });

            downloadClientLayout.add(new H5("Here you can choose which IP add port you want to use to send REST requests:"));
            downloadClientLayout.add(chooseIP);

            Button sendTestPostButton = new Button("Send test POST");
            sendTestPostButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            sendTestPostButton.setWidth("192px");

            sendTestPostButton.addClickListener(e -> {
                sendTestPost(ipForREST);
            });
            downloadClientLayout.add(portTextField);
            downloadClientLayout.add(sendTestPostButton);

        } catch (IOException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Unable to create the archive");
            getContent().add(String.valueOf(errorLabel));
        }


        // Create a modify project layout
        VerticalLayout modifyProjectLayout = new VerticalLayout();
        modifyProjectLayout.setWidthFull(); // Set width to full
        modifyProjectLayout.setHeightFull();
        modifyProjectLayout.getStyle().set("flex-grow", "1");

        HorizontalLayout horizontalLayoutForModifyProject = new HorizontalLayout();
        VerticalLayout leftModVerticalLayout = new VerticalLayout();
        VerticalLayout rightModVerticalLayout = new VerticalLayout();





        TextField urlField = new TextField();
        urlField.setPlaceholder("Enter URL to view HTML code");
        urlField.setWidthFull();

        TextArea htmlTextField = new TextArea();
//        htmlTextField.setMaxHeight("490px");
        htmlTextField.setSizeFull();
        htmlTextField.setReadOnly(true);


        TextArea consoleTextField = new TextArea();
//        ConsoleTextField.setMaxHeight("490px");
        consoleTextField.setSizeFull();


        TextField getHtmlStartSearch = new TextField();
        getHtmlStartSearch.setLabel("Start search with this:");


        TextField getHtmlStartSearchOffset = new TextField();
        getHtmlStartSearchOffset.setLabel("Enter offset");
        getHtmlStartSearchOffset.setPattern("[0-9]*");
        getHtmlStartSearchOffset.setErrorMessage("Only numbers are allowed");
        getHtmlStartSearchOffset.addValueChangeListener(event -> {
            String text = event.getValue();
            if (!text.matches("[0-9]*")) {
                // Если введено не число, очищаем поле или возвращаем предыдущее допустимое значение
                getHtmlStartSearchOffset.setValue(event.getOldValue());
            }
        });

        TextField getHtmlEndSearch = new TextField();
        getHtmlEndSearch.setLabel("End search with this:");


        TextField getHtmlEndSearchOffset = new TextField();
        getHtmlEndSearchOffset.setLabel("Enter offset");
        getHtmlEndSearchOffset.setPattern("[0-9]*");
        getHtmlEndSearchOffset.setErrorMessage("Only numbers are allowed");
        getHtmlEndSearchOffset.addValueChangeListener(event -> {
            String text = event.getValue();
            if (!text.matches("[0-9]*")) {
                // Если введено не число, очищаем поле или возвращаем предыдущее допустимое значение
                getHtmlEndSearchOffset.setValue(event.getOldValue());
            }
        });

        Button calculateOffsetButton = new Button("Calculate offset");
        calculateOffsetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        calculateOffsetButton.setWidth("192px");
        calculateOffsetButton.addClickListener(event -> {
            getHtmlStartSearchOffset.setValue(String.valueOf(getHtmlStartSearch.getValue().length()));
            getHtmlEndSearchOffset.setValue(String.valueOf(getHtmlEndSearch.getValue().length()));
        });






        Button updateOperationsButton = new Button("Update operations");
        updateOperationsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateOperationsButton.setWidth("192px");

        updateOperationsButton.addClickListener(e -> {
            Project projectToUpdate = sampleProjectService.findById(selectedProject.getId());
            String getHtmlStartSearchValue = getHtmlStartSearch.getValue();
            String getHtmlStartSearchOffsetValue = getHtmlStartSearchOffset.getValue();
            String getHtmlEndSearchValue = getHtmlEndSearch.getValue();
            String getHtmlEndSearchOffsetValue = getHtmlEndSearchOffset.getValue();

            Map<String, String> parameters = new HashMap<>();
            parameters.put("getHtmlStartSearch", getHtmlStartSearchValue);
            parameters.put("getHtmlStartSearchOffset", getHtmlStartSearchOffsetValue);
            parameters.put("getHtmlEndSearch", getHtmlEndSearchValue);
            parameters.put("getHtmlEndSearchOffset", getHtmlEndSearchOffsetValue);

            // Convert the map to JSON using Gson
            Gson gson = new Gson();
            String jsonData = gson.toJson(parameters);

            projectToUpdate.setJsonData(jsonData);
            sampleProjectService.update(projectToUpdate);

        });

        Button testOperationsButton = new Button("Test operations");
        testOperationsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        testOperationsButton.setWidth("192px");

        testOperationsButton.addClickListener(e -> {
            updateOperationsButton.click();
            try {
                selectedProject = sampleProjectService.findById(selectedProject.getId());
                Operations.runTest(selectedProject, consoleTextField);
            } catch (HttpStatusException ex) {
                throw new RuntimeException(ex);
            }
        });



        Button viewHtmlButton = new Button("View HTML code");
        viewHtmlButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewHtmlButton.setWidth("192px");
        viewHtmlButton.setMaxHeight("36px");
        viewHtmlButton.addClickListener(e -> {
            VaadinRequest currentRequest = VaadinService.getCurrentRequest();
            String userAgent = currentRequest.getHeader("User-Agent");

            String url = urlField.getValue();
            if (isValidUrl(url)) {
                if (isUrlValid(url)) {
                    try {
                        Connection.Response response = Jsoup.connect(url)
                                .userAgent(userAgent) // use the user agent from the current request
                                .referrer("http://www.google.com")
                                .timeout(5000)
                                .followRedirects(true)
                                .execute();

                        if (response.statusCode() == 200) {
                            Document document = Jsoup.parse(response.body());
                            String formattedHtml = document.html();
                            htmlTextField.setValue(formattedHtml);
                        } else {
                            // Handle the case when the response code is not 200
                            System.out.println("Failed to fetch URL. Response code: " + response.statusCode());
                        }
                    } catch (SocketTimeoutException ste) {
                        // Логирование исключения или уведомление пользователя
                        System.out.println("Connection timed out. Please try again later.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Notification notification = new Notification("URL does not exist or is unreachable", 3000);
                    notification.open();
                    notification.setPosition(Notification.Position.MIDDLE);
                }
            } else {
                Notification notification = new Notification("Please enter a valid URL starting with 'http://' or 'https://'", 3000);
                notification.open();
                notification.setPosition(Notification.Position.MIDDLE);
            }
        });

        // Create a run project layout
        VerticalLayout runProjectLayout = new VerticalLayout();
        runProjectLayout.setWidthFull(); // Set width to full
        runProjectLayout.setHeightFull();
        runProjectLayout.getStyle().set("flex-grow", "1");




        tabSheet.addSelectedChangeListener(event -> {
            modifyProjectLayout.removeAll(); // Очистить содержимое layout
            runProjectLayout.removeAll();
            String selectProjectText = new String("Select a project to modify from the \"Create and select project\" tab");
            modifyProjectLayout.add(selectProjectText);
            runProjectLayout.add(selectProjectText);
            if (selectedProject.getTitle() != null) {
                selectProjectText = ("On this page you can modify project " + selectedProject.getTitle() + ": " + selectedProject.getDescription());
                modifyProjectLayout.removeAll();
                modifyProjectLayout.add(selectProjectText);
                modifyProjectLayout.setPadding(false);
                rightModVerticalLayout.removeAll();
                H5 consoleH5 = new H5("Console with test result and logs:");
                rightModVerticalLayout.add(consoleH5, consoleTextField, urlField, viewHtmlButton, htmlTextField);
                rightModVerticalLayout.setPadding(false);
                rightModVerticalLayout.getStyle().set("flex-grow", "1");
                horizontalLayoutForModifyProject.add(leftModVerticalLayout, rightModVerticalLayout);
                horizontalLayoutForModifyProject.setWidthFull();
                horizontalLayoutForModifyProject.setHeightFull();
                horizontalLayoutForModifyProject.getStyle().set("flex-grow", "1");
                horizontalLayoutForModifyProject.setPadding(false);
                rightModVerticalLayout.setWidthFull();
                rightModVerticalLayout.setHeightFull();
                leftModVerticalLayout.setMaxWidth("300px");
                leftModVerticalLayout.setHeightFull();
                leftModVerticalLayout.getStyle().set("flex-grow", "1");
                leftModVerticalLayout.setPadding(false);
                leftModVerticalLayout.add(getHtmlStartSearch, getHtmlStartSearchOffset, getHtmlEndSearch, getHtmlEndSearchOffset);
                leftModVerticalLayout.add(calculateOffsetButton, updateOperationsButton, testOperationsButton);
                modifyProjectLayout.add(horizontalLayoutForModifyProject);

                getHtmlStartSearch.setValue(setParamValue("getHtmlStartSearch"));
                getHtmlStartSearchOffset.setValue(setParamValue("getHtmlStartSearchOffset"));
                getHtmlEndSearch.setValue(setParamValue("getHtmlEndSearch"));
                getHtmlEndSearchOffset.setValue(setParamValue("getHtmlEndSearchOffset"));
            } else if (selectedProject == null) {
                modifyProjectLayout.add(selectProjectText);
                return;
            }

        });






        // Now add the VerticalLayout to the "Create and select project" tab.
        tabSheet.add("Download desktop client", downloadClientLayout);
        tabSheet.add("Create and select project", createSelectProjectLayout);
        tabSheet.add("Modify project", modifyProjectLayout);
        tabSheet.add("Run project", runProjectLayout);

        setGridProjectData(stripedGridProjects);

        // Add an event listener to the "Create project" button.
        createProjectButton.addClickListener(e -> {
            if (titleTextField.isEmpty()) {
                Notification.show("Title cannot be empty", 3000, Notification.Position.MIDDLE);
            } else {
                Project project = new Project();
                project.setTitle(titleTextField.getValue());
                project.setDescription(descriptionTextField.getValue());
                sampleProjectService.createProject(project);
                setGridProjectData(stripedGridProjects);
                selectedProject = project;

                deleteVariantsButton.setEnabled(true);
                deleteArticlesButton.setEnabled(true);
                pasteVariantsButton.setEnabled(true);
                pasteArticlesButton.setEnabled(true);

            }
        });

        // Add an event listener to the "Open project" button.
        openProjectButton.addClickListener(e -> {
            if (selectedProject == null) {
                Notification.show("No project selected", 3000, Notification.Position.MIDDLE);
            } else {
                tabSheet.setSelectedIndex(2); // Index of the third tab (0-based)
            }

        });

        // Add an event listener to the "Delete project" button.
        deleteProjectButton.addClickListener(e -> {
            selectedProject = (Project) stripedGridProjects.asSingleSelect().getValue();
            if (stripedGridProjects.getSelectedItems().isEmpty()) {
                Notification.show("No project selected", 3000, Notification.Position.MIDDLE);
            } else {

                sampleProjectService.deleteProject(selectedProject.getId());
                descriptionTextField.clear();
                titleTextField.clear();
                selectedProject = null;
                modifyProjectLayout.removeAll();
                modifyProjectLayout.add(new Text("Select a project to modify from the \"Create and select project\" tab"));
                setGridProjectData(stripedGridProjects);
                strippedGridArticles.setItems(Collections.emptyList());
                strippedGridVariants.setItems(Collections.emptyList());
                deleteVariantsButton.setEnabled(false);
                deleteArticlesButton.setEnabled(false);
                pasteVariantsButton.setEnabled(false);
                pasteArticlesButton.setEnabled(false);
            }
        });

        updateProjectButton.addClickListener(e -> {
            selectedProject = (Project) stripedGridProjects.asSingleSelect().getValue();
            if (selectedProject != null) {
                Project projectToUpdate = sampleProjectService.findById(selectedProject.getId());
                if (projectToUpdate != null) {
                    projectToUpdate.setTitle(titleTextField.getValue());
                    projectToUpdate.setDescription(descriptionTextField.getValue());
                    sampleProjectService.update(projectToUpdate);
                    setGridProjectData(stripedGridProjects);

                    strippedGridArticles.setItems(selectedProject.getArticles().stream()
                            .map(Article::getArticle_content)
                            .collect(Collectors.toList()));
                    strippedGridVariants.setItems(selectedProject.getVariants().stream()
                            .map(Variants::getVariant_content)
                            .collect(Collectors.toList()));
                    stripedGridProjects.getDataProvider().refreshItem(projectToUpdate);
                    // Выбираем обновленный проект
                    stripedGridProjects.asSingleSelect().setValue(projectToUpdate);
                    Notification notification = new Notification("Project " + projectToUpdate.getTitle() + " updated", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.open();
                } else {
                    Notification.show("Project not found", 3000, Notification.Position.MIDDLE);
                }
            }

        });


        stripedGridProjects.asSingleSelect().addValueChangeListener(event -> {
            selectedProject = (Project) stripedGridProjects.asSingleSelect().getValue();
            if (selectedProject != null) {
                titleTextField.setValue(selectedProject.getTitle().toString());
                descriptionTextField.setValue(selectedProject.getDescription());

                strippedGridArticles.setItems(selectedProject.getArticles().stream()
                        .map(Article::getArticle_content)
                        .collect(Collectors.toList()));
                strippedGridVariants.setItems(selectedProject.getVariants().stream()
                        .map(Variants::getVariant_content)
                        .collect(Collectors.toList()));

                deleteVariantsButton.setEnabled(true);
                deleteArticlesButton.setEnabled(true);
                pasteVariantsButton.setEnabled(true);
                pasteArticlesButton.setEnabled(true);
            }
        });


    }


    private String setParamValue(String param) {
        if (selectedProject != null) {
            String jsonData = selectedProject.getJsonData();
            if (jsonData != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objectMapper.readTree(jsonData);
                    String paramValue = jsonNode.get(param).asText();
                    return paramValue;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
        System.out.println("2");
        return "";
    }


    private void sendTestPost(String ipForREST) {
//        System.out.println(ipForREST);
        try {
            URL url = new URL(ipForREST + "/hello");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
//                System.out.println("Request sent successfully to: " + ipForREST);
                Notification.show("Request sent successfully to: " + ipForREST, 5000, Notification.Position.MIDDLE);
            } else {
//                System.out.println("Failed to send request to: " + ipForREST);
                Notification.show("Failed to send request to: " + ipForREST, 5000, Notification.Position.MIDDLE);
            }
        } catch (ConnectException e) {
            System.out.println("Connection refused: " + e.getMessage());
            Notification.show("Connection refused: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            User user = authenticatedUser.get().get();
            if (user.isBanned()) {
                event.forwardTo("banned-view");
            }
        }
    }

    public String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String ip = in.readLine();
            in.close();
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";
    }

    private void setGridProjectData(Grid grid) {
        grid.setItems(query -> sampleProjectService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.setColumns("title", "description");

        grid.getColumnByKey("description")
                .setWidth("35%"); // Set the width of the "title" column to 25%
    }


    public void setSelectedProject(Project project) {
        if (project == null) {
//            throw new IllegalArgumentException("Cannot set selectedProject to null");
            System.out.println("Cannot set selectedProject to null");
        }
        this.selectedProject = project;
    }

    private boolean isDuplicateArticle(String articleContent) {
        if (selectedProject != null) {
            for (Article article : selectedProject.getArticles()) {
                if (article.getArticle_content().equals(articleContent)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDuplicateVariant(String article) {
        if (selectedProject != null) {
            for (Variants variant : selectedProject.getVariants()) {
                if (variant.getVariant_content().equals(article)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    public boolean isUrlValid(String url) {
        try {
            URL websiteUrl = new URL(url);
            URLConnection connection = websiteUrl.openConnection();
            connection.setConnectTimeout(3000); // Set a timeout of 3 seconds
            connection.setReadTimeout(3000); // Set a timeout of 3 seconds
            connection.connect();
            return true;
        } catch (Exception e) {
            // Handle the exception or log the error
            return false;
        }
    }


}
