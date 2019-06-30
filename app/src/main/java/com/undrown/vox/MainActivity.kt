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
import java.lang.IndexOutOfBoundsException
import java.util.*
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val players = Array(8, init = { VoxTrack()})
        players.forEach { Thread(it).start() }
        mainPanel.setOnTouchListener {_, event ->
            //Toast.makeText(applicationContext,"${event.x}", Toast.LENGTH_SHORT).show()
            val index = players.indexOfFirst { it.isReady() }
            val player = players[if(index==-1)0 else index]
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    Toast.makeText(applicationContext,"$player", Toast.LENGTH_SHORT).show()
                    player.stop()
                    player.setFreqHz(event.x)
                    player.generateTone()
                    player.play()
                }
                MotionEvent.ACTION_UP -> {
                    player.stop()
                }
                MotionEvent.ACTION_MOVE -> {
                    player.stop()
                    player.setFreqHz(event.x)
                    player.generateTone()
                    player.play()
                }
            }
            false
        }

    }

    class VoxTrack :Runnable{
        private var freqHz: Float = 0.0f
        var amplitude:Float = 1.0f
        val count = 44100
        val countFull = count*2
        var samples = ShortArray(countFull)
        val track = generateTrack(count)

        private fun generateTrack(count : Int):AudioTrack{
            return AudioTrack(
                AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                440 * (Short.SIZE_BYTES), AudioTrack.MODE_STREAM
            )
        }

        fun generateTone() {
            //val count = (44100.0 * 2.0 * (100 / 1000.0)).toInt() and 1.inv()
            samples.fill(0, 0, countFull)
            track.flush()
            var i = 0
            while (i < countFull) {
                val sample = (sin(2.0 * Math.PI * i.toDouble() / (44100.0 / freqHz))*Short.MAX_VALUE*amplitude).toShort()
                samples[i + 0] = sample
                samples[i + 1] = sample
                i += 2
            }
            //while(i < countFull){
            //    samples[i + 0] = (0).toShort()
            //    samples[i + 1] = (0).toShort()
            //    i += 2
            //}
            track.write(samples, 0, countFull)
        }

        private fun fade(){
            amplitude -= 1/count
        }

        fun setFreqHz(freq: Float){
            freqHz = freq
        }

        fun stop(){
            track.flush()
            track.pause()
        }

        fun play(){
            track.play()
        }

        fun isReady():Boolean{
            return samples.all { it==0.toShort() }
        }

        override fun run() {
            track.play()
        }
    }
}
