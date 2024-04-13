package gb.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import gb.views.login.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
                authorize -> authorize.requestMatchers(new AntPathRequestMatcher("/login")).permitAll());

//         Icons from the line-awesome addon

//        http.authorizeHttpRequests(authorize -> authorize
//                .requestMatchers(new AntPathRequestMatcher("/projects-view")).hasAnyAuthority("ROLE_USER"));
//
//        http.authorizeHttpRequests(authorize -> authorize
//                .requestMatchers(new AntPathRequestMatcher("/admin-view")).hasAnyAuthority("ROLE_USER"));

//        http.authorizeHttpRequests(authorize -> authorize
//                .requestMatchers(new AntPathRequestMatcher("/banned-view")).denyAll());


        super.configure(http);
        setLoginView(http, LoginView.class);
    }



}
