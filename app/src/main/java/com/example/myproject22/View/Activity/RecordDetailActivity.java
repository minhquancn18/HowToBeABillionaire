package com.example.myproject22.View.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myproject22.Model.DetailItem;
import com.example.myproject22.Presenter.Interface.RecordDetailInterface;
import com.example.myproject22.Presenter.Presenter.RecordDetailPresenter;
import com.example.myproject22.R;
import com.example.myproject22.Util.FormatImage;
import com.example.myproject22.Util.Formatter;
import com.example.myproject22.View.Service.Network_receiver;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.myproject22.Model.ConnectionClass.urlAudio;
import static com.example.myproject22.Model.ConnectionClass.urlImage;
import static com.example.myproject22.Model.ConnectionClass.urlImageCategory;
import static com.example.myproject22.Model.ConnectionClass.urlString;

public class RecordDetailActivity extends AppCompatActivity implements RecordDetailInterface {

    //region Khởi tạo component

    //region Presenter
    private RecordDetailPresenter presenter;
    private int id_detail;
    private int isCategory;
    private DetailItem item;
    //endregion

    //region Component
    private TextView tvMoney;
    private TextView tvDescription;
    private TextView tvNameCategory;
    private TextView tvTime;
    private TextView tvStart;
    private TextView tvEnd;
    private ImageView ivImage;
    private SeekBar seekBar;
    /*  private ImageButton btnDescrease;
      private ImageButton btnIncrease;*/


    private ImageButton btnPause;
    private MaterialCardView lb_audio_tray;
    //private BlurLayout blurLayout_2;
    private LinearLayout ln_record_detail;
    private ProgressBar progressBar;
    private CoordinatorLayout mSnackbarLayout;
    //endregion

    //region Media
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Boolean flag = false;
    private Boolean isLoading = true;
    private Boolean isaudio = false;
    //endregion

    //region Broadcast
    private Network_receiver network_receiver;
    //endregion

    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        setContentView(R.layout.activity_record_detail);

        //region Broadcast
        network_receiver = new Network_receiver();
        //endregion

        //region Khởi tạo presenter và giá trị ban đầu
        presenter = new RecordDetailPresenter(this);
        presenter.setInit();
        presenter.GetBundleData();
        //endregion

        //region Xử lí media

