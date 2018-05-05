package com.gupao.mvc.servlet;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 设计这个类的目的是
 * 1、将一个静态文件变为一个动态文件
 * 2、根绝用户传入参数不同，产生不同结果
 * 最终输出字符串交给response处理
 */
public class GPViewResolver {

    private String viewName;
    private File templateFile;

    public GPViewResolver(String viewName, File templateFile) {
        this.viewName = viewName;
        this.templateFile = templateFile;
    }

    public String viewResolver(GPModelAndView mv) throws Exception{
        //读取模板文件，挨个匹配处理
        StringBuffer  sb=new StringBuffer();
        RandomAccessFile ra=new RandomAccessFile(this.templateFile,"r");

        try {
            String line=null;
            while((line=ra.readLine())!=null){
                //页面默认编码是 ISO-8859-1  要转为 utf8 防止乱码
               line=new String(line.getBytes("ISO-8859-1"), "utf-8");

               Matcher matcher=matcher(line);
               while(matcher.find()){
                   for(int i=1;i<=matcher.groupCount();i++){
                       //要把￥{}中间的这个字符串给取出来
                       String paramName=matcher.group(i);

                       Object paramValue = mv.getModelMap().get(paramName);
                       if(null==paramValue){continue;}
                       line=line.replaceAll("￥\\{" + paramName + "\\}",paramValue.toString());

                       //转码回去
                       line=new String(line.getBytes("utf-8"),"ISO-8859-1");

                   }
               }
               sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            ra.close();
        }
        return sb.toString();
    }

    private Matcher matcher(String str){
        Pattern pattern = Pattern.compile("￥\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return  matcher;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }
}
