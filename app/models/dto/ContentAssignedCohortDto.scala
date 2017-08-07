// Copyright (C) 2017 Alexander Worton.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package models.dto

import models.dto.CohortDto.CohortsTable
import models.dto.ContentAssignedDto.ContentAssignedTable
import play.api.data.Form
import play.api.data.Forms.{ mapping, number, optional }
import play.api.libs.json.Json
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{ PrimaryKey, ProvenShape }
import slick.model.ForeignKeyAction

case class ContentAssignedCohortDto(assignedId: Option[Int], cohortId: Option[Int])

object ContentAssignedCohortDto {

  class ContentAssignedCohortTable(tag: Tag) extends Table[ContentAssignedCohortDto](
    tag, "content_assigned_cohorts") {
    def assignedId: lifted.Rep[Option[Int]] = column[Int]("AssignedId")
    def cohortId: lifted.Rep[Option[Int]] = column[Int]("CohortId")

    def * : ProvenShape[ContentAssignedCohortDto] =
      (assignedId, cohortId) <> ((ContentAssignedCohortDto.apply _).tupled, ContentAssignedCohortDto.unapply)

    def pk: PrimaryKey = primaryKey("PRIMARY", (assignedId, cohortId))

    def assignedFK = foreignKey("AssignedId", assignedId, TableQuery[ContentAssignedTable])(
      _.id,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade)

    def cohortsFK = foreignKey("CohortId", cohortId, TableQuery[CohortsTable])(
      _.id,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade)

  }

  def form: Form[ContentAssignedCohortDto] = Form(
    mapping(
      "assignedId" -> optional(number),
      "cohortId" -> optional(number)
    )(ContentAssignedCohortDto.apply _)(ContentAssignedCohortDto.unapply)
  )
  implicit val serializer = Json.format[ContentAssignedCohortDto]
}