        //region Xử lí seekbar
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return presenter.getSeekbarTouch(v);
            }
        });
        //endregion

        //region Xử lí media player
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                seekBar.setSecondaryProgress(percent);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                presenter.setCompleteMedia();
            }
        });
        //endregion

        //region Xử lí các button
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.getPauseClick();
            }
        });

       /* btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setNext5Second();
            }
        });

        btnDescrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setBack5Second();
            }
        });*/
        //endregion

        //endregion

    }

    //region Xử lí override Activity
    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(network_receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(network_receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FormatImage.StopLoadImage(getApplicationContext());
    }

    //endregion

    //region Xử lí các hàm override từ activity
    @Override
    protected void onResume() {
        super.onResume();
        /*presenter.loadDataFromServer();*/
    }

    //Xử lí quay lại
    @Override
    public void onBackPressed() {
        if (isLoading == false) {
            super.onBackPressed();
            presenter.setReleaseMedia();
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }

    //endregion

    //region Set init, get bundle
    @Override
    public void SetInit() {
        tvMoney = findViewById(R.id.textView6);
        tvDescription = findViewById(R.id.tvDescriptionRecord);
        tvNameCategory = findViewById(R.id.tvNameCategory);
        ivImage = findViewById(R.id.imageView6);
        tvTime = findViewById(R.id.textView7);
        tvStart = findViewById(R.id.tvStart);
        tvEnd = findViewById(R.id.tvEnd);
        seekBar = findViewById(R.id.seekBar2);
      /*  btnDescrease = findViewById(R.id.imageButton5);
        btnIncrease = findViewById(R.id.imageButton3);*/
        btnPause = findViewById(R.id.imageButton4);


        ln_record_detail = findViewById(R.id.ln_record_detail);
        lb_audio_tray = findViewById(R.id.lb_audio_tray);

        btnPause.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.pbRecord);
        progressBar.bringToFront();
        mediaPlayer = new MediaPlayer();
        seekBar.setMax(100);
        mSnackbarLayout = findViewById(R.id.cl_snackbar);



        progressBar.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.INVISIBLE);
        ln_record_detail.setVisibility(View.INVISIBLE);
        //blurLayout_2.setVisibility(View.INVISIBLE);
        lb_audio_tray.setVisibility(View.INVISIBLE);
    }

    @Override
    public void GetBundleData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        id_detail = bundle.getInt("ID_DETAIL");
        isCategory = bundle.getInt("IS_CATEGORY");

        Double money = bundle.getDouble("MONEY");
        String description = bundle.getString("DESCRIPTION");
        String sdate = bundle.getString("DATE");
        String name = bundle.getString("NAME");
        String image = bundle.getString("IMAGE");
        String imagecategory = bundle.getString("IMAGECATEGORY");
        String audio = bundle.getString("AUDIO");
        isaudio = bundle.getBoolean("ISAUDIO");

        SimpleDateFormat curFormater = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date dateObj = null;
        try {
            dateObj = curFormater.parse(sdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        item = new DetailItem(money, description, sdate, name, image, imagecategory, audio, dateObj);


        // TAM
       /* if(isaudio){
            LoadDataToLayout();
            presenter.prepareMedia(item.get_AUDIO());
        }
        else{
            LoadDataToLayoutNoAudio();
            isLoading = false;
        }*/

        LoadDataToLayoutNoAudio();


        new Handler().post(() -> {
            if(isaudio){
                LoadDataToLayout();
                presenter.prepareMedia(item.get_AUDIO());
                isLoading = false;
            }
        });


    }
    //endregion

    //region Load data from server to layout
    @Override
    public void LoadDataToLayout() {
        lb_audio_tray.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.VISIBLE);
    }

    @Override
    public void LoadDataToLayoutNoAudio() {
        String sMoney = Formatter.getCurrencyStr(String.valueOf(item.get_MONEY())) + " VND";
        String isPlus = isCategory == 1 ? "+ " : "- ";
        tvMoney.setText(isPlus + " " + sMoney);
        tvDescription.setText(item.get_DESCRIPTION());
        tvNameCategory.setText(item.get_NAME());

        String[] splitdate = item.get_DATE().split(" ");
        String[] splitday = splitdate[0].split("-");
        String date_string = splitdate[1] + " ngày " + splitday[2] + "-" + splitday[1] + "-" + splitday[0];
        tvTime.setText(date_string);

        if (!item.get_IMAGE().equals("NULL")) {
            FormatImage.LoadImageIntoView(ivImage, RecordDetailActivity.this, item.get_IMAGE());
        }

        progressBar.setVisibility(View.GONE);
        ln_record_detail.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);
    }

    @Override
    public void LoadFromServer() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isCategory == 1) {
                    presenter.fetchIncomeDatFromServer();
                } else {
                    presenter.fetchOutcomeDataFromServer();
                }
            }
        }, 500);
    }
    //endregion

    //region Fetch income, outcome from server
    @Override
    public void FetchIncomeDataFromServer() {
        StringRequest request = new StringRequest(Request.Method.POST,
                urlString + "getIncomeDetailByID.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i("RESPONSERECORĐETAIL", response);

                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");

                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    if (success.equals("1")) {
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);

                                String smoney = object.getString("MONEY");
                                String description = object.getString("DESCRIPTION");
                                String sdate = object.getString("DATE");
                                String name = object.getString("NAME");
                                String image = object.getString("IMAGE");
                                String imagecategory = object.getString("IMAGECATEGORY");
                                String audio = object.getString("AUDIO");

                                SimpleDateFormat curFormater = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                Date dateObj = curFormater.parse(sdate);


                                Double money = Double.parseDouble(smoney);

                                String url_image = image.equals("null") ? "NULL" : urlImage + image;
                                String url_image_category = imagecategory.equals("null") ? "NULL" : urlImageCategory + imagecategory;
                                String url_audio = audio.equals("null") ? "NULL" : urlAudio + audio;

                                item = new DetailItem(money, description, sdate, name, url_image, url_image_category, url_audio, dateObj);



                            }
                        }
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

                if (item.get_AUDIO().equals("NULL")) {
                    presenter.loadDataToLayoutNoAudio();
                    isLoading = false;
                } else {
                    presenter.loadDataToLayout();
                    presenter.prepareMedia(item.get_AUDIO());

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(mSnackbarLayout, "Lỗi kết nối internet", Snackbar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                snackbar.show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_incomedetail", String.valueOf(id_detail));
                return params;
            }
        };
        if (getApplicationContext() != null) {
            RequestQueue requestQueue = Volley.newRequestQueue(RecordDetailActivity.this);
            requestQueue.add(request);
        }
    }

    @Override
    public void FetchOutcomeDataFromServer() {
        StringRequest request = new StringRequest(Request.Method.POST,
                urlString + "getOutcomeDetailByID.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i("RESPONSERECORDDETAIL", response);

                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");

                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    if (success.equals("1")) {
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);

                                String smoney = object.getString("MONEY");
                                String description = object.getString("DESCRIPTION");
                                String sdate = object.getString("DATE");
                                String name = object.getString("NAME");
                                String imagecategory = object.getString("IMAGECATEGORY");
                                String image = object.getString("IMAGE");
                                String audio = object.getString("AUDIO");

                                SimpleDateFormat curFormater = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                                Date dateObj = curFormater.parse(sdate);


                                Double money = Double.parseDouble(smoney);

                                String url_image = image.equals("null") ? "NULL" : urlImage + image;
                                String url_image_category = imagecategory.equals("null") ? "NULL" : urlImageCategory + imagecategory;
                                String url_audio = audio.equals("null") ? "NULL" : urlAudio + audio;

                                item = new DetailItem(money, description, sdate, name, url_image, url_image_category, url_audio, dateObj);


                            }
                        }
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }


                if (item.get_AUDIO().equals("NULL")) {
                    presenter.loadDataToLayoutNoAudio();
                } else {
                    presenter.loadDataToLayout();
                    presenter.prepareMedia(item.get_AUDIO());
                    isLoading = false;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar.make(mSnackbarLayout, "Lỗi kết nối internet", Snackbar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                snackbar.show();
                /*Toast.makeText(RecordDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();*/
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id_outcomedetail", String.valueOf(id_detail));
                return params;
            }
        };
        if (getApplicationContext() != null) {
            RequestQueue requestQueue = Volley.newRequestQueue(RecordDetailActivity.this);
            requestQueue.add(request);
        }
    }
    //endregion

    //region Xử lí Media

    //Chuẩn bị media
    @Override
    public void PrepareMedia(String url) {
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            tvEnd.setText(presenter.getTimeMedia(mediaPlayer.getDuration()));
            seekBar.setProgress(0);
            isLoading = false;
            progressBar.setVisibility(View.GONE);
        } catch (IOException e) {
            Snackbar snackbar = Snackbar.make(mSnackbarLayout, "Lỗi không tìm thấy nguồn thu âm", Snackbar.LENGTH_SHORT);
            snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
            snackbar.show();
            isLoading = false;
            progressBar.setVisibility(View.GONE);
            /*Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();*/
        }
    }

    //Lấy thời gian của media
    @Override
    public String GetTimeMedia(long millionsecond) {
        String timeString = "";
        String secondString;

        int hour = (int) (millionsecond / (1000 * 60 * 60));
        int minute = (int) (millionsecond % (1000 * 60 * 60)) / (1000 * 60);
        int second = (int) ((millionsecond % (1000 * 60 * 60) % (1000 * 60)) / 1000);

        if (hour > 0) {
            timeString = hour + ":";
        }

        if (second < 10) {
            secondString = "0" + second;
        } else {
            secondString = "" + second;
        }

        timeString = timeString + minute + ":" + secondString;

        return timeString;
    }

    //1 runnable để cập nhật thanh seekbar khi media đang phát
    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (flag == true) {
                presenter.updateSeekbar();
                long currentduration = mediaPlayer.getCurrentPosition();
                tvStart.setText(presenter.getTimeMedia(currentduration));
            }
        }
    };

    //Cập nhật seekbar khi media đang phát
    @Override
    public void UpdateSeekbar() {
        if (flag == true) {
            if (mediaPlayer.isPlaying()) {
                seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
                handler.postDelayed(updater, 1000);
            }
        }
    }

    //Xử lí button pause
    @SuppressLint("ResourceAsColor")
    @Override
    public void GetPauseClick() {
        if (mediaPlayer.isPlaying()) {
            handler.removeCallbacks(updater);
            mediaPlayer.pause();
            btnPause.setBackgroundColor(R.color.backgroundTransForText);
            btnPause.setImageResource(R.drawable.icon_play_red);

        } else {
            flag = true;
            mediaPlayer.start();
            btnPause.setBackgroundColor(android.R.color.transparent);
            btnPause.setImageResource(R.drawable.icon_pause_red);
            presenter.updateSeekbar();
        }
    }

    //Xử lí seekbar touch
    @Override
    public boolean GetSeekbarTouch(View view) {
        seekBar = (SeekBar) view;
        int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
        mediaPlayer.seekTo(playPosition);
        tvStart.setText(presenter.getTimeMedia(mediaPlayer.getCurrentPosition()));
        return false;
    }

    //Xử lí layout khi media complete
    @Override
    public void SetCompleteMedia() {
        flag = false;
        btnPause.setImageResource(R.drawable.icon_play_red);
        tvStart.setText("0:00");
        mediaPlayer.reset();
        presenter.prepareMedia(item.get_AUDIO());
    }

    //Xử lí button next 5s
    @Override
    public void SetNext5Second() {
        if (flag == true) {
            int ADD_SECOND = 5000;
            int curPosition = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();

            if (curPosition + ADD_SECOND < duration) {
                mediaPlayer.seekTo(curPosition + ADD_SECOND);
                presenter.updateSeekbar();
            }
        }

        else{


        }




    }

    //Xử lý button back 5s
    @Override
    public void SetBack5Second() {
        if (flag == true) {
            int BACK_SECOND = 5000;

            int curPosition = mediaPlayer.getCurrentPosition();
            if (curPosition - BACK_SECOND > 0) {
                mediaPlayer.seekTo(curPosition - BACK_SECOND);
                presenter.updateSeekbar();
            }
        }
    }

    //Giải phóng media khi quay lại
    @Override
    public void SetRealseMedia() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        flag = false;
    }

    //endregion

}