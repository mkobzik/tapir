package sttp.tapir.docs.asyncapi

import sttp.tapir.Endpoint
import sttp.tapir.SchemaType.SObjectInfo

case class AsyncAPIDocsOptions(
                                subscribeOperationId: (Vector[String], Endpoint[_, _, _, _]) => String,
                                publishOperationId: (Vector[String], Endpoint[_, _, _, _]) => String,
                                schemaObjectInfoToNameMapper: SObjectInfo => String = SObjectInfo.defaultToNameMapper
)

object AsyncAPIDocsOptions {
  val defaultOperationIdGenerator: String => (Vector[String], Endpoint[_, _, _, _]) => String = { prefix => (pathComponents, _) =>
    val components = if (pathComponents.isEmpty) {
      Vector("root")
    } else {
      pathComponents
    }

    // converting to camelCase
    (prefix +: components.map(_.toLowerCase.capitalize)).mkString
  }

  implicit val default: AsyncAPIDocsOptions = AsyncAPIDocsOptions(defaultOperationIdGenerator("on"), defaultOperationIdGenerator("send"))
}
