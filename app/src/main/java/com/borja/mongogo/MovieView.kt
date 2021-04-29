package com.borja.mongogo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide


/*
    creamos todos los constructores automaticamente por nosotros,
    y en el init creamos el constructor para añadirle lo que necesitems a mayores
    lo primero que hacemos es inflar la vista

 */
class MovieView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val cover: ImageView
    private val title: TextView

    // el this le pasas la vista padre que va a inflar esta vista hija en este caso es
    // el linear layout que estamos creando
    //attach to Parent indica si las vistas que estamos creando se añaden automaticamente a la padre si o no.

    init {
        var view = LayoutInflater
            .from(context)
            .inflate(R.layout.view_movie, this, true)

        cover = findViewById(R.id.cover_id)
        title = findViewById(R.id.title_id)

        orientation = VERTICAL
    }

    fun setMovie(movie: Movie) {
        title.text = movie.title
        Glide.with(context).load(movie.cover).into(cover)

        //android no permite cargar datosd e tipo image sin uytilizar librerias de terceros de por medio
        // por lo que esto no es posible ahcerlo, de momento
    }
}