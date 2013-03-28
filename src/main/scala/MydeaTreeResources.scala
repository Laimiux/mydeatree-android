package com.limeblast.mydeatree

import reflect.BeanInfo

import java.util.List
import com.google.gson.annotations.SerializedName


class DatedObject(val id: String, val modified_date: String)

@BeanInfo class BasicUser(username: String, email: String, password: String)

// This basic idea class
abstract class BasicIdea(val id: String, val title: String, val text: String, val created_date: String, val modified_date: String, val resource_uri: String, val parent: String)



@BeanInfo class Meta(val limit: Int, val next: String, val offset: Int, val previous: String, val total_count: Int)

@BeanInfo class Owner(val username: String)

@BeanInfo class Idea(title: String, text: String, id: String, parent:String, created_date: String,
                         modified_date: String, resource_uri: String, val public: Boolean) extends BasicIdea(id, title, text, created_date, modified_date, resource_uri, parent)

@BeanInfo class PublicIdea(title: String, text: String, id: String, parent: String, created_date: String, modified_date: String,
                           resource_uri: String, val owner: Owner, val children_count: Int = 0) extends BasicIdea(id, title, text, created_date, modified_date, resource_uri, parent)

@BeanInfo class User(val username: String, val first_name: String, val last_name: String, val resource_uri: String)

//@DatabaseTable(tableName = "favorite_ideas")
@BeanInfo class FavoriteIdea(val id: String, val favorite_idea: String, val resource_uri: String)

@BeanInfo abstract class DjangoRootObject[K](val meta: Meta, val objects: List[K])

@BeanInfo class FavoriteIdeas(meta: Meta, objects: List[FavoriteIdea]) extends DjangoRootObject(meta, objects)
@BeanInfo class PublicIdeas(meta: Meta, objects: List[PublicIdea]) extends DjangoRootObject(meta, objects)
@BeanInfo class PersonalIdeas(meta: Meta, objects: List[Idea]) extends DjangoRootObject(meta, objects)
@BeanInfo class Users(meta: Meta, objects: List[User]) extends DjangoRootObject(meta, objects)



/*

*/


@com.limeblast.scaliteorm.DatabaseTable(tableName = "some_table")
class SomeTable(val id: String, val description: String)