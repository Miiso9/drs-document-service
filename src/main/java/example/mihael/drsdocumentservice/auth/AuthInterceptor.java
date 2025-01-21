package example.mihael.drsdocumentservice.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String accessToken = (String) session.getAttribute("accessToken");
            if (accessToken != null) {
                request.setAttribute("Authorization", "Bearer " + accessToken);
            }
        }
        return true;
    }
}