package svn;

import idiExt.FileOperateDemo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 增量发布工具
 * <p>
 * 1、	可切换项目、显示项目本今日提交版本号、项目分期存储
 * 2、	解决后导出的文件不能覆盖之前文件问题
 */
@SuppressWarnings("JavadocReference")
public class DeltReleaseTool {
    //工程路径
    String projectPath = "F:/workspace/esp";
    //导出路径
    String exportPath = "D:/需求变更对应提交文件/";
    //工程路径
    String projectUrl;
    //日志查询类
    SVNLog sVNLog;
    //查询天数

    static int year = 2017;
    static int month = 1;
    static int day = 1;

    public DeltReleaseTool(String baseUrl, String projectUrl, String username, String password) throws SVNException {
        sVNLog = new SVNLog(baseUrl + projectUrl, username, password);
        this.projectUrl = projectUrl;
    }

    public DeltReleaseTool(String baseUrl, String projectUrl, String username, String password, String projectPath) throws SVNException {
        this(baseUrl, projectUrl, username, password);
        this.projectPath = projectPath;
    }

    /**
     * 生成发布包，区间版本发布，从verFrom到最新版本
     *
     * @param verFrom
     * @param verTo
     */
    public void creatPackWithLogs(long verFrom, String project) {
        List<SVNLogEntry> logs = sVNLog.getSVNLogs(verFrom);
        this.creatPack(logs, project);
    }

    /**
     * 生成发布包，区间版本发布
     *
     * @param verFrom
     * @param verTo
     */
    public void creatPackWithLogs(long verFrom, long verTo, String project) {
        List<SVNLogEntry> logs = sVNLog.getSVNLogs(verFrom, verTo);
        this.creatPack(logs, project);
    }

