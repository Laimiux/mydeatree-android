package com.limeblast.mydeatree

import com.limeblast.rest.RestModule
import java.util

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/21/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
trait TastypieRestModule[Obj <: { def id: String; def resource_uri: String }, ObjCollection <: { def meta: Meta; def objects: util.List[Obj]}] extends RestModule[Obj, ObjCollection] {


  def collectionToList(collection: ObjCollection): util.ArrayList[Obj] = new util.ArrayList(collection.objects)

  override protected def handleCollectionMeta(objects: util.ArrayList[Obj], collection: ObjCollection) {
    val meta = collection.meta
    if (meta.next != null) {
      getObjects(api_url + meta.next) match {
        case Some(moreObjects) => objects.addAll(moreObjects)
        case None =>
      }
    }
  }
}