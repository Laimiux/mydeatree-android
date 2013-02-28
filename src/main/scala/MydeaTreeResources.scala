package com.limeblast.mydeatree

import reflect.BeanInfo

import java.util.List


class ObjectIdWithDate(val id: String, val modified_date: String)

@BeanInfo class BasicUser(username: String, email: String, password: String)

// This basic idea class
abstract class BasicIdea(val id: String, val title: String, val text: String, val created_date: String, val modified_date: String)



@BeanInfo class Meta(val limit: Int, val next: String, val offset: Int, val previous: String, val total_count: Int)

@BeanInfo class Owner(val username: String)

@BeanInfo class Idea(title: String, text: String, id: String, val parent:String, created_date: String,
                         modified_date: String, val resource_uri: String, val public: Boolean) extends BasicIdea(id, title, text, created_date, modified_date)

@BeanInfo class PublicIdea(title: String, text: String, id: String,val parent: String, created_date: String, modified_date: String,
                           val resource_uri: String, val owner: Owner) extends BasicIdea(id, title, text, created_date, modified_date)

@BeanInfo class User(val username: String, val first_name: String, val last_name: String, val resource_uri: String)

@BeanInfo class FavoriteItem(content_object: String, id: String, object_id: String, resource_uri: String)


@BeanInfo abstract class DjangoRootObject[K](val meta: Meta, val objects: List[K])

@BeanInfo class FavoriteItems(meta: Meta, objects: List[FavoriteItem]) extends DjangoRootObject(meta, objects)
@BeanInfo class PublicIdeas(meta: Meta, objects: List[PublicIdea]) extends DjangoRootObject(meta, objects)
@BeanInfo class PersonalIdeas(meta: Meta, objects: List[Idea]) extends DjangoRootObject(meta, objects)
@BeanInfo class Users(meta: Meta, objects: List[User]) extends DjangoRootObject(meta, objects)
