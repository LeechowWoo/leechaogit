package com.leechao.didi.filter;

import com.alibaba.fastjson.JSON;
import com.leechao.didi.common.BaseContext;
import com.leechao.didi.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        //1、获取本次请求的URI
        String requestURI=request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        //将不需要过滤器处理的请求放在下面的数组中
        String[]urls=new String[]{
            "/employee/login",
            "/employee/logout",
            "/backend/**",//静态资源直接放行，静态资源可以被直接查看，重要的是不会被看见controller中的数据
            "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3、如果不需要处理直接放行
        if(check){
            log.info("本次请求 {} 不需要处理",requestURI);
            //如果返回时true，则说明已经和和需要放行的url匹配成功，可以放行
            //doFilter(request,response)的方法就是在你打开一个页面当满足过滤器的条件的时候，他就会继续执行你打开页面时候的操作
            filterChain.doFilter(request,response);
            return;
        }

        //4-1、判断登录状态，如果已经登录则直接放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2、判断登录状态，如果已经登录则直接放行
        if(request.getSession().getAttribute("user")!=null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        //5、如果还未登录则返回登录结果，采用前端写好的前端拦截器进行拦截，只需要将数据返回给前端即可
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));//通过输出流将数据返回到前端
        /*
        前端实现页面的跳转，跳转到登录页面
         */
        return;
    }

    /**
     * 路径匹配，检查此次访问路径是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            //遍历url，与传进来的url进行匹配
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
