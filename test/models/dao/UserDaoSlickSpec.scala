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

import java.io.File
import javax.inject.Inject

import akka.actor.Status.Success
import com.google.inject.Guice
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.{ Profile, User }
import models.services.{ UserService, UserServiceImpl }
import org.mockito.Mockito._
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers, TestSuite }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{ FakeApplicationFactory, PlaySpec }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{ Application, Configuration }
import play.api.test._
import play.api.test.Helpers._
import play.api.inject.bind
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import libs.AppFactory

class UserDaoSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with MockitoSugar with AppFactory {

  var user1: User = _
  var user2: User = _
  var profile1: Profile = _
  var profile2: Profile = _
  val userDao: UserDaoSlick = fakeApplication().injector.instanceOf[UserDaoSlick]

  def generateProfile(user: User): Profile = {
    Profile(
      loginInfo = LoginInfo(CredentialsProvider.ID, user.email),
      email = Some(user.email),
      firstName = Some(user.firstName),
      lastName = Some(user.surName),
      fullName = Some(s"${user.firstName} ${user.surName}"))
  }

  before {
    user1 = User(id = None, firstName = "Bart", surName = "Illiyan", email = "String")
    user2 = User(id = None, firstName = "Micvhael", surName = "Grunthy",
      email = "somewhere@over.the.rainbow")
    profile1 = generateProfile(user1)
    user1 = user1.copy(profiles = profile1 :: user1.profiles)

    profile2 = generateProfile(user2)
    user2 = user2.copy(profiles = profile2 :: user2.profiles)



    //    user2 = User(id = Option(2), firstName = "Peter", surName = "Pan", email = "String")
  }

  describe("UserDaoSlick") {
    it("should correctly insert a user by id (new user)") {
      //tests save
      val returnedUser = userDao.save(user1)
      returnedUser map {
        result => {
          result should not be None
          result match {
            case Some(usr) => {
              //also tests delete
              userDao.delete(usr.id.get)
              usr.id.isDefined should be(true)
              usr.id.get should be > 0
            }
          }
        }
      }
    }

    it("should correctly update a user by id (existing user)") {
      //tests save
      val returnedUser = userDao.save(user1)
      returnedUser map {
        result => {
          result should not be None
          result match {
            case Some(usr) => {
              val returnedUser2 = userDao.update(usr.copy(firstName = "Malethew"))
              returnedUser2 map {
                secondResult => {
                  secondResult should not be None
                  secondResult match {
                    case Some(uzr) => {
                      uzr.id.isDefined should be(true)
                      uzr.id.get should be(usr.id.get)
                      uzr.firstName should be("Malethew")
                    }
                  }
                }
              }
              //also tests delete
              userDao.delete(usr.id.get)
              usr.id.isDefined should be(true)
              usr.id.get should be > 0
            }
          }
        }
      }
    }

//    it("should retrieve an existing profile") {
//      val returnedUser = userDao.find(27)
//            returnedUser map {
//              result => {
//                result should not be None
//                result match {
//                  case Some(usr) => {
//                    usr.id.isDefined should be(true)
//                    usr.id.get should be (27)
//                    usr.firstName should be ("Bart")
//                    usr.profiles.head should be (profile1.copy(userId = Some(27)))
//                  }
//                }
//              }
//            }
//    }

    it("should correctly link a profile") {
      val returnedUser = userDao.save(user1)
      returnedUser map {
        result => {
          result should not be None
          result match {
            case Some(usr) => {
              //now perform link
              val linkedUser = userDao.link(usr, profile1)
              linkedUser map {
                r => {
                  r should not be None
                  r match {
                    case Some(u) => {
                      userDao.delete(u.id.get)
                      u.profiles.size should be(1)
                      u.profiles.head should be(profile1)
                      u.profiles.head.userId should be(u.id)
                    }
                  }
                }
              }

              usr.profiles.size should be(1)
              usr.profiles.head should be(profile1.copy(userId = usr.id))
            }
          }
        }
      }
    }

    it("should correctly save an attached user profile") {
      val returnedUser = userDao.save(user2)
      returnedUser map {
        result =>
          {
            result should not be None
            result match {
              case Some(usr) => {
                //also tests delete
                //userDao.delete(usr.id.get)
                val expected = profile2.copy(userId = usr.id)
                usr.profiles.size should be(1)
                usr.profiles.head should be(expected)
              }
            }
          }
      }
    }

  }
}