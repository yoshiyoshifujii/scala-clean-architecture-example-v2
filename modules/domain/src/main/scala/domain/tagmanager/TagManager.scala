package domain.tagmanager

import com.github.j5ik2o.dddbase.Aggregate
import domain.constructionimage.ConstructionImage
import domain.tag.Tag

import scala.reflect._

sealed trait TagManager extends Aggregate {
  override type AggregateType = TagManager
  override type IdType        = TagManagerId
  override protected val tag: ClassTag[TagManager] = classTag[TagManager]

  val dTag: Tag
  val constructionImage: ConstructionImage
}

case class GeneratedTagManager private[tagmanager] (
    id: TagManagerId,
    dTag: Tag,
    constructionImage: ConstructionImage
) extends TagManager

case class ResolvedTagManager private[tagmanager] (
    id: TagManagerId,
    dTag: Tag,
    constructionImage: ConstructionImage
) extends TagManager

object TagManager {

  val generate: (TagManagerId, Tag, ConstructionImage) => GeneratedTagManager = GeneratedTagManager.apply

  val generateResolved: (TagManagerId, Tag, ConstructionImage) => ResolvedTagManager = ResolvedTagManager.apply

}
