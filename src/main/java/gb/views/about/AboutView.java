package gb.views.about;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import gb.data.User;
import gb.security.AuthenticatedUser;
import gb.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;


@PageTitle("About")
@Route(value = "about-view", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
@AnonymousAllowed
@Uses(Icon.class)
public class AboutView extends Composite<VerticalLayout> implements BeforeEnterObserver {
    private final AuthenticatedUser authenticatedUser;

    public AboutView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");

        VerticalLayout layout = new VerticalLayout();

        Html header = new Html("<h4>О проекте Parser</h4>");

        Html description = new Html("<div>Этот проект используется для скачивания файлов с сайтов используя поиск ссылок по шаблону, заданному пользователем.<br>"
                + "Для тестирования доступны два заранее созданных пользователя: <b>user</b> и <b>admin</b> с паролями <b>user</b> и <b>admin</b>.<br>" +
                "Пользователи могут создавать и запускать свои проекты, администраторы могут банить и удалять пользователей а так же просматривать их проекты.<br>" +
                "Для того, чтобы работать с проектами, необходимо будет скачать десктопное приложение из вкладки Projects - Download desktop client. " +
                "Данная опция доступна только для пользователей и не доступна для администраторов. Приложение представляет из себя jar файл и требует " +
                "для запуска установленной JVM последней версии.<br>" +
                "Во вкладке Projects - Download desktop client пользователь может скачать десктопное приложение и проверить его, отправив простой REST запрос.<br>" +
                "Во вкладке Create and select project пользователь может создать свой проект и добавить туда артикулы для поиска" +
                " и варианты сайтов, где искать файлы для скачивания.<br>" +
                "Во вкладке Modify project пользователь может задать параметры поиска и протестировать проект.<br>" +
                "Во вкладке Run project пользователь может запустить проект, поставить его на паузу, возобновить и получить отчет в виде сгенерированных файлов с артикулами.<br>" +
                "Администраторы во вкладке Administration могут банить пользователей и просматривать список их проектов. Так же администраторы могут удалять пользователей" +
                " и производить поиск и сортировку по базе пользователей.<br>" +
                "Для начала работы с сервисом воспользуйтесь ссылкой <a href='login'>Login</a><br></div>");

        Html recommended = new Html("<h4>Рекомендованные параметры для тестирования проекта</h4>");

        Text params = new Text("Используйте кнопки ниже для получения параметров поиска. Параметры будут скопированы в буфер обмена.\n" +
                "Для параметров offset используйте кнопку Calculate offset, установите значение смещения для параметра End search равным 0 и нижмите кнопку Update operations.\n +" +
                "После этого нажмите кнопку Test operations и проверьте работоспособность программы. ");

        Button getArticles = new Button("Get articles");
        getArticles.setWidth("192px");
        getArticles.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getArticles.addClickListener(buttonClickEvent -> {
            Notification notification = new Notification("Articles are copied to the clipboard! Use Ctrl+V for pasting!", 5000);
            UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)", "18622\n" +
                    "18613\n" +
                    "18616\n" +
                    "18615\n" +
                    "18611\n" +
                    "18610\n" +
                    "18621\n" +
                    "18617\n" +
                    "18612\n" +
                    "18614\n" +
                    "18618\n" +
                    "18623");
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
        });

        Button getVariants = new Button("Get variants");
        getVariants.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getVariants.setWidth("192px");
        getVariants.addClickListener(buttonClickEvent -> {
            Notification notification = new Notification("Variants are copied to the clipboard! Use Ctrl+V for pasting!", 5000);
            UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)", "https://www.se.com/in/en/product/\n" +
                    "https://www.se.com/mv/en/product/\n" +
                    "https://www.se.com/fj/en/product/");
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
        });

        Button getStartSearch = new Button("Start search");
        getStartSearch.setWidth("192px");
        getStartSearch.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getStartSearch.addClickListener(buttonClickEvent -> {
            Notification notification = new Notification("Start search is copied to the clipboard! Use Ctrl+V for pasting!", 5000);
            UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)", "Product picture Schneider Electric&quot;,&quot;url&quot;:&quot;");
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
        });

        Button getEndSearch = new Button("End search");
        getEndSearch.setWidth("192px");
        getEndSearch.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getEndSearch.addClickListener(buttonClickEvent -> {
            Notification notification = new Notification("End search is copied to the clipboard! Use Ctrl+V for pasting!", 5000);
            UI.getCurrent().getPage().executeJs("window.copyToClipboard($0)", ";p_File_Type=rendition_1500_jpg&quot;}");
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();
        });


        layout.add(header, description, recommended, params, getArticles, getVariants, getStartSearch, getEndSearch);
        layout.setFlexGrow(1);
        layout.setWidthFull();
        layout.setHeightFull();
        getContent().add(layout);
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
