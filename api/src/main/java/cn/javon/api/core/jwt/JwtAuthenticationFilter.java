package cn.javon.api.core.jwt;

import cn.javon.api.core.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 身份认证过滤器
 *
 * @author Javon
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {

        // 解决跨域问题
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Content-Length, Authorization, Admin-Token, Accept, X-Requested-With");
        // 明确允许通过的方法，不建议使用*
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Expose-Headers", "*");

        // 预请求后，直接返回
        // 返回码必须为 200 否则视为请求失败
        if ("OPTIONS".equals(request.getMethod())) {
            return;
        }

        final String token = this.jwtUtil.getTokenFromRequest(request);
        if (token == null) {
            if (log.isInfoEnabled()) {
                log.info("=> Anonymous<{}> request URL<{}> Method<{}>", IpUtil.getIpAddress(request), request.getRequestURL(), request.getMethod());
            }
        } else {
            final String username = this.jwtUtil.getUsername(token);
            if (log.isInfoEnabled()) {
                log.info("=> user<{}> token : {}", username, token);
                log.info("=> request URL<{}> Method<{}>", request.getRequestURL(), request.getMethod());
            }
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                final UsernamePasswordAuthenticationToken authentication = this.jwtUtil.getAuthentication(username, token);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                if (log.isInfoEnabled()) {
                    log.info("=> user<{}> is authorized, set security context", username);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
