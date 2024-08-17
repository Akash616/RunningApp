package com.akashgupta.runningapp.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {
    /*Add a type converter to our database, we need to define two functions
        . One function to provide a way for room to convert a bitmap into a format that room understand.
          Bitmap -> ByteArray(10011...) -> Save in DB
        . Another fun to convert the format that room understand to the format that we want to have.
          Get from DB -> convert to BMP -> Bitmap
     */

    //@TypeConverter -> tell room that those are type converter functions. Room will automatically look up and use.

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap{
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray{
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}