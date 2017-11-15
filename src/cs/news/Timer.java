package cs.news;

import static cs.news.util.WebUtils.AvailableInternetConnection;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cs.news.swing.TrayIcon;
import cs.news.util.Options;
import cs.news.util.WebDataParser;
import cs.newsdatamanagers.AnnounceManager;
import cs.newsdatamanagers.TeacherManager;

public class Timer extends java.util.Timer {
	private static final long ANNOUNCES_READ_TASK_INITIAL = 1; //1 Second
	private static Timer instance;

	public Timer() {
		schedule(new ParseWebDataTask(TeacherManager.getInstance()), 500);
		schedule(new ParseWebDataTask(AnnounceManager.getInstance()), ANNOUNCES_READ_TASK_INITIAL * 1000,
				TimeUnit.MINUTES.toMillis(Options.SYNC_ANNOUNCES_TIME.toInt()));

		long remindTime = TimeUnit.MINUTES.toMillis(Options.REMIND_ANNOUNCES_TIME.toInt());
		if (remindTime > 0)//If 0, reminder is disabled.
			schedule(new UnreadAnnouncesRemindTask(), remindTime / 2, remindTime); //Initial time is the half.

		instance = this;
	}

	public static void restart() {
		instance.cancel();
		new Timer();
	}

	private final class ParseWebDataTask extends TimerTask {
		private WebDataParser manager;

		public ParseWebDataTask(WebDataParser manager) {
			this.manager = manager;
		}

		@Override
		public void run() {
			manager.parseDataFromWeb();
		}
	}

	private final class UnreadAnnouncesRemindTask extends TimerTask {

		@Override
		public void run() {
			int unreadAnnounces = AnnounceManager.getInstance().getNumOfUnreadAnnounces();
			if (!AvailableInternetConnection()) {
				TrayIcon.getInstance().reBuild();
				return; // If no connection, don't remind anything.
			}
			if (unreadAnnounces <= 0)
				return;
			else if (unreadAnnounces == 1)
				TrayIcon.getInstance().showMessage("����������", "������� 1 ��� ����������.", true);
			else
				TrayIcon.getInstance().showMessage("����������", "�������� " + unreadAnnounces + " ���� ������������.",
						true);
		}
	}
}
