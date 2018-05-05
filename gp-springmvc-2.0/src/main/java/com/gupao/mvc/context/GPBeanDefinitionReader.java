package com.gupao.mvc.context;

import com.gupao.mvc.beans.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 对配置文件进行查找  读取  解析
 */
public class GPBeanDefinitionReader {
    private Properties config = new Properties();

    private List<String> registyBeanClasses = new ArrayList<String>();


    //在配置文件中，用来获取自动扫描的包名的key
    private final String SCAN_PACKAGE = "scanPackage";


    public GPBeanDefinitionReader(String... locations){
        //在Spring中是通过Reader去查找和定位对不对
        String filePath=locations[0].replace("classpath:","");
        InputStream fis=this.getClass().getClassLoader().getResourceAsStream(filePath);
        try {
            config.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));


    }

    //递归扫描所有的相关联的class，并且保存到一个List中
    private void doScanner(String packageName) {

        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.","/"));

        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()){
            if(file.isDirectory()){
                doScanner(packageName + "." +file.getName());
            }else {
                registyBeanClasses.add(packageName + "." + file.getName().replace(".class",""));
            }
        }


    }

    public Properties getConfig() {
        return config;
    }

    public List<String> loadBeanDefinitions(){ return this.registyBeanClasses;}

    /**
     * 每注册一个className，就返回一个BeanDefinition，我自己包装
     * 只是为了对配置信息进行一个包装
     * @param className
     * @return
     */
    public GPBeanDefinition registerBean(String className){

        if(this.registyBeanClasses.contains(className)){
            GPBeanDefinition beanDefinition=new GPBeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".")+1)));
            return beanDefinition;
        }
        return  null;
    }

    public String lowerFirstCase(String str){
        char[] chars=str.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

}
