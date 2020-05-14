 package jp.co.miosys.aiworldview.fragment;


 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Point;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;

 import androidx.annotation.NonNull;
 import androidx.annotation.Nullable;
 import androidx.core.content.ContextCompat;
 import androidx.fragment.app.DialogFragment;
 import androidx.fragment.app.Fragment;

 import jp.co.miosys.aiworldview.R;
 import jp.co.miosys.aiworldview.data_post_response.Memo;

 import com.google.android.exoplayer2.C;
 import com.google.android.exoplayer2.DefaultLoadControl;
 import com.google.android.exoplayer2.DefaultRenderersFactory;
 import com.google.android.exoplayer2.ExoPlayerFactory;
 import com.google.android.exoplayer2.LoadControl;
 import com.google.android.exoplayer2.SimpleExoPlayer;
 import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
 import com.google.android.exoplayer2.source.ExtractorMediaSource;
 import com.google.android.exoplayer2.source.MediaSource;
 import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
 import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
 import com.google.android.exoplayer2.trackselection.TrackSelection;
 import com.google.android.exoplayer2.trackselection.TrackSelector;
 import com.google.android.exoplayer2.ui.PlaybackControlView;
 import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
 import com.google.android.exoplayer2.upstream.BandwidthMeter;
 import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
 import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
 import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
 import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
 import com.google.android.exoplayer2.util.Util;

 /**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends DialogFragment {

     private final String STATE_RESUME_WINDOW = "resumeWindow";
     private final String STATE_RESUME_POSITION = "resumePosition";
     private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

     private SimpleExoPlayerView mExoPlayerView;
     private MediaSource mVideoSource;
     private boolean mExoPlayerFullscreen = false;
     private FrameLayout mFullScreenButton;
     private ImageView mFullScreenIcon;
     private Dialog mFullScreenDialog;

     private int mResumeWindow;
     private long mResumePosition;

     private Context mContext;
     private View rootView;

     private TextView nameCategory, nameWorker, date, contentDescription;
     private Memo memo;

     public static VideoFragment newInstance(Memo memo) {
        VideoFragment videoFragment = new VideoFragment();
         Bundle args = new Bundle();
         args.putSerializable("memo", memo);
         videoFragment.setArguments(args);
         return videoFragment;
     }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.description_point, container, false);
        Bundle getBundle = getArguments();
        if (getBundle != null)
            this.memo = (Memo) getBundle.getSerializable("memo");
        initView(view);
        attachDataView(memo);

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
        return view;
    }

     @Override
     public void onCreate(@Nullable Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setStyle(DialogFragment.STYLE_NORMAL, R.style.background_dialog);
     }

     @Override
     public void onSaveInstanceState(@NonNull Bundle outState) {

         outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
         outState.putLong(STATE_RESUME_POSITION, mResumePosition);
         outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);

         super.onSaveInstanceState(outState);
     }

     private void initView(View view) {

         rootView = view;
         mContext = getContext();
         nameCategory = view.findViewById(R.id.text_name_category);
         nameWorker = view.findViewById(R.id.text_name_worker);
         date = view.findViewById(R.id.text_date);
         contentDescription = view.findViewById(R.id.text_description_category);

     }

     private void attachDataView(Memo memo) {
         contentDescription.setText(memo.getContent());
         date.setText(memo.getCollectionTime());
         nameCategory.setText(memo.getCategoryName());
         nameWorker.setText(memo.getUserName());
     }

     private void initFullscreenDialog() {

         mFullScreenDialog = new Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
             public void onBackPressed() {
                 if (mExoPlayerFullscreen)
                     closeFullscreenDialog();
                 super.onBackPressed();
             }
         };
     }


     private void openFullscreenDialog() {

         ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
         mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
         mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_fullscreen_skrink));
         mExoPlayerFullscreen = true;
         mFullScreenDialog.show();
     }


     private void closeFullscreenDialog() {

         ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
         ((FrameLayout) rootView.findViewById(R.id.main_media_frame)).addView(mExoPlayerView);
         mExoPlayerFullscreen = false;
         mFullScreenDialog.dismiss();
         mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_fullscreen_expand));
     }


     private void initFullscreenButton() {

         PlaybackControlView controlView = mExoPlayerView.findViewById(R.id.exo_controller);
         mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
         mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
         mFullScreenButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (!mExoPlayerFullscreen)
                     openFullscreenDialog();
                 else
                     closeFullscreenDialog();
             }
         });
     }


     private void initExoPlayer() {

         BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
         TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
         TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
         LoadControl loadControl = new DefaultLoadControl();
         SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(mContext), trackSelector, loadControl);
         mExoPlayerView.setPlayer(player);

         boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;

         if (haveResumePosition) {
             mExoPlayerView.getPlayer().seekTo(mResumeWindow, mResumePosition);
         }

//        mExoPlayerView.getPlayer().prepare(mVideoSource);
         player.prepare(mVideoSource);
         mExoPlayerView.getPlayer().setPlayWhenReady(true);
     }

//      link video test : http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8
//      https://www.radiantmediaplayer.com/media/bbb-360p.mp4
     @Override
     public void onResume() {

         super.onResume();

         super.onResume();
         Window window = getDialog().getWindow();
         Point size = new Point();
         Display display = window.getWindowManager().getDefaultDisplay();
         display.getSize(size);
         window.setLayout((int)(size.x * 0.9), WindowManager.LayoutParams.WRAP_CONTENT);
         window.setGravity(Gravity.CENTER);

         if (mExoPlayerView == null) {

             mExoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.exoplayer);
             initFullscreenDialog();
             initFullscreenButton();

             FrameLayout frameLayoutVideo = rootView.findViewById(R.id.main_media_frame);

             String streamUrl;
             if (memo != null) {
                 if (memo.getLinkVideo() != null) {
                     if (memo.getLinkVideo().equals("")) {
                         frameLayoutVideo.setVisibility(View.GONE);
                         mExoPlayerView.setVisibility(View.GONE);
                         return;
                     } else {
                         streamUrl = memo.getLinkVideo();
                         frameLayoutVideo.setVisibility(View.VISIBLE);
                         mExoPlayerView.setVisibility(View.VISIBLE);
                     }
                 }
                 else {
                     frameLayoutVideo.setVisibility(View.GONE);
                     mExoPlayerView.setVisibility(View.GONE);
                     return;
                 }
             }
             else return;
             String userAgent = Util.getUserAgent(mContext, getActivity().getApplicationContext().getApplicationInfo().packageName);
             DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent, null, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
             DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext, null, httpDataSourceFactory);
             Uri daUri = Uri.parse(streamUrl);

             // play with mp4..
             mVideoSource = new ExtractorMediaSource(daUri, dataSourceFactory, new DefaultExtractorsFactory(), null, null);
             // play with hls
//             mVideoSource = new HlsMediaSource(daUri, dataSourceFactory, 1, null, null);
         }

         initExoPlayer();

         if (mExoPlayerFullscreen) {
             ((ViewGroup) mExoPlayerView.getParent()).removeView(mExoPlayerView);
             mFullScreenDialog.addContentView(mExoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
             mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_fullscreen_skrink));
             mFullScreenDialog.show();
         }
     }


     @Override
     public void onPause() {

         super.onPause();

         if (mExoPlayerView != null && mExoPlayerView.getPlayer() != null) {
             mResumeWindow = mExoPlayerView.getPlayer().getCurrentWindowIndex();
             mResumePosition = Math.max(0, mExoPlayerView.getPlayer().getContentPosition());

             mExoPlayerView.getPlayer().release();
         }

         if (mFullScreenDialog != null)
             mFullScreenDialog.dismiss();
     }

 }
