package com.sedat.note.hilt

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sedat.note.R
import com.sedat.note.room.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Singleton
    @Provides
    fun injectRoomDb(@ApplicationContext context: Context) =
        Room.databaseBuilder(
            context,
            Database::class.java,
            "NoteDB"
        ).build()

    @Singleton
    @Provides
    fun injectGlide(@ApplicationContext context: Context) =
        Glide.with(context)
            .setDefaultRequestOptions(
                RequestOptions().placeholder(R.drawable.replay_icon)
                    .error(R.drawable.replay_icon)
            )

    @Singleton
    @Provides
    fun injectDao(database: Database) = database.noteDao()

}