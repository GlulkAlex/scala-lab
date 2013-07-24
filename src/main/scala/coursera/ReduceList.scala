object sessionReduceList {
  def main(args: Array[String]) = {
    def sumList(xs: List[Int]): Int = (0 :: xs) reduceLeft ((x, y) => x + y)

    val elems = List(1, 3, 5, 7)
    println(sumList(elems))
  }
}

// [info] Running sessionReduceList
// 16
// [success] Total time: 1 s, completed 24 juil. 2013 12:26:25
