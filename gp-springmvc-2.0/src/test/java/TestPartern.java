import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPartern {

    public static void main(String[] args) {
        String line="    <h1>大家好,我是￥{teacher}老师<br/>欢迎大家学习springmvc</h1>";
        System.out.printf(line.length()+"");
        Matcher matcher=matcher(line);
        for(int i=0;i<matcher.groupCount();i++){
            int start=matcher.start(i+1);
            int end=matcher.end(i+1);
            System.out.printf(line.substring(start,end));
        }
    }

    public static Matcher matcher(String str){
        Pattern pattern = Pattern.compile("￥\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return  matcher;
    }
}
