/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package generators

import models.TaskStatus.TaskStatus
import models.registration.Matched
import models.{TaskStatus, TrusteesBasedInTheUK}
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait ModelGenerators {

  implicit lazy val arbitraryTrusteesBasedInTheUK: Arbitrary[TrusteesBasedInTheUK] = {
    Arbitrary {
      Gen.oneOf(TrusteesBasedInTheUK.values)
    }
  }

  implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = {
    Arbitrary {
      for {
        year <- Gen.choose(min = 1500, max = 2099)
        month <- Gen.choose(1, 12)
        day <- Gen.choose(
          min = 1,
          max = month match {
            case 2 if year % 4 == 0 => 29
            case 2 => 28
            case 4 | 6 | 9 | 11 => 30
            case _ => 31
          }
        )
      } yield {
        LocalDate.of(year, month, day)
      }
    }
  }

  implicit lazy val arbitraryMatches: Arbitrary[Matched] = {
    Arbitrary {
      Gen.oneOf(Matched.values.toList)
    }
  }

  implicit lazy val arbitraryTaskStatus: Arbitrary[TaskStatus] = Arbitrary {
    Gen.oneOf(TaskStatus.values.toList)
  }

}
