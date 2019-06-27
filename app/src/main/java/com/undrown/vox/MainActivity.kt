package com.undrown.vox

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val player = VoxTrack(0f)
        Thread(player).start()
        mainPanel.setOnTouchListener {_, event ->
            Toast.makeText(applicationContext,"${event.x}", Toast.LENGTH_SHORT).show()
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    player.setFreqHz(event.x)
                    player.generateTone()
                }
            }
            false
        }

    }

    class VoxTrack(private var freqHz: Float):Runnable{

        var amplitude:Float = 1.0f
        val count = 800
        val countFull = 1000
        val track = generateTrack(count)

        private fun generateTrack(count : Int):AudioTrack{
            return AudioTrack(
                AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                countFull * (Short.SIZE_BYTES), AudioTrack.MODE_STREAM
            )
        }

        public fun generateTone() {
            //val count = (44100.0 * 2.0 * (100 / 1000.0)).toInt() and 1.inv()
            val samples = ShortArray(countFull)
            var i = 0
            while (i < count) {
                val sample = (sin(2.0 * Math.PI * i.toDouble() / (44100.0 / freqHz))*Short.MAX_VALUE*amplitude).toShort()
                samples[i + 0] = sample
                samples[i + 1] = sample
                i += 2
            }
            while(i < countFull){
                samples[i + 0] = (0).toShort()
                samples[i + 1] = (0).toShort()
                i += 2
            }
            track.write(samples, 0, countFull)
        }

        private fun fade(){
            amplitude -= 1/count
        }

        public fun setFreqHz(freq: Float){
            freqHz = freq
        }

        override fun run() {
            track.play()
        }
    }
}
