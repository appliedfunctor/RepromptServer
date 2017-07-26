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

package models.dao

import javax.inject.Inject

import models.dto.{ContentItemDto, ContentPackageDto}
import models.dto.ContentPackageDto.PackageTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

class ContentPackageDaoSlick @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends ContentPackageDao with HasDatabaseConfigProvider[JdbcProfile] {

  private val ContentPackages = TableQuery[PackageTable]

  def findContentPackageQuery(packageId: Int) =
    sql"""
          SELECT  cp.Id, cp.FolderId, cp.OwnerId, cp.Name,
                  ci.Id, ci.ImageUrl, ci.Content, ci.Name

          FROM content_packages AS cf

            LEFT JOIN content_items AS ci
            ON cp.Id = ci.PackageId

          WHERE cp.Id = $packageId
         """.as[(ContentPackageDto, Option[ContentItemDto])]

  def findContentPackageQueryByOwner(ownerId: Int) =
    sql"""
          SELECT  cp.Id, cp.FolderId, cp.OwnerId, cp.Name,
                  ci.Id, ci.ImageUrl, ci.Content, ci.Name

          FROM content_packages AS cf

              LEFT JOIN content_items AS ci
              ON cp.Id = ci.PackageId

          WHERE cp.OwnerId = $ownerId
          ORDER BY cp.Name, ci.Name
         """.as[(ContentPackageDto, Option[ContentItemDto])]

  override def find(packageId: Int): Future[Option[ContentPackageDto]] = {
    val result = findContentPackageQuery(packageId)
    val run = db.run(result)

    run.flatMap(
      r => {
        if (r.nonEmpty) {
          val packageList = r.map(p => p._2.get).toList.filter(m => m.id.get > 0)
          Future(Some(r.head._1.copy(content = Some(packageList))))
        } else {
          Future(None)
        }
      })
  }

  override def findByOwner(ownerId: Int): Future[Seq[ContentPackageDto]] = {
    val result = findContentPackageQueryByOwner(ownerId)
    val run = db.run(result)

    run.flatMap(
      r => {
        val grouped = r.groupBy(_._1)
        val out = for {
          groupMembers <- grouped
          packages = groupMembers._2
          packagesProc = packages.map(p => p._2.get).toList.filter(m => m.id.get > 0)
          result = groupMembers._1.copy(content = Some(packagesProc))
        } yield result
        Future(out.toSeq)
      }
    )
  }

  override def save(packageDto: ContentPackageDto): Future[Option[ContentPackageDto]] = {
    db.run((ContentPackages returning ContentPackages.map(_.id)
      into ((packge, returnedId) => Some(packge.copy(id = returnedId)))
      ) += packageDto)
  }

  override def update(packageDto: ContentPackageDto): Future[Option[ContentPackageDto]] = {
    if (packageDto.id.isEmpty) {
      Future(None)
    } else {
      for {
        _ <- db.run(ContentPackages.filter(_.id === packageDto.id).map(c => (c.name, c.folderId, c
          .ownerId))
          .update(packageDto.name, packageDto.folderId, packageDto.ownerId))
        read <- find(packageDto.id.get)
      } yield read
    }
  }

  override def delete(packageId: Int): Future[Int] = {
    db.run(ContentPackages.filter(_.id === packageId).delete)
  }

  override def deleteByOwner(ownerId: Int): Future[Int] = {
    db.run(ContentPackages.filter(_.ownerId === ownerId).delete)
  }
}
