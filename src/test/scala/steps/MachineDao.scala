package steps

import cats.effect.IO
import io.finch.{Application, Input, Output}
import cats.implicits.{catsStdInstancesForOption, toTraverseOps}
import com.github.morotsman.investigate_finagle_service.candy_finch.{App, MachineState}
import io.circe.generic.auto._
import io.finch.circe._

object MachineDao {

  def getMachines(app: App): IO[Option[Output[List[MachineState]]]] = {
    val input = Input.get("/machine")
    app.getMachines(input).output.sequence
  }

  def createMachine(app: App, machine: MachineWithoutId): IO[Option[Output[MachineState]]] = {
    val createMachineRequest = Input.post("/machine").withBody[Application.Json]
    app.createMachine(createMachineRequest(machine)).output.sequence
  }

  def insertCoin(app: App, machineId: Long): IO[Option[Output[MachineState]]] = {
    val insertCoinRequest = Input.put(s"/machine/$machineId/coin")
    app.insertCoin(insertCoinRequest).output.sequence
  }

}
