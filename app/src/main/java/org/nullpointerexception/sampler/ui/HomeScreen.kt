package org.nullpointerexception.sampler.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.huhx0015.hxaudio.audio.HXSound
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.overlay_bpm.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.nullpointerexception.sampler.R
import org.nullpointerexception.sampler.event.PeriodChangeEvent
import org.nullpointerexception.sampler.logic.NetworkController
import org.nullpointerexception.sampler.logic.SoundController
import java.util.*

class HomeScreen : AppCompatActivity() {

    private var soundController: SoundController? = null

    private var timers = arrayOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        HXSound.engines(20)
        HXSound.logging(true)

        timers = arrayOf(
            imgTimerBeat1, imgTimerBeat2,
            imgTimerBeat3, imgTimerBeat4,
            imgTimerBeat5, imgTimerBeat6,
            imgTimerBeat7, imgTimerBeat8,
            imgTimerBeat9, imgTimerBeat10,
            imgTimerBeat11, imgTimerBeat12,
            imgTimerBeat13, imgTimerBeat14,
            imgTimerBeat15, imgTimerBeat16
        )

        // регаемся на ивенты таймера
        EventBus.getDefault().register(this)

        soundController = SoundController(this)

        // Убираем анимацию про notifyitemchanged
        (recycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        recycler.apply {
            layoutManager = LinearLayoutManager(this@HomeScreen)
            adapter = soundController?.soundEntities?.let {
                HomeAdapter(
                    it
                )
            }
        }

        btnShuffle.setOnClickListener {
            shuffle()
        }

        // кнопка плюс
        btnPlus.setOnClickListener {
            soundController?.bpm?.let { bpm ->
                soundController?.bpm = bpm + 5
            }
            updateBPM()
        }

        // кнопка минус
        btnMinus.setOnClickListener {
            soundController?.bpm?.let { bpm ->
                soundController?.bpm = bpm - 5
            }
            updateBPM()
        }
        btnPlay.setOnClickListener {
            // если играет - стоп, если на паузе - играть
            if (soundController?.playing == true) {
                btnPlay.setImageResource(R.drawable.ic_newplay)
                soundController?.pause()
                removeTimers()
            } else {
                btnPlay.setImageResource(R.drawable.ic_newpause)
                soundController?.play()
            }
        }
        btnReset.setOnClickListener {
            soundController?.resetSoundboard()
            recycler.adapter?.notifyDataSetChanged()
        }
        updateBPM()
    }

    fun shuffle() {
        for (s in 0..6) {
            if (Random().nextBoolean()) {

                if (Random().nextBoolean()) {
                    soundController?.soundEntities?.get(s)!!.array = arrayOf<Boolean>(
                        true, false, true, false, true, false, true, false,
                        true, false, true, false, true, false, true, false
                    )
                } else {

                    if (Random().nextBoolean()) {
                        soundController?.soundEntities?.get(s)!!.array = arrayOf<Boolean>(
                            false, true, false, true, false, true, false, true,
                            false, true, false, true, false, true, false, true
                        )
                    } else {
                        soundController?.soundEntities?.get(s)!!.array = arrayOf<Boolean>(
                            false, true, true, false, false, true, true, false,
                            false, true, true, false, false, true, true, false
                        )
                    }
                }
            } else {
                soundController?.soundEntities?.get(s)!!.array = arrayOf<Boolean>(
                    false, false, false, false, false, false, false, false,
                    false, false, false, false, false, false, false, false
                )
            }
        }
        recycler.adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        NetworkController.init()
    }

    private fun updateBPM() {
        // Обновляем TextView из SoundController'a
        txvBpm.text = soundController?.bpm?.toString()
    }

    private fun removeTimers() {
        // Очищаем все таймеры-вьюшки
        removeTimers(
            timers = timers
        )
    }

    private fun removeTimers(timers: Array<View>) {
        for (view in timers) {
            view.setBackgroundResource(android.R.color.transparent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(periodChangedEvent: PeriodChangeEvent) {
        val transparent = android.R.color.transparent
        val active = R.color.colorPrimary

        val activeIndex = periodChangedEvent.period
        // формула для вычисления предыдущего значения
        val inactiveIndex = (activeIndex + timers.size - 1) % 16

        timers[activeIndex].setBackgroundResource(active)
        timers[inactiveIndex].setBackgroundResource(transparent)
    }
}