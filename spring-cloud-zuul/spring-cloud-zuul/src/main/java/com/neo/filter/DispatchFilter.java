package com.neo.filter;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * @author Lgy
 * @desc 分发请求过滤器
 * @date 2019-11-08
 */
@Component
@WebFilter(filterName="commonFilter",urlPatterns="/*")
public class DispatchFilter extends OncePerRequestFilter{
    private static final String PZ_BASE_PATH ="";
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)throws ServletException, IOException {
        Set<String> urls = ReqUriScanner.getAllUri();
        Console.log("callBack:{}",urls.size());
        if(urls.contains(req.getRequestURI())){
            filterChain.doFilter(req, res);
        }
        String url = StrUtil.format("{}/{}", StrUtil.removeSuffix(PZ_BASE_PATH, "/"), StrUtil.removePrefix(req.getRequestURI(), "/"));

        PrintWriter writer = null;
        try {
            String result = HttpUtil.post(url, ReqParamTool.getParamMap(req));
            writer = res.getWriter();
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer !=null ){
                writer.close();
            }
        }
    }
}
