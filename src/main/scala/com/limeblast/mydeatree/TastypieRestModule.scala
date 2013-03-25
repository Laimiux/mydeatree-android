package com.limeblast.mydeatree

import com.limeblast.rest.RestModule
import java.util
import android.util.Log

/**
 * Created with IntelliJ IDEA.
 * User: Laimiux
 * Date: 3/21/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
trait TastypieRestModule[Obj <: { def resource_uri: String }, ObjCollection <: { def meta: Meta; def objects: util.List[Obj]}] extends RestModule[Obj, ObjCollection] {
  private val MODULE_TAG = "TastyPieRestModule"

  private def constructObjectUrl(obj: Obj): String = api_url + obj.resource_uri


  /**
   * Deletes the object
   * @param obj Object to be deleted
   * @return
   */
  def deleteObject(obj: Obj): Boolean = deleteObject(constructObjectUrl(obj))

  /**
   * Updates the object
   * @param objectToUpdate Updated object that should be sent to the server
   * @return
   */
  def updateObject(objectToUpdate: Obj): Option[Obj] = {
    val url = constructObjectUrl(objectToUpdate)
    if(RestModule_DEBUG) Log.d(MODULE_TAG, "Updating object at " + url)
    updateObject(url, objectToUpdate)
  }


  def collectionToList(collection: ObjCollection): util.ArrayList[Obj] = new util.ArrayList(collection.objects)

  /**
   * Method for handling Meta information
   * @param objects Current objects retrieved so far
   * @param collection Collection object
   */
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