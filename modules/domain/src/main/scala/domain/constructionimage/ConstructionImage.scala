package domain.constructionimage

import com.github.j5ik2o.dddbase.Aggregate
import domain.project.Project

import scala.reflect._

sealed trait ConstructionImage extends Aggregate {
  override type AggregateType = ConstructionImage
  override type IdType        = ConstructionImageId
  override protected val tag: ClassTag[ConstructionImage] = classTag[ConstructionImage]

  val project: Project
  val image: Image
}

case class GeneratedConstructionImage private[constructionimage] (
    id: ConstructionImageId,
    project: Project,
    image: Image
) extends ConstructionImage

case class ResolvedConstructionImage private[constructionimage] (
    id: ConstructionImageId,
    project: Project,
    image: Image
) extends ConstructionImage

object ConstructionImage {

  val generate: (ConstructionImageId, Project, Image) => GeneratedConstructionImage = GeneratedConstructionImage.apply

  val generateResolve: (ConstructionImageId, Project, Image) => ResolvedConstructionImage =
    ResolvedConstructionImage.apply

}
