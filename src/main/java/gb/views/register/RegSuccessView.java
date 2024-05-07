package gb.views.register;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("regsuccess")
@AnonymousAllowed
public class RegSuccessView extends VerticalLayout {

    public RegSuccessView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        H1 successMessage = new H1("User registration successful!");
        H2 redirectMessage = new H2("Redirecting to the login page...");
        add(successMessage, redirectMessage);

        UI.getCurrent().getPage().executeJs("setTimeout(function() {window.location.href='login'}, 3000);");
    }
}