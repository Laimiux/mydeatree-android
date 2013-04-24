package com.limeblast.androidhelpers

import android.util.Log
import language.experimental.macros
import reflect.macros.Context


object LoggerMacros {
  def logger(tag: String, param: Any): Unit = macro logger_impl

  def logger_impl(c: Context)(tag: c.Expr[String], param: c.Expr[Any]): c.Expr[Unit]  = {
    import c.universe._
    val paramRep = show(param.tree)
    val paramRepTree = Literal(Constant(paramRep))
    val paramRepExpr = c.Expr[String](paramRepTree)
    reify { if(true) Log.d(tag.splice, paramRepExpr.splice + " = " + param.splice) }
  }
}
