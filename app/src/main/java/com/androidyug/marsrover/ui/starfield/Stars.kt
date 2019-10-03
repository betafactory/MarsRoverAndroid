package com.androidyug.marsrover.ui.starfield

import android.graphics.Canvas
import android.graphics.Paint

import java.util.ArrayList
import java.util.Random

class Stars(private val size: Float, private val speed: Float, private val number: Int, private val mWidth: Int, private val mHeight: Int) {
    private val poss: ArrayList<Pos> = ArrayList()
    private val old: ArrayList<Pos> = ArrayList()
    private val r = Random()

    private class Pos(var x: Float, var y: Float, var mDirection: Int) {

        fun updatePos(move: Float) {
            when (mDirection) {
                RIGHT -> x += move
                LEFT -> x -= move
            }
        }
    }

    fun step(direction: Int) {
        var direction = direction
        if (r.nextInt(number) == 0) {
            val size = old.size
            if (size > 0) {
                poss.add(old.removeAt(size - 1))
            } else {
                if (direction == RANDOM) {
                    direction = r.nextInt(RANDOM) // Random direction
                }
                poss.add(Pos(-1f, 0f, direction))
            }
        }
        val width = mWidth
        val height = mHeight

        synchronized(poss) {
            val itr = poss.iterator()
            loop@ while (itr.hasNext()) {
                val pos = itr.next()

                when (pos.mDirection) {
                    RIGHT -> {
                        if (pos.x > width) {
                            pos.x = -1f
                            old.add(pos)
                            itr.remove()
                            continue@loop
                        }

                        if (pos.x < 0) {
                            pos.x = 0f
                            pos.y = (r.nextInt() % height).toFloat()
                        }
                    }
                    LEFT -> {
                        if (pos.x < 0) {
                            pos.x = (mWidth + 1).toFloat()
                            old.add(pos)
                            itr.remove()
                            continue@loop
                        }

                        if (pos.x > mWidth) {
                            pos.x = mWidth.toFloat()
                            pos.y = (r.nextInt() % height).toFloat()
                        }
                    }
                }
                pos.updatePos(speed)
            }
        }
    }

    fun draw(c: Canvas, width: Int, offset: Int, p: Paint) {
        for (pos in poss) {
            if (pos.x + size > offset && pos.x < offset + width) {
                c.drawRect(pos.x - offset, pos.y, pos.x - offset + size, pos.y + size, p)
            }
        }
    }

    companion object {
        const val LEFT = 0
        const val RIGHT = 1
        const val RANDOM = 2
    }
}