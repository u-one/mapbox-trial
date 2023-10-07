package net.uoneweb.android.mapbox.wrapper.mapbox

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.uoneweb.android.mapbox.wrapper.MapWrapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MapWrapperModule {
    @Singleton
    @Provides
    fun bindMapwoxWrapper(): MapWrapper = MapBoxWrapper()
}