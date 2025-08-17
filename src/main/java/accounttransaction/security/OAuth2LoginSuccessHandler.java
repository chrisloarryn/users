package accounttransaction.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    public OAuth2LoginSuccessHandler(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        String email = null;
        if (principal instanceof OAuth2User oAuth2User) {
            Object emailAttr = oAuth2User.getAttributes().get("email");
            email = emailAttr != null ? emailAttr.toString() : oAuth2User.getName();
        } else {
            email = authentication.getName();
        }
        String jwt = tokenProvider.generateToken(email);
        response.setContentType("application/json");
        response.getWriter().write("{\"jwt\":\"" + jwt + "\"}");
        response.getWriter().flush();
    }
}
