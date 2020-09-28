package com.sonny.xianwan.utils

import android.util.Size
import java.lang.Long

internal class CompareSizesByArea : Comparator<Size> {
    override fun compare(lhs: Size, rhs: Size): Int =
        Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

}