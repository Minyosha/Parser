package gb.views.projects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
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
    private boolean isTestRunning = false;

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
        getContent().setHeight("100%");
        getContent().getStyle().set("flex-grow", "1");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.getStyle().set("flex-grow", "1");
        setTabSheetSampleData(tabSheet);
        getContent().add(tabSheet);
    }


    private void setTabSheetSampleData(TabSheet tabSheet) {

        Button createProjectButton = new Button("Create project");
        createProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createProjectButton.setWidth("192px");

        Button updateProjectButton = new Button("Update project");
        updateProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateProjectButton.setWidth("192px");

        Button deleteProjectButton = new Button("Delete project");
        deleteProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteProjectButton.setWidth("192px");

        Button openProjectButton = new Button("Open project");
        openProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        openProjectButton.setWidth("192px");

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

        pasteArticlesButton.addClickListener(e -> {
            if (selectedProject != null) {
                String text = pasteArticlesTextField.getValue();
                String[] articles = text.trim().split("\\s+");
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

        Button deleteArticlesButton = new Button("Delete all articles");
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


        Button pasteVariantsButton = new Button("Paste site variants");
        pasteVariantsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        pasteVariantsButton.setEnabled(false);


        pasteVariantsButton.addClickListener(e -> {
            if (selectedProject != null) {
                String text = pasteVariantsTextField.getValue();
                String[] variants = text.trim().split("\\s+");
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


        Button deleteVariantsButton = new Button("Delete all site variants");
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


        VerticalLayout createSelectProjectLayout = new VerticalLayout();
        createSelectProjectLayout.setHeightFull();
        createSelectProjectLayout.setWidthFull();
        createSelectProjectLayout.add(new Text("On this page you can create, delete and select project to modify"));

        HorizontalLayout horizontalLayoutForCreateAndSelect = new HorizontalLayout();
        horizontalLayoutForCreateAndSelect.setWidthFull();
        horizontalLayoutForCreateAndSelect.setHeightFull();
        horizontalLayoutForCreateAndSelect.getStyle().set("flex-grow", "1");

        createSelectProjectLayout.add(horizontalLayoutForCreateAndSelect);

        VerticalLayout verticalLayoutForCreatingProject = new VerticalLayout();
        verticalLayoutForCreatingProject.setWidthFull();
        verticalLayoutForCreatingProject.setHeightFull();
        verticalLayoutForCreatingProject.getStyle().set("flex-grow", "1");

        verticalLayoutForCreatingProject.add(titleTextField, descriptionTextField, createProjectButton, updateProjectButton, openProjectButton, deleteProjectButton);
        verticalLayoutForCreatingProject.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        verticalLayoutForCreatingProject.setMaxWidth("220px");
        horizontalLayoutForCreateAndSelect.add(verticalLayoutForCreatingProject, stripedGridProjects, verticalLayoutForArticles, verticalLayoutForVariants);

        VerticalLayout downloadClientLayout = new VerticalLayout();
        TextField portTextField = new TextField();
        portTextField.setValue(String.valueOf(port));
        portTextField.setLabel("Enter port");
        portTextField.addValueChangeListener(event -> {
            String text = event.getValue();
            if (!text.matches("[0-9]*")) {
                portTextField.setValue(event.getOldValue());
            }
        });
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ZipOutputStream zos = new ZipOutputStream(baos);

            addDetachListener(event -> {
                try {
                    zos.close();
                } catch (IOException e) {
                }
            });

            zos.putNextEntry(new ZipEntry("Parser/"));
            zos.closeEntry();

            InputStream fis = getClass().getClassLoader().getResourceAsStream("Parser.jar");
            if (fis != null) {
                zos.putNextEntry(new ZipEntry("Parser/Parser.jar"));

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                zos.closeEntry();
                fis.close();
            } else {
                System.out.println("Couldn't find file Parser.jar in resources");
            }

            String clientText = "Greetings!";
            zos.putNextEntry(new ZipEntry("Parser/client.txt"));
            zos.write(clientText.getBytes());
            zos.closeEntry();

            zos.close();

            byte[] zipContent = baos.toByteArray();

            StreamResource resource = new StreamResource("Parser.zip", () -> {
                return new ByteArrayInputStream(zipContent);
            });

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
        htmlTextField.setSizeFull();
        htmlTextField.setReadOnly(true);

        TextArea consoleTextField = new TextArea();
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

            Gson gson = new Gson();
            String jsonData = gson.toJson(parameters);

            projectToUpdate.setJsonData(jsonData);
            sampleProjectService.update(projectToUpdate);

        });

        TextField numberOfTestedArticles = new TextField();
        numberOfTestedArticles.setLabel("How many articles to test?");
        numberOfTestedArticles.setWidth("192px");
        numberOfTestedArticles.setValue("1");
        numberOfTestedArticles.setPattern("[1-9]|10");
        numberOfTestedArticles.setErrorMessage("Please enter a number from 1 to 10");
        numberOfTestedArticles.addValueChangeListener(event -> {
            String text = event.getValue();
            if (!text.matches("[1-9]|10")) {
                numberOfTestedArticles.setValue(event.getOldValue());
            }
        });

        Button testOperationsButton = new Button("Test operations");
        testOperationsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        testOperationsButton.setWidth("192px");

        testOperationsButton.addClickListener(e -> {
            updateOperationsButton.click();
            try {
                selectedProject = sampleProjectService.findById(selectedProject.getId());
                int howManyTestedArticles = Integer.parseInt(numberOfTestedArticles.getValue());
                Operations.runTest(selectedProject, consoleTextField, howManyTestedArticles);
                isTestRunning = true;
            } catch (HttpStatusException ex) {
                throw new RuntimeException(ex);
            } finally {
                isTestRunning = false;
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
                                .userAgent(userAgent)
                                .referrer("http://www.google.com")
                                .timeout(5000)
                                .followRedirects(true)
                                .execute();

                        if (response.statusCode() == 200) {
                            Document document = Jsoup.parse(response.body());
                            String formattedHtml = document.html();
                            htmlTextField.setValue(formattedHtml);
                        } else {
                            System.out.println("Failed to fetch URL. Response code: " + response.statusCode());
                        }
                    } catch (SocketTimeoutException ste) {
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

        VerticalLayout runProjectLayout = new VerticalLayout();
        runProjectLayout.setWidthFull();
        runProjectLayout.setHeightFull();
        runProjectLayout.getStyle().set("flex-grow", "1");

        HorizontalLayout horizontalLayoutForRunProject = new HorizontalLayout();
        horizontalLayoutForRunProject.setWidthFull();
        horizontalLayoutForRunProject.setHeightFull();
        horizontalLayoutForRunProject.getStyle().set("flex-grow", "1");

        VerticalLayout leftRunVerticalLayout = new VerticalLayout();
        leftRunVerticalLayout.setWidthFull();
        leftRunVerticalLayout.setHeightFull();
        leftRunVerticalLayout.getStyle().set("flex-grow", "1");

        VerticalLayout middleRunVerticalLayout = new VerticalLayout();
        middleRunVerticalLayout.setWidthFull();
        middleRunVerticalLayout.setHeightFull();
        middleRunVerticalLayout.getStyle().set("flex-grow", "1");

        VerticalLayout rightRunVerticalLayout = new VerticalLayout();
        rightRunVerticalLayout.setWidthFull();
        rightRunVerticalLayout.setHeightFull();
        rightRunVerticalLayout.getStyle().set("flex-grow", "1");

        TextField fileExtension = new TextField();
        fileExtension.setLabel("Enter file extension");
        fileExtension.setWidth("192px");

        Button runProjectButton = new Button("Update");
        runProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        runProjectButton.setWidth("192px");
        runProjectButton.addClickListener(e -> {
            sendUpdatePost(ipForREST, selectedProject, fileExtension.getValue());
        });

        Button pauseProjectButton = new Button("Pause");
        pauseProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        pauseProjectButton.setWidth("192px");
        pauseProjectButton.addClickListener(e -> {
            sendPausePost(ipForREST, selectedProject);
        });

        Button runResumeProjectButton = new Button("Run / Resume");
        runResumeProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        runResumeProjectButton.setWidth("192px");
        runResumeProjectButton.addClickListener(e -> {
            sendRunPost(ipForREST, selectedProject);
        });

        Button createReportButton = new Button("Open report");
        createReportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createReportButton.setWidth("192px");
        createReportButton.addClickListener(e -> {
            sendReportPost(ipForREST, selectedProject);
        });

        tabSheet.addSelectedChangeListener(event -> {
            modifyProjectLayout.removeAll();
            runProjectLayout.removeAll();
            String selectProjectText = new String("Select a project to modify from the \"Create and select project\" tab");
            modifyProjectLayout.add(selectProjectText);
            runProjectLayout.add(selectProjectText);
            if (selectedProject.getTitle() != null) {
                selectProjectText = ("On this page you can modify project " + selectedProject.getTitle() + ": " + selectedProject.getDescription());
                modifyProjectLayout.removeAll();
                modifyProjectLayout.add(selectProjectText);
                modifyProjectLayout.setPadding(false);
                modifyProjectLayout.add(horizontalLayoutForModifyProject);
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
                leftModVerticalLayout.removeAll();
                leftModVerticalLayout.add(getHtmlStartSearch, getHtmlStartSearchOffset, getHtmlEndSearch, getHtmlEndSearchOffset,
                        calculateOffsetButton, updateOperationsButton, numberOfTestedArticles, testOperationsButton);
                getHtmlStartSearch.setValue(setParamValue("getHtmlStartSearch"));
                getHtmlStartSearchOffset.setValue(setParamValue("getHtmlStartSearchOffset"));
                getHtmlEndSearch.setValue(setParamValue("getHtmlEndSearch"));
                getHtmlEndSearchOffset.setValue(setParamValue("getHtmlEndSearchOffset"));

                runProjectLayout.removeAll();
                String runProjectText = new String("On this page you can run project " +
                        selectedProject.getTitle() + ": " + selectedProject.getDescription());
                runProjectLayout.add(runProjectText);
                String localIp = null;
                try {
                    localIp = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                String publicIp = getPublicIp();
                RadioButtonGroup chooseIP = new RadioButtonGroup();
                chooseIP.setItems("Local IP: " + localIp, "Public IP: " + publicIp);
                chooseIP.setValue("Local IP: " + localIp);

                runProjectLayout.add(new H5("Here you can select IP and port to use:"));
                runProjectLayout.add(chooseIP);

                Button sendTestPostButton = new Button("Send test POST");
                sendTestPostButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                sendTestPostButton.setWidth("192px");

                sendTestPostButton.addClickListener(e -> {
                    sendTestPost(ipForREST);
                });
                runProjectLayout.add(portTextField);
                runProjectLayout.add(sendTestPostButton);
                runProjectLayout.add(new H5("Here you can run you project and get report:"));

                runProjectLayout.add(fileExtension, runProjectButton, runResumeProjectButton, pauseProjectButton, createReportButton);
            } else if (selectedProject == null) {
                modifyProjectLayout.add(selectProjectText);
                return;
            }
        });


        tabSheet.add("Download desktop client", downloadClientLayout);
        tabSheet.add("Create and select project", createSelectProjectLayout);
        tabSheet.add("Modify project", modifyProjectLayout);
        tabSheet.add("Run project", runProjectLayout);

        setGridProjectData(stripedGridProjects);

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


        openProjectButton.addClickListener(e -> {
            if (selectedProject == null) {
                Notification.show("No project selected", 3000, Notification.Position.MIDDLE);
            } else {
                tabSheet.setSelectedIndex(2);
            }
        });


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
        try {
            URL url = new URL(ipForREST + "/hello");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Notification.show("Request sent successfully to: " + ipForREST, 5000, Notification.Position.MIDDLE);
            } else {
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
                .setWidth("35%");
    }


    public void setSelectedProject(Project project) {
        if (project == null) {
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
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendUpdatePost(String ipForREST, Project selectedProject, String fileExtension) {
        try {
            PostData postData = new PostData(selectedProject, fileExtension);

            URL url = new URL(ipForREST + "/project?update");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            Gson gson = new Gson();
            String json = gson.toJson(postData);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(json.getBytes("UTF-8"));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Notification.show("Project updated successfully", 5000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Failed to send request to: " + ipForREST, 5000, Notification.Position.MIDDLE);
            }
        } catch (ConnectException e) {
            System.out.println("Connection refused: " + e.getMessage());
            Notification.show("Connection refused: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRunPost(String ipForREST, Project selectedProject) {
        try {

            URL url = new URL(ipForREST + "/project?run");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            HashMap<String, String> data = new HashMap<>();
            data.put("title", selectedProject.getTitle());
            data.put("userAgent", Operations.userAgent);

            Gson gson = new Gson();
            String json = gson.toJson(data);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(json.getBytes("UTF-8"));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseString = response.toString();
                Notification.show(responseString, 5000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Failed to send request to: " + ipForREST, 5000, Notification.Position.MIDDLE);
            }
        } catch (ConnectException e) {
            System.out.println("Connection refused: " + e.getMessage());
            Notification.show("Connection refused: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendPausePost(String ipForREST, Project selectedProject) {
        try {
            URL url = new URL(ipForREST + "/project?pause");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            String sendedData = selectedProject.getTitle().toString();
            Gson gson = new Gson();
            String json = gson.toJson(sendedData);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(json.getBytes("UTF-8"));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseString = response.toString();
                Notification.show(responseString, 5000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Failed to send request to: " + ipForREST, 5000, Notification.Position.MIDDLE);
            }
        } catch (ConnectException e) {
            System.out.println("Connection refused: " + e.getMessage());
            Notification.show("Connection refused: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendReportPost(String ipForREST, Project selectedProject) {
        try {

            URL url = new URL(ipForREST + "/project?report");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            String sendedData = selectedProject.getTitle().toString();
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("title", sendedData);

            Gson gson = new Gson();
            String json = gson.toJson(dataMap);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(json.getBytes("UTF-8"));

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String responseString = response.toString();
                Notification.show(responseString, 5000, Notification.Position.MIDDLE);
            } else {
                Notification.show("Failed to send request to: " + ipForREST, 5000, Notification.Position.MIDDLE);
            }
        } catch (ConnectException e) {
            System.out.println("Connection refused: " + e.getMessage());
            Notification.show("Connection refused: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
