package ru.abagiev.examples.spring.intercept.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.currentTimeMillis;

public class RestControllerProxy implements MethodInterceptor {

    private final Object bean;
    private final Map<String, MethodDef> methods;
    private final Logger log;
    private String pathPrefix = "";

    public RestControllerProxy(Object bean, Class<?> clazz) {
        this.bean = bean;
        this.methods = new ConcurrentHashMap<>();
        this.log = LoggerFactory.getLogger(clazz);

        RequestMapping rm = clazz.getAnnotation(RequestMapping.class);
        if (rm != null) {
            this.pathPrefix = getRequestMappingPath(rm.value(), rm.path());
        }

        scanMethods(clazz);
    }

    private void scanMethods(Class<?> clazz) {
        for (Method m : clazz.getMethods()) {
            /* Check that method is a request mapping method */
            MappingDef mapping = getRequestMapping(m);
            if (mapping == null) {
                continue;
            }

            /* Check that return type is Mono */
            Class<?> returnType = m.getReturnType();
            if (!returnType.isAssignableFrom(Mono.class)) {
                continue;
            }

            MethodDef def = new MethodDef();
            def.mapping = mapping;
            methods.put(m.getName(), def);

            /* Scan for arguments */
            Parameter[] params = m.getParameters();
            for (int i = 0; i < params.length; i++) {
                Parameter p = params[i];
                if (p.isAnnotationPresent(RequestBody.class) && !def.hasBodyDto) {
                    def.hasBodyDto = true;
                    def.bodyDtoIndex = i;
                } else if (p.isAnnotationPresent(PathVariable.class)) {
                    def.pathVarList.add(VarDef.of(i, "{" + p.getName() + "}"));
                } else if (p.isAnnotationPresent(RequestParam.class)) {
                    def.reqParamList.add(VarDef.of(i, p.getName()));
                }
            }
        }
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        try {
            /* Check if this is remote method */
            MethodDef def = methods.get(method.getName());
            if (def == null) {
                return method.invoke(bean, args);
            }

            /* Fetch arguments */
            Object bodyDto = def.hasBodyDto ? args[def.bodyDtoIndex] : null;
            String path = getPath(def, args);
            String reqParams = getRequestParams(def.reqParamList, args);

            /* Save current nanoseconds */
            long millis = currentTimeMillis();

            try {
                /* Invoke method */
                Mono<?> mono = (Mono<?>) method.invoke(bean, args);
                if (mono == null) {
                    return Mono.error(new RuntimeException("Method returned no mono: " + method.getName()));
                }

                /* Log result and return mono */
                return mono.doOnSuccessOrError((rsp, error) -> {
                    log(def.mapping.method, path, bodyDto, reqParams, rsp, error, currentTimeMillis() - millis);
                });
            } catch (InvocationTargetException e) {
                log(def.mapping.method, path, bodyDto, reqParams, null, e.getCause(), currentTimeMillis() - millis);
                return Mono.error(e.getCause());
            }
        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

    private MappingDef getRequestMapping(Method method) {
        RequestMapping rm = method.getAnnotation(RequestMapping.class);
        if (rm != null && rm.method().length > 0) {
            return MappingDef.of(rm.method()[0], joinPrefix(getRequestMappingPath(rm.value(), rm.path())));
        }

        GetMapping get = method.getAnnotation(GetMapping.class);
        if (get != null) {
            return MappingDef.of(RequestMethod.GET, joinPrefix(getRequestMappingPath(get.value(), get.path())));
        }

        PostMapping post = method.getAnnotation(PostMapping.class);
        if (post != null) {
            return MappingDef.of(RequestMethod.POST, joinPrefix(getRequestMappingPath(post.value(), post.path())));
        }
        return null;
    }

    private String joinPrefix(String path) {
        return (pathPrefix + "/" +  path).replace("///", "/").replace("//", "/");
    }

    private String getRequestMappingPath(String[] value, String[] path) {
        if (value.length > 0) {
            return value[0];
        }
        if (path.length > 0) {
            return path[0];
        }
        return "";
    }

    private String getPath(MethodDef def, Object[] args) {
        String path = def.mapping.path;
        for (VarDef v : def.pathVarList) {
            Object value = args[v.index];
            if (value != null) {
                path = path.replace(v.name, value.toString());
            }
        }
        return path;
    }

    private String getRequestParams(List<VarDef> reqParamList, Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (VarDef v : reqParamList) {
            Object value = args[v.index];
            sb.append(", ").append(v.name).append("=").append(value);
        }
        return sb.toString();
    }

    private void log(RequestMethod method, String path, Object req, String reqParams, Object rsp, Throwable error, Long millis) {
        StringBuilder sb = new StringBuilder();

        switch (method) {
            case GET:
                sb.append("GET ");
                break;
            case POST:
                sb.append("POST ");
                break;
            default:
                sb.append(method.toString()).append(" ");
                break;
        }

        sb.append(path);

        if (req != null) {
            sb.append(", req=").append(req.toString());
        }

        sb.append(reqParams);

        if (rsp != null) {
            sb.append(", rsp=").append(rsp.toString());
        }

        if (error != null) {
            sb.append(", error=").append(error.getMessage());
        }

        if (millis != null) {
            sb.append(", time=").append(millis);
        }

        if (error != null) {
            log.error(sb.toString());
        } else {
            log.info(sb.toString());
        }
    }

    private static class MethodDef {
        private MappingDef mapping;
        private boolean hasBodyDto;
        private List<VarDef> pathVarList = new ArrayList<>();
        private List<VarDef> reqParamList = new ArrayList<>();
        private int bodyDtoIndex;
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class MappingDef {
        private final RequestMethod method;
        private final String path;
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class VarDef {
        private final int index;
        private final String name;
    }
}
