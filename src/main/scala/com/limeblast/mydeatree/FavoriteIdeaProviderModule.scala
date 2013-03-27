package com.limeblast.mydeatree

import com.limeblast.androidhelpers.HasProvider
import android.net.Uri
import providers.FavoriteIdeaProvider

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/27/13
 * Time: 1:15 PM
 * To change this template use File | Settings | File Templates.
 */
trait FavoriteIdeaProviderModule extends HasProvider {
  val provider_uri: Uri = FavoriteIdeaProvider.CONTENT_URI
}
