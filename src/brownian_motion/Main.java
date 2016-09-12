package brownian_motion;

import brownian_motion.crash.Crash;
import brownian_motion.model.BMBoard;
import brownian_motion.model.BMParticle;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Main {
  private static final String OUTPUT = "files/output.xyz";
  private static final String TIME_PER_COLLISION_OUTPUT_PATH = "files/statistics/timePerCollision";
  private static final String FREQUENCIES_OUTPUT_PATH = "files/statistics/frequencies.txt";

  private static final int SIMULATION_TIME_SECONDS = 100;
  private static final int FRAMES_PER_SECOND = 60 * 5;
  private static final int PRINT_STEPS = 1;

  private static final boolean WITH_ANIMATION = false;
  private static final boolean RECORD_STATISTICS = false;

  public static void main(String[] args) {
//        run(BMParameters.getInitialBoard());
    collisionTimes();
  }

  public static void collisionTimes() {
    Integer[] particles = {11, 51, 101, 201, 401, 601, 801};

    for (int i = 0; i < particles.length; i++) {
      System.out.println("Particles: " + particles[i]);
      BMBoard board = BMParameters.getBoard(particles[i]);
      BMStats stats = run(board);

      recordTimePerCollision(TIME_PER_COLLISION_OUTPUT_PATH + "_" + String.valueOf(particles[i] + ".txt"), stats);
    }
  }

  public static void frequencies() throws FileNotFoundException, UnsupportedEncodingException {
    Integer[] particles = {11, 51, 101, 201, 401, 601, 801};

    List<Double> frequencies = new ArrayList();

    for (int i = 0; i < particles.length; i++) {
      System.out.println("Particles: " + particles[i]);
      BMBoard board = BMParameters.getBoard(particles[i]);
      BMStats stats = run(board);

      frequencies.add(stats.getCollisionsPerSecond());
    }

    recordFrequenciesTo(FREQUENCIES_OUTPUT_PATH, particles, frequencies);
  }

  private static void recordFrequenciesTo(String path, Integer[] particles, List<Double> frequencies) {
    PrintWriter basicWriter = null;
    try {
      basicWriter = new PrintWriter(path, "UTF-8");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    PrintWriter writer = new PrintWriter(basicWriter, true);

    System.out.println("Recording frequencies to file");

    for (int i = 0; i < particles.length; i++) {
      writer.println(particles[i] + "" + frequencies.get(i));
    }

    System.out.println("Finished recording frequencies");
  }

  private static BMStats run(BMBoard board) {
    int totalFrames = SIMULATION_TIME_SECONDS * FRAMES_PER_SECOND * PRINT_STEPS;
    int frames = 0;
    PrintWriter basicWriter = null;
    try {
      basicWriter = new PrintWriter(OUTPUT, "UTF-8");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    PrintWriter writer = new PrintWriter(basicWriter, true);

    double totalTime = 0;

    BMStats stats = new BMStats(board);

    while (frames < totalFrames) {
      if (WITH_ANIMATION) {
        if (frames % PRINT_STEPS == 0) {
          print(board, writer, frames);
        }
      }

      Crash crash = board.calculateTimeUntilNextCrash();
      stats.onCollision(crash.getTimeUntilCrash());
      totalTime += crash.getTimeUntilCrash();
      board.advanceTime(crash.getTimeUntilCrash());
      crash.applyCrash();
      frames += 1;

      System.out.println("Frame " + frames + " of " + totalFrames);
    }

    if (RECORD_STATISTICS) {
      recordTimePerCollision(TIME_PER_COLLISION_OUTPUT_PATH, stats);
    }

    writer.close();
    return stats;
  }

  private static void print(BMBoard board, PrintWriter writer, int time) {
    // Create 2 invisible particles in the diagonal
    writer.println(board.getParticles().size() + 2);
    writer.println(time);

    writer.println("0 0 0 0 0 0 0 0");
    writer.println(BMParameters.BOARD_SIDE_LENGTH + " " + BMParameters.BOARD_SIDE_LENGTH + " 0 0 0 0 0 0");

    for (BMParticle p : board.getParticles()) {
      writer.println(p.toDrawableString());
    }
  }

  private static void recordTimePerCollision(String path, BMStats stats) {
    PrintWriter basicWriter = null;
    try {
      basicWriter = new PrintWriter(path, "UTF-8");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    PrintWriter writer = new PrintWriter(basicWriter, true);

    System.out.println("Recording statistics to file");

    for (int i = 0; i < stats.timePerCollision.size(); i++) {
      writer.println(stats.timePerCollision.get(i));
    }

    System.out.println("Finished recording statistics");
  }
}
