package idiExt;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 判断是不是属于内部类的过滤器
 */
public class InnerFileFilter implements FilenameFilter {
    private String outName; //外部类的名称(不包含前绰与扩展名称)

    @Override
    public boolean accept(File dir, String name) {
        if (name != null){
            String s=outName.replaceFirst("/","");
            if (name.equals(s)) return true;
            if (outName.indexOf(".class")>0){
                s=s.replaceAll(".class","");
                return name.indexOf(s + "$") >= 0;
            }
        }
        return false;
    }

    public String getOutName() {
        return outName;
    }

    public void setOutName(String outName) {
        this.outName = outName;
    }


    public InnerFileFilter(String outName) {
        this.outName = outName;
    }
}
