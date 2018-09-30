package com.atguigu.gmall.interceptor;

import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpClientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);

        if (null != methodAnnotation) {
            return true;
        }

        boolean b = methodAnnotation.needSuccess();
        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        String newToken = request.getParameter("token");

        if (StringUtils.isNotBlank(oldToken)&&StringUtils.isBlank(newToken)) {
            token = oldToken;
        }
        if (StringUtils.isBlank(oldToken)&&StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        if (StringUtils.isNotBlank(oldToken)&&StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        if (StringUtils.isNotBlank(token)) {
            String ip = getMyIpFromRequest(request);
            String url = "http://passport.gmall.com:8085/verify?token=" + token + "&currentIp=" + ip;

            String success = HttpClientUtil.doGet(url);

            if (success.equals("success")) {
                CookieUtil.setCookie(request, response, "oldToken", token, 1000 * 60 * 60 * 24, true);
                Map atguigugmall0508 = JwtUtil.decode("atguigugmall0508", token, ip);
                request.setAttribute("userId", atguigugmall0508.get("userId"));
                request.setAttribute("nickName", atguigugmall0508.get("nickName"));
                return true;
            }

        }
        if (b) {
            response.sendRedirect("http://passport.gmall.com:8085/index?returnUrl=" + request.getRequestURL());

            return false;
        } else {
            return true;
        }
    }

    private String getMyIpFromRequest(HttpServletRequest request) {

        String ip = "";
        ip = request.getRemoteAddr();
        if (StringUtils.isBlank(ip)) {
            ip = request.getHeader("x-forwarder-for");
            if (StringUtils.isBlank(ip)) {
                ip = "192.168.0.16";
            }
        }

        return ip;
    }


}
