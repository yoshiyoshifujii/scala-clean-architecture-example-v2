package domain.tag

import com.github.j5ik2o.dddbase.Aggregate

import scala.reflect._

sealed trait Tag extends Aggregate {
  override type AggregateType = Tag
  override type IdType        = TagId
  override protected val tag: ClassTag[Tag] = classTag[Tag]

  val key: TagKey
  val value: TagValue
}

case class GeneratedTag private[tag] (
    id: TagId,
    key: TagKey,
    value: TagValue
) extends Tag

case class ResolvedTag private[tag] (
    id: TagId,
    key: TagKey,
    value: TagValue
) extends Tag

object Tag {

  val generate: (TagId, TagKey, TagValue) => GeneratedTag = GeneratedTag.apply

  val generateResolved: (TagId, TagKey, TagValue) => ResolvedTag = ResolvedTag.apply

}
