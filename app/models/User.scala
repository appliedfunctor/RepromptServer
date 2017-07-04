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

package models

import java.time.{ LocalDate, LocalDateTime }
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import com.mohiva.play.silhouette.api.services.IdentityService
import dao.UserDao

import scala.concurrent.Future

case class User(
  id: Option[Int],
  userName: String,
  firstName: String,
  surName: String,
  email: String,
  isEmailVerified: Boolean,
  authHash: String,
  authResetCode: Option[String],
  authResetExpiry: Option[LocalDate],
  authToken: Option[String],
  authExpire: Option[LocalDateTime],
  isEducator: Boolean,
  isAdministrator: Boolean,
  avatarUrl: Option[String]
) extends Identity