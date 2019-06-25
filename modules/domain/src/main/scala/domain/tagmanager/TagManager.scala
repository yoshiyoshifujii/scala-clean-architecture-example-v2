package domain.tagmanager

import domain.constructionimage.ConstructionImage
import domain.tag.Tag

trait TagManager {

  val tag: Tag
  val constructionImage: ConstructionImage

}
