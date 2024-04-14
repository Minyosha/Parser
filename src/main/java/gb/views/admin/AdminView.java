package gb.views.admin;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.model.Select;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import gb.data.SamplePerson;
import gb.data.User;
import gb.data.UserRepository;
import gb.security.AuthenticatedUser;
import gb.services.SamplePersonService;
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
    private UserRepository userRepository;
    public AdminView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser; // Set the authenticated user

        // Create the text fields
        TextField idTextField = new TextField("id");
        idTextField.setReadOnly(true);
//        idTextField.setValue("id");
        TextField usernameTextField = new TextField("Username");
        TextField nameTextField = new TextField("Name");
        TextField rolesTextField = new TextField("Roles");
        rolesTextField.setReadOnly(true);
        TextField emailTextField = new TextField("E-mail");

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

        // Create a column layout for the text fields and buttons
        VerticalLayout layoutColumn2 = new VerticalLayout();
        layoutColumn2.setWidth(null); // Width is determined by the widest component
        layoutColumn2.add(idTextField, bannedRadioGroup, usernameTextField, nameTextField, rolesTextField, emailTextField, updateUserButton, deleteUserButton);
        layoutColumn2.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        // Create the grid, set it to take full height and full width
        Grid<User> stripedGrid = new Grid<>(User.class);
        stripedGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        stripedGrid.setHeightFull();
        stripedGrid.setWidthFull();

        // Add the grid to the layout
        stripedGrid.asSingleSelect().addValueChangeListener(event -> {
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
            User selectedUser = stripedGrid.asSingleSelect().getValue();
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
                    stripedGrid.getDataProvider().refreshItem(userToUpdate);
                    Notification notification = new Notification("User " + userToUpdate.getUsername() + " updated", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.open();
                } else {
                    // Пользователь не найден, обработка ошибки
                }
            }
        });

        deleteUserButton.addClickListener(event -> {
            User selectedUser = stripedGrid.asSingleSelect().getValue();
            if (selectedUser != null) {
                // Получение последней версии пользователя из базы данных
                User userToDelete = userService.findById(selectedUser.getId());
                if (userToDelete != null) {
                    // Удаление пользователя
                    userService.delete(userToDelete.getId());
                    // Обновление таблицы
                    stripedGrid.getDataProvider().refreshAll();
                    Notification notification = new Notification("User " + userToDelete.getUsername() + " deleted", 3000);
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.open();
                } else {
                    // Пользователь не найден, обработка ошибки
                }
            }
        });

        // Create a row layout, add the column layout and the grid to it
        HorizontalLayout layoutRow = new HorizontalLayout();
        layoutRow.setHeightFull();
        layoutRow.setWidthFull();
        layoutRow.add(layoutColumn2, stripedGrid);
        layoutRow.setFlexGrow(0, layoutColumn2); // Column layout does not grow
        layoutRow.setFlexGrow(1, stripedGrid); // Grid takes the remaining space

        // Ensure the button column doesn't grow and the grid takes the remaining space
        layoutRow.setFlexGrow(0, layoutColumn2);
        layoutRow.setFlexGrow(1, stripedGrid);

        // Configure the content layout, add the row layout to it, and set it to take full size
        getContent().setSizeFull();
        getContent().add(layoutRow);
        getContent().setFlexGrow(1, layoutRow);
        getContent().setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        getContent().setAlignItems(FlexComponent.Alignment.START);
        getContent().getStyle().set("flex-grow", "1");
        getContent().setWidth("100%");

        // Populate the grid with data
        setGridSampleData(stripedGrid);
    }

    private void setGridSampleData(Grid grid) {
        grid.setItems(query -> userService.list(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());

        grid.setColumns("id", "banned", "username", "name", "roles", "email");
    }



    @Autowired()
    private SamplePersonService samplePersonService;





//    public AdminView(AuthenticatedUser authenticatedUser) {
//        this.authenticatedUser = authenticatedUser;
//        Grid multiSelectGrid = new Grid(User.class);
//        getContent().setWidth("100%");
//        getContent().getStyle().set("flex-grow", "1");
//        multiSelectGrid.setSelectionMode(Grid.SelectionMode.MULTI);
//        multiSelectGrid.setWidth("100%");
//        multiSelectGrid.getStyle().set("flex-grow", "0");
//        setGridSampleData(multiSelectGrid);
//        getContent().add(multiSelectGrid);
//    }
//
//    private void setGridSampleData(Grid grid) {
//        grid.setItems(query -> userService.list(
//                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                .stream());
//
//        grid.setColumns("id", "banned", "username", "name", "roles", "email");
//
//
//
//    }

//    private void setGridSampleData(Grid<User> grid) {
//        grid.setItems(query -> userService.list(
//                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
//                .stream());
//
//        grid.addColumn(User::getId).setHeader("ID");
//        grid.addColumn(User::getUsername).setHeader("Username");
//        grid.addColumn(User::getName).setHeader("Name");
//        grid.addColumn(User::getRoles).setHeader("Roles");
//        grid.addColumn(User::getEmail).setHeader("Email");
//
//        grid.addColumn(new ComponentRenderer<>(user -> {
//            Checkbox checkbox = new Checkbox();
//            checkbox.setValue(user.isBanned());
//            checkbox.addValueChangeListener(event -> {
//                user.setBanned(checkbox.getValue());
//                userRepository.save(user); // Сохранение изменений
//            });
//            return checkbox;
//        })).setHeader("Banned");
//    }

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
