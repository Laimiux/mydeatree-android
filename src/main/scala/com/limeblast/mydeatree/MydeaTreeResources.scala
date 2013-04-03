package com.limeblast.mydeatree

import java.util.{List=>JList}



class DatedObject(val id: String, val modified_date: String)

@scala.beans.BeanInfo class BasicUser(username: String, email: String, password: String)

// This basic idea class
abstract class BasicIdea(val id: String, val title: String, val text: String, val created_date: String, val modified_date: String, val resource_uri: String, val parent: String)



@scala.beans.BeanInfo class Meta(val limit: Int, val next: String, val offset: Int, val previous: String, val total_count: Int)

@scala.beans.BeanInfo class Owner(val username: String)

@scala.beans.BeanInfo class Idea(title: String, text: String, id: String, parent:String, created_date: String,
                         modified_date: String, resource_uri: String, val public: Boolean) extends BasicIdea(id, title, text, created_date, modified_date, resource_uri, parent)

@scala.beans.BeanInfo class PublicIdea(title: String, text: String, id: String, parent: String, created_date: String, modified_date: String,
                           resource_uri: String, val owner: Owner, val children_count: Int) extends BasicIdea(id, title, text, created_date, modified_date, resource_uri, parent)

@scala.beans.BeanInfo class User(val username: String, val first_name: String, val last_name: String, val resource_uri: String)



@scala.beans.BeanInfo class FavoriteIdea(val id: String, val favorite_idea: String, val resource_uri: String)

@scala.beans.BeanInfo abstract class DjangoRootObject[K](val meta: Meta, val objects: JList[K])

@scala.beans.BeanInfo class FavoriteIdeas(meta: Meta, objects: JList[FavoriteIdea]) extends DjangoRootObject(meta, objects)
@scala.beans.BeanInfo class PublicIdeas(meta: Meta, objects: JList[PublicIdea]) extends DjangoRootObject(meta, objects)
@scala.beans.BeanInfo class PersonalIdeas(meta: Meta, objects: JList[Idea]) extends DjangoRootObject(meta, objects)
@scala.beans.BeanInfo class Users(meta: Meta, objects: JList[User]) extends DjangoRootObject(meta, objects)
