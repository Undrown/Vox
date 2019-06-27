package com.undrown.vox

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainPanel.setOnTouchListener {_, event ->
            Toast.makeText(applicationContext,"${event.x*20}", Toast.LENGTH_SHORT).show()
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    Thread(VoxTrack(event.x*20)).start()
                }
            }
            false
        }

    }

    //private fun generateTone(freqHz: Float, track: AudioTrack) {
    //    //val count = (44100.0 * 2.0 * (100 / 1000.0)).toInt() and 1.inv()
    //    val samples = ShortArray(count)
    //    var i = 0
    //    while (i < count) {
    //        val sample = (sin(2.0 * Math.PI * i.toDouble() / (44100.0 / freqHz))*Short.MAX_VALUE).toShort()
    //        samples[i + 0] = sample
    //        samples[i + 1] = sample
    //        i += 2
    //    }
    //    track.write(samples, 0, count)
    //}
//
    //private fun generateTrack(count : Int):AudioTrack{
    //    return AudioTrack(
    //        AudioManager.STREAM_MUSIC, 44100,
    //        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
    //        count * (Short.SIZE / 8), AudioTrack.MODE_STREAM
    //    )
    //}

    class VoxTrack(private val freqHz: Float):Runnable{

        var amplitude:Float = 1.0f
        val count = 44100
        val track = generateTrack(count)

        private fun generateTrack(count : Int):AudioTrack{
            return AudioTrack(
                AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                44100 * (Short.SIZE_BYTES), AudioTrack.MODE_STREAM
            )
        }

        private fun generateTone(freqHz: Float, track: AudioTrack) {
            //val count = (44100.0 * 2.0 * (100 / 1000.0)).toInt() and 1.inv()
            val samples = ShortArray(count)
            var i = 0
            while (i < count) {
                val sample = (sin(2.0 * Math.PI * i.toDouble() / (44100.0 / freqHz))*Short.MAX_VALUE*amplitude).toShort()
                samples[i + 0] = sample
                samples[i + 1] = sample
                i += 2
                if(amplitude > 0)
                    fade()
            }
            track.write(samples, 0, count)
        }

        private fun fade(){
            amplitude -= 1/count
        }

        override fun run() {
            generateTone(freqHz, track)
            track.play()
            //fade()
        }
    }
}
