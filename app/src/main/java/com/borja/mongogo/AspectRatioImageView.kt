package com.borja.mongogo

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class AspectRatioImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var ratio: Float = 1f //es el ratio con el que se generarán las imagenes al añadirse al grid

    /*
    CONSTRUCTOR
    el valuel por defecto que se crea se utilizará solo cuando la foto nos haya venido sin valor.
     */
    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView)
        ratio = a.getFloat(R.styleable.AspectRatioImageView_ratio, 1f)
        a.recycle()
    }

    /*
        dejamos que la vista coja las propiedades del padre que necesita, pero le modificamos so alto con el aspect ratio
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var width = measuredWidth
        var height = measuredHeight

        /*
            //este return hará que la funcion pare de ejecutarse a partir de aqui normalmente porque la viusta
             todavia no ha terminado de medirse
            y portanto no tienes suficientes datos para pintarla, tefalta informacion
         */
        if (width == 0 && height == 0) {
            return
        }
        /*
            ahora comprobamos si alguna de las dos es cero, cual es distinta de cero. para calcular la otra
         */

        if (width > 0) {
            height = (width * ratio).toInt()
        } else if (height > 0) {
            width = (height / ratio).toInt()
        }

        setMeasuredDimension(width, height)
    }
}