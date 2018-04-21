package svn;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SVNLog {
	
	private String url;
	private String username;
	private String password;
	private SVNRepository repository;
	private static final int YEAR=DeltReleaseTool.year;
	private static final int MONTH=DeltReleaseTool.month;
	private static final int DAY=DeltReleaseTool.day;

	public  SVNLog(String url, String username, String password) throws SVNException {
		this.url = url;
		this.username = username;
		this.password = password;
		// 版本库初始化
		DAVRepositoryFactory.setup();
		repository = DAVRepositoryFactory.create(SVNURL.parseURIEncoded(url));
		ISVNAuthenticationManager authManager = SVNWCUtil
				.createDefaultAuthenticationManager(username, password);
		repository.setAuthenticationManager(authManager);
	}
	public List<SVNLogEntry> getSVNLogs(long startRevision, long endRevision) {
		try {
			// 存放结果
			List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
			repository.log(new String[] { "/" }, logEntries, startRevision,
					endRevision, true, true);
			return logEntries;
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return null;
		}
	}

	public List<SVNLogEntry> getSVNLogs(long startRevision) {
		long lastRevision = 0;
		try {
			lastRevision = repository.getLatestRevision();
			return getSVNLogs(startRevision,lastRevision);
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		}
	}
	//获取今日提交记录
	public List<SVNLogEntry> getSVNLogs() {
		long lastRevision = 0;
		long startRevision=0;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); 		
		try {
			//Date today = format.parse(format.format(new Date(new Date().getTime()-24*60*60*1000*TIME)));
			Calendar calendar=Calendar.getInstance();
			calendar.set(YEAR,MONTH-1,DAY);
			Date today=calendar.getTime();
			startRevision = repository.getDatedRevision(today);
			//lastRevision = repository.getLatestRevision();
			return getSVNLogs(startRevision,-1);
		} catch (SVNException e) {
			e.printStackTrace();
			
			return null;
		}
	}	
	public static void main(String[] args) throws SVNException {
		String url = "https://123.56.193.172:8443/svn/PBMS_1.0/PBMS";
		
		String username = "jiwei";
		String password = "jiwei";
		List<SVNLogEntry> logs = new SVNLog(url, username, password)
				.getSVNLogs();
		for (SVNLogEntry log : logs) {
			System.out.println(log.getDate());
		}
	}
}
