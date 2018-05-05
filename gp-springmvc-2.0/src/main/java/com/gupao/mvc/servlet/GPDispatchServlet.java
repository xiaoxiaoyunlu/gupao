package com.gupao.mvc.servlet;

import com.gupao.mvc.annotation.GPController;
import com.gupao.mvc.annotation.GPRequestMapping;
import com.gupao.mvc.annotation.GPRequestParam;
import com.gupao.mvc.aop.GPAopProxyUtils;
import com.gupao.mvc.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * springmvc 2.0
 */
public class GPDispatchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String LOCATION = "contextConfigLocation";

    //保存所有的url 和 方法的映射关系
    //GPHandlerMapping最核心的设计，也是最经典的
    //牛逼到直接干掉了Struts 和 Webwork等mvc框架
    private List<GPHandlerMapping> handlerMapping = new ArrayList<>();

    //保存所有的适配器
//    private List<GPHandlerAdapter> handlersAdapters = new ArrayList<>();
    private Map<GPHandlerMapping,GPHandlerAdapter> handlersAdapters=new HashMap<>();

    private List<GPViewResolver> viewResolvers = new ArrayList<>();

    public GPDispatchServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>Details:<br/>" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s","\r\n") +  "<font color='green'><i>Copyright@GupaoEDU</i></font>");
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp)throws Exception {
        //根据用户请求的url 获取一个handler Mapping
        GPHandlerMapping handler=getHandler(req);

        if(handler==null){
            //找不到  404
            resp.getWriter().write("404 not found");
        }
        //根据 handler mapping 获取一个 adapte
        GPHandlerAdapter adapter=getHanedlerAdapter(handler);


        //这一步只是调用方法，得到返回值？
        GPModelAndView mv=adapter.handler(req,resp,handler);

        processDispatchResult(resp,mv);
    }

    private void processDispatchResult(HttpServletResponse resp, GPModelAndView mv) throws Exception {
         //调用ViewResolver的resolverView方法
         if(mv==null){return;}
         if(this.viewResolvers.isEmpty()){return;}
        for (GPViewResolver viewResolver:this.viewResolvers) {
            if(!mv.getViewName().equals(viewResolver.getViewName())){continue;}
            String out = viewResolver.viewResolver(mv);
            if(out!=null){
                 resp.getWriter().write(out);
            }
        }
    }

    private GPHandlerAdapter getHanedlerAdapter(GPHandlerMapping handler) {
        if(this.handlersAdapters.isEmpty()){return null;}
        return this.handlersAdapters.get(handler);
    }

    private GPHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMapping.isEmpty()){return null;}
        String url=req.getRequestURI();
        String contextPath=req.getContextPath();
        url=url.replace(contextPath,"").replaceAll("/+","/");
        for(GPHandlerMapping handler:this.handlerMapping){
            Matcher matcher=handler.getPattern().matcher(url);
            if(!matcher.matches()){
                continue;
            }
            return handler;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化容器
        GPApplicationContext context = new GPApplicationContext(config.getInitParameter(LOCATION));
        //参照springmvc  九大组件来模仿

//        initMultiPartResolver(context);
//        initLocaleReslover(context);
//        initThemeResolver(context);
//        initHandlerMappings(context);
//        initHandlerApapters(context);
//        initHandlerExceptionResolvers(context);
//        initRequestToViewNameTranslator(context);
//        initViewResolvers(context);
//        initFlashMapManager(context);

        initStrategies(context);

    }

    private void initStrategies(GPApplicationContext context) {

        //有九种策略
        // 针对于每个用户请求，都会经过一些处理的策略之后，最终才能有结果输出
        // 每种策略可以自定义干预，但是最终的结果都是一致
        // ModelAndView

        // =============  这里说的就是传说中的九大组件 ================
        initMultipartResolver(context);//文件上传解析，如果请求类型是multipart将通过MultipartResolver进行文件上传解析
        initLocaleResolver(context);//本地化解析
        initThemeResolver(context);//主题解析

        /** 我们自己会实现 */
        //GPHandlerMapping 用来保存Controller中配置的RequestMapping和Method的一个对应关系
        initHandlerMappings(context);//通过HandlerMapping，将请求映射到处理器
        /** 我们自己会实现 */
        //HandlerAdapters 用来动态匹配Method参数，包括类转换，动态赋值
        initHandlerAdapters(context);//通过HandlerAdapter进行多类型的参数动态匹配

        initHandlerExceptionResolvers(context);//如果执行过程中遇到异常，将交给HandlerExceptionResolver来解析
        initRequestToViewNameTranslator(context);//直接解析请求到视图名

        /** 我们自己会实现 */
        //通过ViewResolvers实现动态模板的解析
        //自己解析一套模板语言
        initViewResolvers(context);//通过viewResolver解析逻辑视图到具体视图实现

        initFlashMapManager(context);//flash映射管理器
    }

    private void initFlashMapManager(GPApplicationContext context) {

    }

    /**
     * 初始化视图模板
     *
     * @param context
     */
    private void initViewResolvers(GPApplicationContext context) {
        // 比如在访问页面文件   Http://localhost/first.html
        //解决页面文字和模板文件关联的问题

        String templateRoot = context.getConfig().getProperty("templateRoot");

        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File template:templateRootDir.listFiles()) {
            //要创构建一个list来保存 viewResolver
            this.viewResolvers.add(new GPViewResolver(template.getName(),template));

        }

    }

    private void initLocaleResolver(GPApplicationContext context) {
    }

    private void initMultipartResolver(GPApplicationContext context) {
    }

    private void initThemeResolver(GPApplicationContext context) {

    }

    /**
     * 将Controler中配置的RequestMapping 和Method 一一对应起来
     * @param context
     */
    private void initHandlerMappings(GPApplicationContext context) {

        //按照我们通常的做法  应该是map
        //Map<String,Method> map;
        // map.put(url,mthod);

        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName:beanNames) {
            //到了MVC层，对外提供的方法只有一个getBean()方法
            //返回的对象不是BeanWrapper 而是一个代理？ 怎么办？
            //想办法 GPAopProxyUtils 的  getTargetObject 来拿到原始object
//            Object controller = context.getBean(beanName);

            //getBean获取出来的都是代理对象  用来调用
            Object proxy=context.getBean(beanName);
            //获取代理的原始对象  用来做判断
            Object controller= null;
            try {
                controller = GPAopProxyUtils.getTargetObject(proxy);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Class<?> clazz = controller.getClass();
            //判断是不是GPController
            if(!clazz.isAnnotationPresent(GPController.class)){continue;}
            String baseUrl="";
            if(clazz.isAnnotationPresent(GPRequestMapping.class)){
                GPRequestMapping requestMapping=clazz.getAnnotation(GPRequestMapping.class);
                baseUrl=requestMapping.value().trim();
            }

            //扫描所有的public method
            Method[] methods = clazz.getMethods();
            String regex="";
            Pattern pattern=null;
            for (Method method:methods) {
                if(!method.isAnnotationPresent(GPRequestMapping.class)){ continue;}
                GPRequestMapping requestMapping=method.getAnnotation(GPRequestMapping.class);
                regex=baseUrl+requestMapping.value().trim().replaceAll("\\*",".*").replaceAll("/+","/");
                pattern=Pattern.compile(regex);

                this.handlerMapping.add(new GPHandlerMapping(controller,method,pattern));
                System.out.println("Maping:"+regex+","+method);
            }
        }
        

    }

    /**
     * 把映射的所有url对应的method的参数动态配置
     * 好比1.0 里面的  doAutoWired()方法
     * 命名参数？  带有  GPRequestParam
     * 非命名参数？ HttpServletRequest  HtttpServletResponse
     * @param context
     */
    private void initHandlerAdapters(GPApplicationContext context) {
        //初始化阶段我们能做的就是把参数的名字或者类型按照一定顺序存储下来
        //因为通过反射调用的时候，传的形参是一个数组
        //可以通过记录这些参数的index ,挨个从数组中填充  这样就和参数顺序无关了
        for (GPHandlerMapping handleMapping:this.handlerMapping) {
            //每个方法都有一个参数列表，保存形参列表
            Map<String,Integer> paramMapping=new HashMap<>();

            //因为每个参数 可能会有多个注解来标注，因此用一个二维数组来存储
            Annotation[][] parameterAnnotations = handleMapping.getMethod().getParameterAnnotations();

            for(int i=0;i<parameterAnnotations.length;i++){
                for(Annotation a:parameterAnnotations[i]){
                    if(a instanceof GPRequestParam){
                        String paramName = ((GPRequestParam) a).value().trim();
                        if(!"".equals(paramName)){
                            paramMapping.put(paramName,i);
                        }
                    }
                }
            }
            //处理非命名参数
            Class<?>[] parameterTypes = handleMapping.getMethod().getParameterTypes();
            for(int i=0;i<parameterTypes.length;i++){
                Class<?> type=parameterTypes[i];
                if(type==HttpServletRequest.class || type==HttpServletResponse.class){
                    paramMapping.put(type.getName(),i);
                }
            }
            this.handlersAdapters.put(handleMapping,new GPHandlerAdapter(paramMapping));

        }


    }

    private void initHandlerExceptionResolvers(GPApplicationContext context) {

    }

    private void initRequestToViewNameTranslator(GPApplicationContext context) {

    }


}