    /**
     * 生成发布包,版本区间发布
     *
     * @param verFrom
     * @param verTo
     */
    public void creatPack(List<SVNLogEntry> logs, String project) {
        //List<SVNLogEntry> logs = sVNLog.getSVNLogs(verFrom,verTo);
        Date today = new Date();
        DateFormat format = new SimpleDateFormat("MMdd");
        String todayStr = format.format(today);
        String exportPath = this.exportPath + "_" + project + "_" + todayStr;
        File logFile = new File(exportPath);
        if (!logFile.exists()) {
            logFile.mkdir();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(exportPath + "/log.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (SVNLogEntry log : logs) {
            Map<String, SVNLogEntryPath> map = log.getChangedPaths();
            for (Iterator entries = map.keySet().iterator(); entries.hasNext(); ) {
                Object key = entries.next();
                SVNLogEntryPath svnPath = map.get(key);
                String path = svnPath.getPath();
                if (path.split(projectUrl).length < 1) {
                    continue;
                }
                if (path.split(projectUrl).length >= 2) {
                    path = path.split(projectUrl)[1];
                } else {
                    path = path.split(projectUrl)[0];
                }
                String bigPath;
                if (path.startsWith("/java")||path.endsWith(".java")) {
                    path = path.replaceFirst("/","/WEB-INF/classes/").replaceFirst("/java/", "/");
                    if (path.endsWith(".java")) {
                        path = path.replaceFirst(".java", ".class");
                    }
                    bigPath = projectPath + path;//WebRoot
                } else if (path.startsWith("/webapp") || path.startsWith("/FTDMS/WebRoot")||path.indexOf("/WebRoot")>0) {
                   // path = path.replaceFirst("/FTDMS/WebRoot", "");
                    if (path.indexOf("/WebRoot")>0){
                       path= path.substring(path.indexOf("/WebRoot")+"/WebRoot".length());
                    }
                    path = path.replaceFirst("/webapp", "");
                    bigPath = projectPath + path;
                } else if (path.startsWith("/resources")||path.indexOf(".properties")>0||path.indexOf(".xml")>0) {
                    path = path.replaceFirst("/","/WEB-INF/classes/").replaceFirst("/resources/", "");
                    bigPath = projectPath + path;
                } else {
                    continue;
                }
                //如果为文件夹则跳过
                if (new File(bigPath).isDirectory()) {
                    continue;
                }
                String pathTo = exportPath + path;
                pathTo = pathTo
                        .substring(0, pathTo.lastIndexOf("/"));
                System.out.println(bigPath);
                FileOperateDemo.copyGeneralFile(bigPath, pathTo);

            }
            if (writer == null) {
                continue;
            }
            //提交记录
            String message = log.getMessage();
            //版本
            long version = log.getRevision();
            //写入文件
            try {
                String enter = System.getProperty("line.separator");
                writer.write(version + "/" + log.getAuthor() + "/" + message + enter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                // TODO 自动生成的 catch 块
                e.printStackTrace();
            }
        }
    }

    public List<SVNLogEntry> getSVNLogs() {
        return sVNLog.getSVNLogs();
    }

    public static void main(String[] args) throws FileNotFoundException {
        String baseUrl;
        String projectUrl;
        String username;
        String password;
        String projectPath;
        String projects;
        String currentProject;
        //InputStream in = ClassLoader.getSystemResourceAsStream("delt.properties");
        FileInputStream in = new FileInputStream(new File("delt.properties"));
        Properties p = new Properties();
        try {
            p.load(in);
        } catch (Exception e) {
            System.err.println("读取delt.properties失败");
            return;
        }
        currentProject = p.getProperty("currentProject");
        baseUrl = p.getProperty(currentProject + ".baseUrl");
        username = p.getProperty("username");
        password = p.getProperty("password");
        projects = p.getProperty("projects");
        year = Integer.valueOf(p.getProperty("year"));
        month = Integer.valueOf(p.getProperty("month"));
        day = Integer.valueOf(p.getProperty("day"));
        String[] projectsA = projects.split(",");
        if (currentProject == null) {
            currentProject = projectsA[0];//当前工程
        }
        projectUrl = p.getProperty(currentProject + ".projectUrl");
        projectPath = p.getProperty(currentProject + ".projectPath");

        DeltReleaseTool tool;
        try {
            tool = new DeltReleaseTool(baseUrl, projectUrl, username, password, projectPath);
        } catch (SVNException e) {
            System.err.println("连接svn失败");
            return;
        }

        System.out.println("连接svn成功！");
        System.out.println("支持项目:" + projects);
        System.out.println("当前路径:" + projectUrl);

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("输入版本号,z-区间版本,c-切换项目,s-查询历史,q-退出");
            try {
                String ver = sc.next();
                if (ver.equals("q")) {
                    System.out.println("退出成功！");
                    break;
                } else if (ver.equals("c")) {
                    System.out.println("输入项目编号");
                    String project = sc.next();
                    if (projects.indexOf(project) < 0) {
                        System.out.println("项目编号无效！");
                        continue;
                    }
                    projectUrl = p.getProperty(project + ".projectUrl");
                    projectPath = p.getProperty(project + ".projectPath");
                    try {
                        tool = new DeltReleaseTool(baseUrl, projectUrl, username, password, projectPath);
                        currentProject = project;
                        baseUrl = p.getProperty(currentProject + ".baseUrl");
                        p.setProperty("currentProject", currentProject);
                        tool = new DeltReleaseTool(baseUrl, projectUrl, username, password, projectPath);
                        System.out.println("切换成功！");
                        System.out.println("当前路径:" + projectUrl);
                    } catch (SVNException e) {
                        System.err.println("连接svn失败");
                        return;
                    }
                    continue;
                } else if (ver.equals("s")) {
                    //查询历史
                    List<SVNLogEntry> logs = tool.getSVNLogs();
                    for (SVNLogEntry log : logs) {
                        System.out.print(log.getRevision());
                        System.out.print("\t");
                        System.out.print(log.getAuthor());
                        System.out.print("\t");
                        System.out.print(log.getMessage());
                        System.out.print("\t");
                        System.out.print(log.getDate());
                        System.out.print("\n");
                    }
                    continue;
                } else if (ver.equals("z")) {
                    System.out.println("输入起始版本");
                    String beginVer = sc.next();
                    long begin = Long.parseLong(beginVer);
                    System.out.println("输入结束版本（n-表示最新）");
                    String endVer = sc.next();
                    if ("n".equals(endVer)) {
                        tool.creatPackWithLogs(begin, currentProject);
                    } else {
                        long end = Long.parseLong(endVer);
                        tool.creatPackWithLogs(begin, end, currentProject);
                    }
                    continue;
                }
                tool.creatPackWithLogs(Long.parseLong(ver), Long.parseLong(ver), currentProject);
            } catch (NumberFormatException ex) {
                System.err.println("版本号应为数字！");
                continue;
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("版本号输入有误！");
                continue;
            }
        }
    }
}
