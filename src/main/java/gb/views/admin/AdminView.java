package gb.views.admin;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import gb.data.Role;
import gb.data.SamplePerson;
import gb.data.User;
import gb.data.UserRepository;
import gb.security.AuthenticatedUser;
import gb.services.SamplePersonService;
import gb.services.UserService;
import gb.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;


@PageTitle("Administration")
@Route(value = "admin-view", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class AdminView extends Composite<VerticalLayout> implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;
    private UserRepository userRepository;
    @Autowired()
    private SamplePersonService samplePersonService;
    @Autowired
    private UserService userService;
    private List<String> filterValues = new ArrayList<>();
    public AdminView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser; // Set the authenticated user
        this.userService = userService;

        // Create the text fields
        TextField idTextField = new TextField("id");
        idTextField.setReadOnly(true);
//        idTextField.setValue("id");
        TextField usernameTextField = new TextField("Username");
        TextField nameTextField = new TextField("Name");
        TextField rolesTextField = new TextField("Roles");
        rolesTextField.setReadOnly(true);
        TextField emailTextField = new TextField("E-mail");

        // Create headings
        H5 h5UserDetails = new H5();
        h5UserDetails.setText("User details:");
        H5 h5Users = new H5();
        h5Users.setText("Users:");
        H5 h5UserProjects = new H5();
        h5UserProjects.setText("User projects:");

        // Create the primary button
        Button updateUserButton = new Button("Update user");
        updateUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateUserButton.setWidth("192px");

        // Create the secondary button
        Button deleteUserButton = new Button("Delete user");
        deleteUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        deleteUserButton.setWidth("192px");

        // Create Radio buttons
        RadioButtonGroup bannedRadioGroup = new RadioButtonGroup();
        bannedRadioGroup.setLabel("Banned?");
        bannedRadioGroup.setWidth("100%");
        bannedRadioGroup.setMinWidth("192px");
        bannedRadioGroup.setItems("Yes", "No");
        bannedRadioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        // Create the grid, set it to take full height and full width
        Grid<User> stripedGridUsers = new Grid<>(User.class);
        stripedGridUsers.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        stripedGridUsers.setHeightFull();
        stripedGridUsers.setWidthFull();

        // Create the grid, set it to take full height and full width
        Grid<SamplePerson> stripedGridUserProjects = new Grid<>(SamplePerson.class);
        stripedGridUserProjects.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        stripedGridUserProjects.setHeightFull();
        stripedGridUserProjects.setWidthFull();

        // Create a column layout for the text fields and buttons
        VerticalLayout layoutColumnLeft = new VerticalLayout();
        layoutColumnLeft.setWidth(null); // Width is determined by the widest component
        layoutColumnLeft.add(h5UserDetails, idTextField, bannedRadioGroup, usernameTextField, nameTextField, rolesTextField, emailTextField, updateUserButton, deleteUserButton);
        layoutColumnLeft.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Create a column layout for tables
        VerticalLayout layoutColumnRight = new VerticalLayout();
        layoutColumnRight.setWidth(null); // Width is determined by the widest component
        layoutColumnRight.add(h5Users, stripedGridUsers, h5UserProjects, stripedGridUserProjects);
        layoutColumnRight.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Add the grid to the layout
        stripedGridUsers.asSingleSelect().addValueChangeListener(event -> {
            User selectedUser = event.getValue();
            if (selectedUser != null) {
                idTextField.setValue(selectedUser.getId().toString());
                usernameTextField.setValue(selectedUser.getUsername());
                nameTextField.setValue(selectedUser.getName());
                rolesTextField.setValue(selectedUser.getRoles().toString());
                emailTextField.setValue(selectedUser.getEmail());
                bannedRadioGroup.setValue(selectedUser.isBanned() ? "Yes" : "No");

                // Здесь вы можете добавить код для установки значений в другие текстовые поля
            } else {
                idTextField.clear();
                // Очистите значения других текстовых полей при отмене выбора
            }
        });

        updateUserButton.addClickListener(event -> {
            User selectedUser = stripedGridUsers.asSingleSelect().getValue();
            if (selectedUser != null) {
                // Получение последней версии пользователя из базы данных
                User userToUpdate = userService.findById(selectedUser.getId());
                if (userToUpdate != null) {
                    // Обновление данных пользователя
                    userToUpdate.setUsername(usernameTextField.getValue());
                    userToUpdate.setName(nameTextField.getValue());
//                    userToUpdate.setRoles(rolesTextField.getValue()); // Это может потребовать конвертации
                    userToUpdate.setEmail(emailTextField.getValue());
                    userToUpdate.setBanned("Yes".equals(bannedRadioGroup.getValue()));

                    // Сохранение обновленных данных пользователя
                    userService.update(userToUpdate);

                    // Обновление таблицы
                    stripedGridUsers.getDataProvider().refreshItem(userToUpdate);
                    Notification notification = new Notification("User " + userToUpdate.getUsername() + " updated", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.open();
                } else {
                    // Пользователь не найден, обработка ошибки
                }
            }
        });


        deleteUserButton.addClickListener(event -> {
            User selectedUser = stripedGridUsers.asSingleSelect().getValue();

            // Идентификатор текущего пользователя
            Long currentUserId = getCurrentUserId(); // Замените этот метод на ваш способ получения ID текущего пользователя

            if (selectedUser != null) {
                if (selectedUser.getId().equals(currentUserId)) {
                    // Попытка удаления самого себя, отображаем уведомление
                    Notification notification = new Notification("You cannot delete your own account", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.open();
                } else {
                    // Получение последней версии пользователя из базы данных
                    User userToDelete = userService.findById(selectedUser.getId());
                    if (userToDelete != null) {
                        // Удаление пользователя
                        userService.delete(userToDelete.getId());
                        // Обновление таблицы
                        stripedGridUsers.getDataProvider().refreshAll();
                        Notification notification = new Notification("User " + userToDelete.getUsername() + " deleted", 3000);
                        notification.setPosition(Notification.Position.MIDDLE);
                        notification.open();
                    } else {
                        // Пользователь не найден, обработка ошибки
                    }
                }
            }
        });

        // Create a row layout, add the column layout and the grid to it
        HorizontalLayout layoutRow = new HorizontalLayout();
        layoutRow.setHeightFull();
        layoutRow.setWidthFull();
        layoutRow.add(layoutColumnLeft, layoutColumnRight);
        layoutRow.setFlexGrow(0, layoutColumnLeft); // Column layout does not grow
        layoutRow.setFlexGrow(1, layoutColumnRight); // Grid takes the remaining space

        // Ensure the button column doesn't grow and the grid takes the remaining space
        layoutRow.setFlexGrow(0, layoutColumnLeft);
        layoutRow.setFlexGrow(1, layoutColumnRight);

        // Configure the content layout, add the row layout to it, and set it to take full size
        getContent().setSizeFull();
        getContent().add(layoutRow);
        getContent().setFlexGrow(1, layoutRow);
        getContent().setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        getContent().setAlignItems(FlexComponent.Alignment.START);
        getContent().getStyle().set("flex-grow", "1");
        getContent().setWidth("100%");

        // Populate the grid with data
        setGridUserData(stripedGridUsers);

        setGridProjectData(stripedGridUserProjects);
    }

    private Long getCurrentUserId() {
        return authenticatedUser.get().map(User::getId).orElse(null);
    }








// типо говно
    private void setGridUserData(Grid<User> grid) {
        // Initialize filterValues with empty strings for each column
        grid.getColumns().forEach(column -> filterValues.add(""));

        grid.setItems(query -> userService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());

        grid.setColumns("id", "banned", "username", "name", "roles", "email");

        // Create a header row for filters
        HeaderRow filterRow = grid.appendHeaderRow();

        // Create a filter field for each column and add a value change listener
        for (Grid.Column<User> column : grid.getColumns()) {
            if ("banned".equals(column.getKey()) || "roles".equals(column.getKey())) {
                ComboBox<String> filterComboBox = new ComboBox<>();
                if ("banned".equals(column.getKey())) {
                    filterComboBox.setItems("true", "false");
                } else if ("roles".equals(column.getKey())) {
                    filterComboBox.setItems(Role.USER.toString(), Role.ADMIN.toString());
                }
                filterComboBox.setPlaceholder("Select");
                filterComboBox.setClearButtonVisible(true);
                filterComboBox.addValueChangeListener(event -> {
                    String selectedValue = filterComboBox.getValue() != null ? filterComboBox.getValue() : "";
                    filterValues.set(grid.getColumns().indexOf(column), selectedValue); // Save the filter value
                    grid.setItems(query -> userService.filteredList(
                                    filterValues,
                                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                            .stream());
                    System.out.println(filterValues); // Print the filterValues list
                });
                filterComboBox.setSizeFull();
                filterRow.getCell(column).setComponent(filterComboBox);
            } else {
                TextField filterField = new TextField();
                filterField.setValueChangeMode(ValueChangeMode.EAGER);
                filterField.addValueChangeListener(event -> {
                    filterValues.set(grid.getColumns().indexOf(column), filterField.getValue()); // Save the filter value
                    grid.setItems(query -> userService.filteredList(
                                    filterValues,
                                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                            .stream());
                    System.out.println(filterValues); // Print the filterValues list
                });
                filterField.setSizeFull();
                filterField.setPlaceholder("Filter");
                filterRow.getCell(column).setComponent(filterField);
            }
        }
    }

    private void setGridProjectData(Grid grid) {
        grid.setItems(query -> samplePersonService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
    }









// с сохранением в лист
//    private void setGridUserData(Grid<User> grid) {
//        // Initialize filterValues with empty strings for each column
//        grid.getColumns().forEach(column -> filterValues.add(""));
//
//        grid.setItems(query -> userService.list(
//                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                .stream());
//
//        grid.setColumns("id", "banned", "username", "name", "roles", "email");
//
//        // Create a header row for filters
//        HeaderRow filterRow = grid.appendHeaderRow();
//
//        // Create a filter field for each column and add a value change listener
//        grid.getColumns().forEach(column -> {
//            if ("banned".equals(column.getKey()) || "roles".equals(column.getKey())) {
//                ComboBox<String> filterComboBox = new ComboBox<>();
//                if ("banned".equals(column.getKey())) {
//                    filterComboBox.setItems("true", "false");
//                } else if ("roles".equals(column.getKey())) {
//                    filterComboBox.setItems(Role.USER.toString(), Role.ADMIN.toString());
//                }
//                filterComboBox.setPlaceholder("Select");
//                filterComboBox.setClearButtonVisible(true);
//                filterComboBox.addValueChangeListener(event -> {
//                    String selectedValue = filterComboBox.getValue() != null ? filterComboBox.getValue() : "";
//                    grid.setItems(query -> userService.filteredList(
//                                    selectedValue, column.getKey(),
//                                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                            .stream());
//                    grid.getDataProvider().refreshAll(); // Refresh the grid data
//                    filterValues.set(grid.getColumns().indexOf(column), selectedValue); // Save the filter value
//                    System.out.println(filterValues); // Print the filterValues list
//                });
//                filterComboBox.setSizeFull();
//                filterRow.getCell(column).setComponent(filterComboBox);
//            } else {
//                TextField filterField = new TextField();
//                filterField.setValueChangeMode(ValueChangeMode.EAGER);
//                filterField.addValueChangeListener(event -> {
//                    grid.setItems(query -> userService.filteredList(
//                                    filterField.getValue(), column.getKey(),
//                                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                            .stream());
//                    grid.getDataProvider().refreshAll(); // Refresh the grid data
//                    filterValues.set(grid.getColumns().indexOf(column), filterField.getValue()); // Save the filter value
//                    System.out.println(filterValues); // Print the filterValues list
//                });
//                filterField.setSizeFull();
//                filterField.setPlaceholder("Filter");
//                filterRow.getCell(column).setComponent(filterField);
//            }
//        });
//    }





//    private void setGridUserData(Grid<User> grid) {
//        grid.setItems(query -> userService.list(
//                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                .stream());
//
//        grid.setColumns("id", "banned", "username", "name", "roles", "email");
//
//        // Create a header row for filters
//        HeaderRow filterRow = grid.appendHeaderRow();
//
//        // Create a filter field for each column and add a value change listener
//        grid.getColumns().forEach(column -> {
//            if ("banned".equals(column.getKey()) || "roles".equals(column.getKey())) {
//                ComboBox<String> filterComboBox = new ComboBox<>();
//                if ("banned".equals(column.getKey())) {
//                    filterComboBox.setItems("true", "false");
//                } else if ("roles".equals(column.getKey())) {
//                    filterComboBox.setItems(Role.USER.toString(), Role.ADMIN.toString());
//                }
//                filterComboBox.setPlaceholder("Select");
//                filterComboBox.setClearButtonVisible(true);
//                filterComboBox.addValueChangeListener(event -> {
//                    String selectedValue = filterComboBox.getValue() != null ? filterComboBox.getValue() : "";
//                    grid.setItems(query -> userService.filteredList(
//                                    selectedValue, column.getKey(),
//                                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                            .stream());
//                    grid.getDataProvider().refreshAll(); // Refresh the grid data
//                });
//                filterComboBox.setSizeFull();
//                filterRow.getCell(column).setComponent(filterComboBox);
//            } else {
//                TextField filterField = new TextField();
//                filterField.setValueChangeMode(ValueChangeMode.EAGER);
//                filterField.addValueChangeListener(event -> {
//                    grid.setItems(query -> userService.filteredList(
//                                    filterField.getValue(), column.getKey(),
//                                    PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                            .stream());
//                    grid.getDataProvider().refreshAll(); // Refresh the grid data
//                });
//                filterField.setSizeFull();
//                filterField.setPlaceholder("Filter");
//                filterRow.getCell(column).setComponent(filterField);
//            }
//        });
//    }


    //старый вариант
//    private void setGridProjectData(Grid grid) {
//        grid.setItems(query -> samplePersonService.list(
//                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                .stream());
//    }


    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            User user = authenticatedUser.get().get();
            if (user.isBanned()) {
                event.forwardTo("banned-view");
            }
        }
    }
}
