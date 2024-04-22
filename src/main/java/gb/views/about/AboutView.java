package gb.views.about;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gb.data.User;
import gb.security.AuthenticatedUser;
import gb.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


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

        TextField urlField = new TextField();
        urlField.setPlaceholder("Enter URL");
        urlField.setWidthFull();

        TextArea textField = new TextArea();
        textField.setSizeFull();

        Button fetchButton = new Button("Fetch");
        fetchButton.addClickListener(e -> {
            String url = urlField.getValue();
            try {
                Connection.Response response = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                        .referrer("http://www.google.com")
                        .timeout(10000)
                        .followRedirects(true)
                        .execute();

                Document document = Jsoup.parse(response.body());
                String formattedHtml = document.html();

                textField.setValue(formattedHtml);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        VerticalLayout layout = new VerticalLayout(urlField, fetchButton, textField);
        layout.setFlexGrow(1, urlField, fetchButton, textField);
        layout.setWidthFull();
        layout.setHeightFull();
        getContent().add(layout);
    }

    private static List<String> readLinesFromFile(String fileName) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(String.format(" %s", line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
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
