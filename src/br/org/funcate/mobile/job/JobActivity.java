package br.org.funcate.mobile.job;

import android.app.Activity;
import android.os.Bundle;
import br.org.funcate.mobile.R;

public class JobActivity extends Activity {
	
	private JobController controller = new JobController();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_job);	
		
		
	}
}