package com.xhochy

import scala.util.parsing.combinator.RegexParsers


case class InfoboxAttribute(key: String, value: String)
case class Infobox(attributes: List[InfoboxAttribute])

/**
 * Parses a Wikipedia Infobox for Artists. 
 *
 * This is made as all other infobox on MediaWiki by a fixed prefix and then
 * (key, value) pairs of the the type "|key = value".
 */
class InfoboxArtistParser extends RegexParsers {
  override type Elem = Char
  def start = "{{Infobox musical artist" ~ comment.?
  def comment = "!-- [^\n]*".r
  def key = "[^=]+".r ^^ { n => n.trim() }
  def variable = "\\{\\{[^\\}]*}}".r
  def link = ("\\[\\[[^\\]]*\\]\\]".r) | ("\\[[^\\[\\]]*\\]".r)
  def valuetext = "[^|\\}\\{\\[]+".r
  def value = (valuetext | variable | link)*
  def attribute = "|" ~ key ~ "=" ~ value ^^
  { case l~k~e~v => InfoboxAttribute(k, v.mkString("")) }
  def attributes = attribute*
  def box = start ~> attributes <~ "}}" ^^ { a => Infobox(a) }
}

// vim: set ts=2 sw=2 et sts=2:
