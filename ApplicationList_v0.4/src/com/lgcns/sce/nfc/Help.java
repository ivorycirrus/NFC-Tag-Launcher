package com.lgcns.sce.nfc;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class Help extends Activity {
    
    private volatile static boolean mShowingHelpDetail = false;

    private static LinearLayout mHelpLayout;
    private static LayoutInflater mLayoutInflater; 
    private static ViewFlipper mViewFlipper;
    
    private static int m_nPreTouchPosX = 0;
    
    private enum HelpMenu {
        Default , WhatIsTagLauncher , RegisterAndWrite , Settings , Unregister
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        
        mHelpLayout = (LinearLayout) findViewById(R.id.layout_help);
        mLayoutInflater = this.getLayoutInflater();
        inflateView(HelpMenu.Default);
    }
    
    private void MoveNextView()
    {
        if(mViewFlipper.getDisplayedChild()==(mViewFlipper.getChildCount()-1)) return;
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_right));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_left));
        mViewFlipper.showNext();
    }
    
    private void MovewPreviousView()
    {
        if(mViewFlipper.getDisplayedChild()==0) return;
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_left));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_right));
        mViewFlipper.showPrevious();        
    }
    
    private View.OnTouchListener mTouchListener = new View.OnTouchListener()
    {
        public boolean onTouch(View v, MotionEvent event) 
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                m_nPreTouchPosX = (int)event.getX();
            }
            
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                int nTouchPosX = (int)event.getX();
                
                if (nTouchPosX < m_nPreTouchPosX)
                {
                    MoveNextView();
                }
                else if (nTouchPosX > m_nPreTouchPosX)
                {
                    MovewPreviousView();
                }
                
                m_nPreTouchPosX = nTouchPosX;
            }
            
            return true;
        }
    };
    
    public void onBackPressed() {
        if(mShowingHelpDetail)
        {
            inflateView(HelpMenu.Default);
        }
        else
        {
            finish();
        }
    };
    
    private void unregisterOnClickListener()
    {
        findViewById(R.id.btn_help_close).setOnClickListener(null);
        findViewById(R.id.btn_help_register_and_write_tag).setOnClickListener(null);
        findViewById(R.id.btn_help_settings).setOnClickListener(null);
        findViewById(R.id.btn_help_unregister).setOnClickListener(null);
        findViewById(R.id.btn_help_what_is_taglauncher).setOnClickListener(null);        
    }
    
    private void registerOnClickListener()
    {
        findViewById(R.id.btn_help_close).setOnClickListener(new OnClickListener() {            
            public void onClick(View v) {
                finish();                
            }
        });
        findViewById(R.id.btn_help_register_and_write_tag).setOnClickListener(new OnClickListener() {            
            public void onClick(View v) {
                unregisterOnClickListener();
                inflateView(HelpMenu.RegisterAndWrite);
            }
        }); 
        findViewById(R.id.btn_help_settings).setOnClickListener(new OnClickListener() {            
            public void onClick(View v) {
                unregisterOnClickListener();
                inflateView(HelpMenu.Settings);
            }
        }); 
        findViewById(R.id.btn_help_unregister).setOnClickListener(new OnClickListener() {            
            public void onClick(View v) {
                unregisterOnClickListener();
                inflateView(HelpMenu.Unregister);
            }
        }); 
        findViewById(R.id.btn_help_what_is_taglauncher).setOnClickListener(new OnClickListener() {            
            public void onClick(View v) {
                unregisterOnClickListener();
                inflateView(HelpMenu.WhatIsTagLauncher);
            }
        });       
    }
    
    private void inflateView(HelpMenu item)
    {
        mHelpLayout.removeAllViews();        
        switch(item)
        {
            case RegisterAndWrite:
                mLayoutInflater.inflate(R.xml.help_register_and_write_to_tag, mHelpLayout);
                mViewFlipper = (ViewFlipper) findViewById(R.id.flipper_help_register_and_write);
                mViewFlipper.setOnTouchListener(mTouchListener);
                mShowingHelpDetail = true;
                break;
            case Settings:
                mLayoutInflater.inflate(R.xml.help_settings, mHelpLayout);
                mViewFlipper = (ViewFlipper) findViewById(R.id.flipper_help_settings);
                mViewFlipper.setOnTouchListener(mTouchListener);
                mShowingHelpDetail = true;
                break;
            case Unregister:
                mLayoutInflater.inflate(R.xml.help_unregister, mHelpLayout);
                mViewFlipper = (ViewFlipper) findViewById(R.id.flipper_help_unregister);
                mViewFlipper.setOnTouchListener(mTouchListener);
                mShowingHelpDetail = true;
                break;
            case WhatIsTagLauncher:
                mLayoutInflater.inflate(R.xml.help_what_is_taglauncher, mHelpLayout);
                mViewFlipper = (ViewFlipper) findViewById(R.id.flipper_help_what_ia_taglaunch);
                mViewFlipper.setOnTouchListener(mTouchListener);
                mShowingHelpDetail = true;
                break;
            case Default:
            default:                
                if(mViewFlipper != null)
                {
                    mViewFlipper.setOnTouchListener(null);
                    mViewFlipper = null;
                }
                mLayoutInflater.inflate(R.xml.help_main, mHelpLayout);
                registerOnClickListener();
                mShowingHelpDetail = false;
        }
    }
}
