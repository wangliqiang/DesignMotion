package com.app.design_motion.data

import com.app.design_motion.data.model.Theaters
import com.app.design_motion.data.remote.RetrofitFactory

class MovieRepository {

    suspend fun loadIntheaters(city: String, start: Int, count: Int): Theaters {
        return RetrofitFactory.getMovieService().getInTheaters(city, start, count)
    }

    suspend fun loadComingSoon(city: String, start: Int, count: Int): Theaters {
        return RetrofitFactory.getMovieService().getComingSoon(city, start, count)
    }
}