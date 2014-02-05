package br.inpe.mobile.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.http.ResponseEntity;

import android.os.AsyncTask;
import android.widget.Toast;
import br.inpe.mobile.Utility;
import br.inpe.mobile.exception.ExceptionHandler;

/**
 * Async class implementation to get tasks from server.
 * 
 * @author Paulo Luan
 * 
 * @param String
 *            ... urls URL's that will called.
 */
public class DownloadTasks extends AsyncTask<String, String, String> {

	private String userHash = "";

	private TaskActivity taskActivity;

	public DownloadTasks(String userHash, TaskActivity taskActivity) {
		this.userHash = userHash;
		this.taskActivity = taskActivity;
	}

	@Override
	protected String doInBackground(String... urls) {
		String message = null;

		for (String url : urls) {
			try {
				publishProgress("Fazendo Download das tarefas...");
				ResponseEntity<Task[]> response = taskActivity.restTemplate
						.getForEntity(url, Task[].class, userHash);
				Task[] responseTasks = response.getBody();
				ArrayList<Task> tasks = new ArrayList<Task>(
						Arrays.asList(responseTasks));

				publishProgress("Salvando tarefas no banco de dados local...",
						"0", "" + tasks.size()); // set Max Length of progress
													// dialog

				int progress = 0;

				for (Task task : tasks) {
					TaskDao.saveTask(task);
					progress++;
					publishProgress(
							"Salvando tarefas no banco de dados local...", ""
									+ progress);
				}
			} catch (Exception e) {
				message = "Ocorreu um erro ao fazer o download das tarefas.";
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				ExceptionHandler.saveLogFile(errors.toString());
			}
		}

		return message;
	}

	@Override
	protected void onPreExecute() {
		taskActivity.showLoadingMask("Carregando, aguarde...");
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		taskActivity.onProgressUpdate(progress);
	}

	@Override
	protected void onPostExecute(String message) {
		taskActivity.updateCountLabels();
		taskActivity.hideLoadingMask();

		if (message != null) {
			Utility.showToast("Ocorreu um erro ao baixar as atividades.",
					Toast.LENGTH_LONG, taskActivity);
		}
	}

}
