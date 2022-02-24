import org.apache.commons.cli.Option;

/**
 * The type Arguments exception.
 *
 * @author Hongchao Fang
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
    System.out.println("Usage:\n"+" -- num_threads : maximum number of threads to run. \n"+
            "--num_skiers : number of skier to generate lift rides for. \n"+
            "--num_lifts : number of ski lifts. \n"+
            "--num_runs : number of runs for each user. \n"+
            "--ip : ip address for the server. \n"+
            "--port : port for the connection. \n");
  }
}
