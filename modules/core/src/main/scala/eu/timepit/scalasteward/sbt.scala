/*
 * Copyright 2018 scala-steward contributors
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

package eu.timepit.scalasteward

import better.files.File
import cats.effect.IO
import eu.timepit.scalasteward.model.Update

object sbt {
  def addGlobalPlugins: IO[Unit] =
    IO {
      val plugin = """addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")"""
      List(
        ".sbt/0.13/plugins",
        ".sbt/1.0/plugins"
      ).foreach { path =>
        val dir = File.home / path
        dir.createDirectories()
        (dir / "sbt-updates.sbt").writeText(plugin)
      }
    }

  def allUpdates(dir: File): IO[List[Update]] =
    io.exec(sbtCmd :+ ";dependencyUpdates ;reload plugins; dependencyUpdates", dir)
      .map(toDependencyUpdates)

  val sbtCmd: List[String] =
    List("sbt", "-no-colors")

  def toDependencyUpdates(lines: List[String]): List[Update] =
    lines
      .flatMap { line =>
        Update.fromString(line.replace("[info]", "").trim).toSeq
      }
      .distinct
      .sortBy(udate => (udate.groupId, udate.artifactId))
}
