package com.music.app.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.app.R;
import com.music.app.application.MusicApplication;
import com.music.app.storage.DBHelperManager;
import com.music.app.utils.Preferences;
import com.music.app.utils.ToastUtils;
import com.music.app.utils.binding.Bind;

public class LoginActivity extends BaseActivity implements OnClickListener {
	@Bind(R.id.login_user_text)
	private EditText userText;
	@Bind(R.id.login_password_text)
	private EditText passwordText;
	@Bind(R.id.login_user_avatar)
	private ImageView avatarIm;
	@Bind(R.id.login_mobile_text)
	private EditText mobileText;
	@Bind(R.id.login_btn)
	private TextView loginBtn;
	@Bind(R.id.login_register)
	private TextView registerBtn;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		userText.setText(Preferences.getUserName());
		passwordText.setText(Preferences.getPassword());
		performLoginBtn();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(userText.getText().toString().trim())) {
			if (!DBHelperManager.getInstance(LoginActivity.this).isExistByName(userText.getText().toString())) {
				avatarIm.setImageResource(R.drawable.icon_avatar);
			} else {
				if (!TextUtils.isEmpty(MusicApplication.user.avatar)) {
					Bitmap mCoverBitmap = BitmapFactory.decodeFile(MusicApplication.user.avatar);
					avatarIm.setImageBitmap(mCoverBitmap);						
				} else {
					avatarIm.setImageResource(R.drawable.icon_avatar);
				}
			}
		}
	}

	@Override
	protected void setListener() {
		loginBtn.setOnClickListener(this);
		registerBtn.setOnClickListener(this);
		userText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				if (DBHelperManager.getInstance(LoginActivity.this).isExistByName(arg0.toString())) {
					if (!TextUtils.isEmpty(MusicApplication.user.avatar)) {
						Bitmap mCoverBitmap = BitmapFactory.decodeFile(MusicApplication.user.avatar);
						avatarIm.setImageBitmap(mCoverBitmap);						
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if (!DBHelperManager.getInstance(LoginActivity.this).isExistByName(arg0.toString())) {
					avatarIm.setImageResource(R.drawable.icon_avatar);
				}
			}
		});
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.login_register:
			Intent intent = new Intent();
	        intent.setClass(this, RegisterActivity.class);
	        intent.putExtras(getIntent());
	        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        startActivity(intent);
			break;

		case R.id.login_btn:
			performLoginBtn();
			break;
		default:
			break;
		}
	}

	private void performLoginBtn() {
		String userName = userText.getText().toString().trim();
		if (TextUtils.isEmpty(userName)) {
            ToastUtils.show("昵称不能为空");
            return;
        }

		String password = passwordText.getText().toString().trim();
		if (TextUtils.isEmpty(password)) {
            ToastUtils.show("密码不能为空");
            return;
        }
		boolean isRegister = DBHelperManager.getInstance(LoginActivity.this).isExist(userName, password);
		if (isRegister) {
            Preferences.saveUserName(userName);
            Preferences.savePassword(password);
            startMusicActivity();
        } else {
            ToastUtils.show("账号密码不正确");
        }
	}

	private void startMusicActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MusicActivity.class);
        intent.putExtras(getIntent());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
	
}
