package com.neo.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.ClassScaner;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Lgy
 * @desc spring 容器启动后首先执行的操作
 * @date 2019-05-09
 */
@Slf4j
@Component
public class ReqUriScanner implements ApplicationRunner {
    private static final String ACTION_PACKAGE = "com.yrd.fund.user.gateway.api";

    private static final Set<String> urls = new HashSet<>();
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("---CallbackAfterSpringContainerInitializer---");
        this.loadAllClzUris();
    }
    public static Set<String> getAllUri(){
        return urls;
    }

    private void loadAllClzUris(){
        Set<Class<?>> classes = ClassScaner.scanPackage(ACTION_PACKAGE);

        for (Class<?> aClass : classes) {
            String[] prefix = null;
            RequestMapping[] mappings = aClass.getAnnotationsByType(RequestMapping.class);
            if(ArrayUtil.isNotEmpty(mappings)){
                prefix = mappings[0].value();
            }
            List<String> methodUrl = new ArrayList<>();
            Method[] methods = aClass.getMethods();
            for (Method method : methods) {

                RequestMapping[] reqMap = method.getAnnotationsByType(RequestMapping.class);
                if(ArrayUtil.isNotEmpty(reqMap)){
                    methodUrl.addAll(CollUtil.toList(reqMap[0].value()));
                    continue;
                }
                PostMapping[] postMap = method.getAnnotationsByType(PostMapping.class);
                if(ArrayUtil.isNotEmpty(postMap)){
                    methodUrl.addAll(CollUtil.toList(postMap[0].value()));
                    continue;
                }
                GetMapping[] getMap = method.getAnnotationsByType(GetMapping.class);
                if(ArrayUtil.isNotEmpty(getMap)){
                    methodUrl.addAll(CollUtil.toList(getMap[0].value()));
                    continue;
                }
                PutMapping[] putMap = method.getAnnotationsByType(PutMapping.class);
                if(ArrayUtil.isNotEmpty(putMap)){
                    methodUrl.addAll(CollUtil.toList(putMap[0].value()));
                    continue;
                }
                DeleteMapping[] delMap = method.getAnnotationsByType(DeleteMapping.class);
                if(ArrayUtil.isNotEmpty(delMap)){
                    methodUrl.addAll(CollUtil.toList(delMap[0].value()));
                    continue;
                }
                PatchMapping[] patchMap = method.getAnnotationsByType(PatchMapping.class);
                if(ArrayUtil.isNotEmpty(patchMap)){
                    methodUrl.addAll(CollUtil.toList(patchMap[0].value()));
                }
            }
            loadClzUri(prefix,methodUrl);
        }
    }
    private  void  loadClzUri(String[] prefix,List<String> methodUrl){
        if(ArrayUtil.isNotEmpty(prefix)){
            for (String pre : prefix) {
                if(CollUtil.isNotEmpty(methodUrl)){
                    for (String meth : methodUrl) {
                        urls.add(StrUtil.format("{}/{}",StrUtil.removeSuffix(pre,"/"),StrUtil.removePrefix(meth,"/")));
                    }
                    continue;
                }
                urls.add(StrUtil.removeSuffix(pre,"/"));
            }
            return;
        }
        if(CollUtil.isNotEmpty(methodUrl)){
            for (String meth : methodUrl) {
                urls.add(StrUtil.addPrefixIfNot(meth,"/"));
            }

        }
    }

}