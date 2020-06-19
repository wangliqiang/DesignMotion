package com.app.design_motion.ui.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.design_motion.base.BaseViewModel
import com.app.design_motion.data.MovieRepository
import com.app.design_motion.data.model.Theaters

class MovieViewModel : BaseViewModel() {

    private val movieRepository: MovieRepository = MovieRepository()

    private var _inTheaters: MutableLiveData<Theaters> = MutableLiveData()
    val inTheaters: LiveData<Theaters>
        get() = _inTheaters


    fun loadInTheaters() {
        request(
            onExecute = {
                movieRepository.loadIntheaters("济南", 0, 50)
                    .let {
                        _inTheaters.postValue(it)
                    }
            }
        )
    }
}