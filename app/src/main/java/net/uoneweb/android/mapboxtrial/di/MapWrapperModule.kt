package net.uoneweb.android.mapboxtrial.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.uoneweb.android.mapboxtrial.wrapper.MapWrapper
import net.uoneweb.android.mapboxtrial.wrapper.mapbox.MapBoxWrapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MapWrapperModule {
    @Singleton
    @Provides
    fun bindMapwoxWrapper(): MapWrapper = MapBoxWrapper()
}