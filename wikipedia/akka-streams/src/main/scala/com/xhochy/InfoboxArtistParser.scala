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
  override def skipWhitespace = false
  /**
   * Start with "{{Infobox musical artist" and skip the trailing rest of this
   * line as we do not utilise the information (yet).
   */
  def start = "\\{\\{Infobox musical artist( )*(\\|[^\n]*)*".r ~ comment.?
  def end = "\n\\|".r.? ~ "\\|?\\s*}}".r
  def comment = "!--[^\n]*".r
  def key = "[^=\\}]+".r ^^ { n => n.trim() }
  def variable = "\\n?\\{\\{[^\\}]*}}".r
  def link = ("\\s*\\[\\[[^\\]]*\\]\\]".r) | ("\\s*\\[[^\\[\\]]*\\]".r)
  def valuetext = "\\s*[^|\\}\\{\\[\n][^\\}\\{\\[\n]*".r
  def value = (valuetext | variable | link)*
  def macroAttribute = "\n( )*|\\{\\{[^\\}]*\\}\\}".r ^^
  { case _ => InfoboxAttribute("", "") }
  def attribute = "\n( )*|".r ~ key ~ "=" ~ value ^^
  { case l~k~e~v => InfoboxAttribute(k, v.mkString("")) }
  def attributes = (attribute | macroAttribute)*
  def box = start ~> attributes <~ end ^^ { a => Infobox(a) }
}

// vim: set ts=2 sw=2 et sts=2:
