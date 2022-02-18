/**
 * The type Arguments exception.
 *
 * @author Bingfan Tian, Hongchao Fang
 */
public class ArgumentsException extends Exception {

  /**
   * Instantiates a new Arguments exception.
   *
   * @param massage the massage
   */
  public ArgumentsException(String massage) {
    super(massage);
    System.out.println(massage);
    System.out.println("Usage:\n" +
        " --directory  <path> accept a directory that contains\n "
        + "the csv files of course and student\n" +
        " --output <path> accept the name of a folder, all\n" +
        "output is placed in this folder\n"
        + "--threshold <number> accept a value that use to identify days\n"
        + "when each course had the most activity\n");
  }
}
