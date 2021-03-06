package concurrency.actor

import akka.actor.{Actor, Props, ActorRef}

// good practice to define messages inside companion object

object BankAccount {
  case class Deposit(amount: BigInt) {
    require(amount > 0)
  }

  case class Withdraw(amount: BigInt) {
    require(amount > 0)
  }

  case class Balance(name: String)

  case object Done
  case object Failed

}

class BankAccount extends Actor {
  import BankAccount._

  var balance = BigInt(0)

  def receive = {
    case Deposit(amount)  =>
      balance += amount
      sender ! Done
    case Withdraw(amount) =>
      if (balance < amount)
        sender ! Failed
      else {
        balance -= amount
        sender ! Done
      }
    case Balance(name)    =>
      sender ! (name, balance)
    case _                =>
      sender ! Failed
  }
}

object WireTransfer {
  case class Transfer(from: ActorRef, to: ActorRef, amount: BigInt)
  case object Done
  case object Failed
}

class WireTransfer extends Actor {
  import WireTransfer._

  def receive: Receive = {
    case Transfer(from, to, amount) =>
      from ! BankAccount.Withdraw(amount)
      context.become(awaitFrom(to, amount, sender))
  }

  def awaitFrom(to: ActorRef, amount: BigInt, customer: ActorRef): Receive = {
    case BankAccount.Done =>
      to ! BankAccount.Deposit(amount)
      context.become(awaitTo(customer))
    case BankAccount.Failed =>
      customer ! Failed
      context.stop(self)
  }

  def awaitTo(client: ActorRef): Receive = {
    case BankAccount.Done =>
      client ! Done
      context.stop(self)

    case BankAccount.Failed =>
      client ! Failed
      context.stop(self)
  }
}

class TransferMain extends Actor {
  val accountA = context.actorOf(Props[BankAccount], "accountA")
  val accountB = context.actorOf(Props[BankAccount], "accountB")

  accountA ! BankAccount.Deposit(80)
  accountB ! BankAccount.Deposit(20)

  def receive: Receive = {
    case BankAccount.Done => transfer(100)
  }

  def transfer(amount: BigInt): Unit = {
    val transaction = context.actorOf(Props[WireTransfer], "transfer")

    transaction ! WireTransfer.Transfer(accountA, accountB, amount)
    context.become({
                     case WireTransfer.Done =>
                       println("Wire transfer success!")
                       checkAccounts
                     case WireTransfer.Failed =>
                       println("Wire transfer failure!")
                       context.stop(self)
                   })
  }

  def checkAccounts: Unit = {
    accountA ! BankAccount.Balance("A")
    accountB ! BankAccount.Balance("B")

    context.become({case (name, amount) =>
                     println(s"Account '$name' amount: $amount")
                   })
  }
}
