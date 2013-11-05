package br.org.funcate.mobile.job;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import br.org.funcate.mobile.R;

/** 
 * Activity for loading layout resources 
 * 
 * This activity is used to display the Job screen, that. 
 * 
 * @author Paulo Luan 
 * @version 1.0 
 * @since 1.0
 */  
public class JobActivity extends Activity {
	
	private JobController controller = new JobController();
	private ProgressDialog dialog;
	private JobActivity self = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_job);	
	
		Button btn_get_jobs = (Button) findViewById(R.id.btn_get_jobs);
		Button btn_send_jobs = (Button) findViewById(R.id.btn_send_jobs);
		Button btn_clear_jobs = (Button) findViewById(R.id.btn_clear_jobs);
		
		btn_get_jobs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();
				
				try {
					controller.getAllJobs();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				self.hideLoadMask();
			}
		});
		
		btn_send_jobs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();
				
				try {
					controller.sendJobs();	
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				self.hideLoadMask();
			}
		});
		
		btn_clear_jobs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				self.showLoadingMask();

				try {
					controller.clearAlljobs();	
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				self.hideLoadMask();
			}
		});
	}
	
	public void showLoadingMask(){
		dialog = ProgressDialog.show(JobActivity.this, "", "Carregando, aguarde...", true);
	}
	
	public void hideLoadMask(){
		dialog.hide();
	}
}