package com.example.foxizz.navigation.util;

import android.content.Context;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;

/**
 * 语音合成模块
 */
public class MySpeech {

    private Context context;
    public MySpeech(Context context) {
        this.context = context;
    }

    public SpeechSynthesizer mSpeechSynthesizer;

    //初始化语音合成
    public void initSpeech() {
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context);
        mSpeechSynthesizer.setAppId("19031902");
        mSpeechSynthesizer.setApiKey("Y5HQLSAsKHcmmU2Yv2NGtST0nhBVj0iZ",
                "FA6eK7iTuT3btn37ruG1OC4mhPyiepd5");
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");//设置发声的人声音，在线生效
        mSpeechSynthesizer.initTts(TtsMode.ONLINE); // 初始化在线模式
    }

}
