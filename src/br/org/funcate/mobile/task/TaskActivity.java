package br.org.funcate.mobile.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import br.org.funcate.mobile.R;

/** 
 * Activity for loading layout resources 
 * 
 * This activity is used to display the Task screen, that. 
 * 
 * @author Paulo Luan 
 * @version 1.0 
 * @since 1.0
 */  
public class TaskActivity extends Activity {
	
	private TaskController controller = new TaskController();
	private ProgressDialog dialog;
	private TaskActivity self = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_task);	
	
		Button btn_get_tasks = (Button) findViewById(R.id.btn_get_tasks);
		Button btn_send_tasks = (Button) findViewById(R.id.btn_send_tasks);
		Button btn_clear_tasks = (Button) findViewById(R.id.btn_clear_tasks);
		
		btn_get_tasks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();
				
				try {
					controller.getAllTasks();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				self.hideLoadMask();
			}
		});
		
		btn_send_tasks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();
				
				try {
					controller.sendTasks();	
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				self.hideLoadMask();
			}
		});
		
		btn_clear_tasks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();

				try {
					controller.clearAlltasks();	
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				self.hideLoadMask();
			}
		});
	}
	
	public void showLoadingMask(){
		dialog = ProgressDialog.show(TaskActivity.this, "", "Carregando, aguarde...", true);
	}
	
	public void hideLoadMask(){
		dialog.hide();
	}
}