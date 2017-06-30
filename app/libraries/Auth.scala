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

package libraries

import org.mindrot.jbcrypt.BCrypt

object Auth {
    def login(username: String, clearPass: String, hashedPass: String): Boolean = {
        BCrypt.checkpw(clearPass, hashedPass)
    }

    def encrypt(clearPass: String): String = {
      val encrypted = BCrypt.hashpw(clearPass, BCrypt.gensalt())
      val checked = BCrypt.checkpw(clearPass, encrypted)
      encrypted + " :: " + checked
    }
}